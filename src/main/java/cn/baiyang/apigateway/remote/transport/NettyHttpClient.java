package cn.baiyang.apigateway.remote.transport;

import cn.baiyang.apigateway.util.HttpUtils;
import cn.baiyang.apigateway.constant.ChannelAttributeKeys;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static cn.baiyang.apigateway.netty.enums.ResponseEnum.SERVER_UNREACHABLE_ERROR;

public class NettyHttpClient {

	private static final Logger logger = LoggerFactory.getLogger(NettyHttpClient.class);

	private static final int DEFAULT_IO_THREADS = Math
			.max(Runtime.getRuntime().availableProcessors() * 4, 128);

	private final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(
			DEFAULT_IO_THREADS, new DefaultThreadFactory("NettyClientWorker", true));

	private final HttpClientInboundHandler httpClientInboundHandler = new HttpClientInboundHandler();

	private final int connectTimeout;

	private final int maxContentLength;

	private Bootstrap bootstrap;

	private ChannelPoolMap<InetSocketAddress, SimpleChannelPool> channelPoolMap;

	private NettyHttpClient(Builder builder) {
		this.connectTimeout = builder.connectTimeout;
		this.maxContentLength = builder.maxContentLength;
		init();
	}

	// 初始化bootstrap
	private void init() {
		bootstrap = new Bootstrap();
		bootstrap.group(nioEventLoopGroup).option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
				.channel(NioSocketChannel.class);

		channelPoolMap = new AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>() {
			@Override
			protected SimpleChannelPool newPool(InetSocketAddress key) {
				Bootstrap bs = bootstrap.clone();
				return new SimpleChannelPool(bs.remoteAddress(key),
						new ChannelPoolHandler() {
							public void channelReleased(Channel ch) throws Exception {
							}

							public void channelAcquired(Channel ch) throws Exception {
							}

							public void channelCreated(Channel ch) throws Exception {
								if (key.getPort() == 443) {
									SslContext sslContext = SslContextBuilder.forClient()
											.build();
									SSLEngine engine = sslContext.newEngine(ch.alloc());
									ch.pipeline().addFirst("ssl", new SslHandler(engine));
								}
								ch.pipeline().addLast(new HttpClientCodec())
										.addLast(new HttpObjectAggregator(
												maxContentLength))
										.addLast(httpClientInboundHandler);
							}
						});
			}
		};
	}

	public void send(String server, FullHttpRequest request, Channel originalChannel,
			Long proxyServerStartTime) {
		InetSocketAddress remoteAddr = HttpUtils.getSocketAddressFromHost(server);
		final SimpleChannelPool channelPool = channelPoolMap.get(remoteAddr);
		Future<Channel> channelFuture = channelPool.acquire();
		channelFuture.addListener((FutureListener<Channel>) future -> {
			if (future.isSuccess()) {
				Channel channel = future.getNow();
				request.headers().set(HttpHeaderNames.HOST,
						HttpUtils.getHostName(server));

				channel.attr(ChannelAttributeKeys.ORIGINAL_CHANNEL_KEY)
						.set(originalChannel);
				channel.attr(ChannelAttributeKeys.CHANNEL_POOL_KEY).set(channelPool);
				channel.attr(ChannelAttributeKeys.REQUEST_KEY).set(request);
				channel.attr(ChannelAttributeKeys.PROXY_START_KEY)
						.set(proxyServerStartTime); // 请求收到时间
				channel.attr(ChannelAttributeKeys.PROXY_CONNECTED_KEY)
						.set(System.currentTimeMillis()); // 连接建立时间

				channel.writeAndFlush(request);
			}
			else {
				logger.error("traceId={}, failed to connect server={}, uri={}",
						originalChannel.attr(ChannelAttributeKeys.TRACE_ID_KEY).get(),
						server, request.uri(), future.cause());
				ReferenceCountUtil.release(request);
				originalChannel.writeAndFlush(SERVER_UNREACHABLE_ERROR.build())
						.addListener(ChannelFutureListener.CLOSE);
			}
		});
	}

	public Future<?> shutdownGracefully() {
		return nioEventLoopGroup.shutdownGracefully();
	}

	public static final class Builder {

		int connectTimeout = 10_000;

		int maxContentLength = 1 << 28;// 256MB

		public NettyHttpClient.Builder connectTimeout(long timeout) {
			connectTimeout(timeout, TimeUnit.MILLISECONDS);
			return this;
		}

		public NettyHttpClient.Builder connectTimeout(long timeout, TimeUnit unit) {
			connectTimeout = checkDuration("timeout", timeout, unit);
			return this;
		}

		public NettyHttpClient.Builder maxContentLength(int maxContentLength) {
			if (maxContentLength <= 0)
				throw new IllegalArgumentException("maxContentLength too small.");
			this.maxContentLength = maxContentLength;
			return this;
		}

		private int checkDuration(String name, long duration, TimeUnit unit) {
			if (duration < 0)
				throw new IllegalArgumentException(name + " < 0");
			if (unit == null)
				throw new NullPointerException("unit == null");
			long millis = unit.toMillis(duration);
			if (millis > Integer.MAX_VALUE)
				throw new IllegalArgumentException(name + " too large.");
			if (millis == 0 && duration > 0)
				throw new IllegalArgumentException(name + " too small.");
			return (int) millis;
		}

		public NettyHttpClient build() {
			return new NettyHttpClient(this);
		}

	}

}
