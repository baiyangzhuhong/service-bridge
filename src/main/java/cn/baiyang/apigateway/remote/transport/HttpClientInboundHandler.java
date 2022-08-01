package cn.baiyang.apigateway.remote.transport;

import cn.baiyang.apigateway.netty.enums.ResponseEnum;
import cn.baiyang.apigateway.util.HttpUtils;
import cn.baiyang.apigateway.util.LogUtils;

import java.net.SocketAddress;

import org.apache.commons.lang3.StringUtils;

import cn.baiyang.apigateway.constant.ChannelAttributeKeys;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.pool.ChannelPool;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.util.Attribute;
import io.netty.util.ReferenceCountUtil;

@ChannelHandler.Sharable
public class HttpClientInboundHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpResponse) {
			long now = System.currentTimeMillis();
			Attribute<Long> proxyStartAttr = ctx.channel()
					.attr(ChannelAttributeKeys.PROXY_START_KEY);
			Attribute<Long> proxyConnectedAttr = ctx.channel()
					.attr(ChannelAttributeKeys.PROXY_CONNECTED_KEY);
			long connectElapsed = proxyConnectedAttr.get() - proxyStartAttr.get();
			long handleElapsed = now - proxyConnectedAttr.get();

			Attribute<Channel> originalChannelAttr = ctx.channel()
					.attr(ChannelAttributeKeys.ORIGINAL_CHANNEL_KEY);
			Channel originalChannel = originalChannelAttr.get();
			HttpRequest request = ctx.channel().attr(ChannelAttributeKeys.REQUEST_KEY)
					.get();
			SocketAddress remoteAddress = ctx.channel().remoteAddress();
			SocketAddress localAddress = ctx.channel().localAddress();
			Attribute<String> traceIdAttr = null == originalChannel ? null
					: originalChannel.attr(ChannelAttributeKeys.TRACE_ID_KEY);
			String traceId = null == traceIdAttr ? "empty" : traceIdAttr.get();
			LogUtils.access(traceId, localAddress, remoteAddress, request,
					(HttpResponse) msg);

			if (originalChannel != null) {
				ChannelFuture future = originalChannel.writeAndFlush(msg)
						.addListener(f -> {
							long outputElapsed = System.currentTimeMillis() - now;
							long proxyElapsed = connectElapsed + handleElapsed
									+ outputElapsed;
							if (f.isSuccess()) {
								if (proxyElapsed > 1000L) {
									LogUtils.accessTimeout(traceId, localAddress,
											remoteAddress, request.uri(), connectElapsed,
											handleElapsed, outputElapsed);
								}

								HttpResponseStatus status = ((HttpResponse) msg).status();
								int statusCode = status.code();
								if (statusCode != HttpResponseStatus.OK.code()) {
									LogUtils.notOkReturned(traceId, localAddress,
											remoteAddress, request.uri(), statusCode);
								}
							}
							else {
								LogUtils.failedProxyOutput(traceId, localAddress,
										remoteAddress, request.uri(), connectElapsed,
										handleElapsed, outputElapsed, f.cause());
							}
						});
				if (request != null && HttpUtils.isClose(request)) {
					future.addListener(ChannelFutureListener.CLOSE);
				}
				originalChannelAttr.set(null);
			}
			else {
				ReferenceCountUtil.release(msg);
				LogUtils.originalChannelEmpty(request.uri(), localAddress, remoteAddress,
						request, (HttpResponse) msg);
			}

			Attribute<ChannelPool> channelPoolAttr = ctx.channel()
					.attr(ChannelAttributeKeys.CHANNEL_POOL_KEY);
			if (channelPoolAttr.get() != null) {
				channelPoolAttr.get().release(ctx.channel());
			}
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Attribute<Channel> originalChannelAttr = ctx.channel()
				.attr(ChannelAttributeKeys.ORIGINAL_CHANNEL_KEY);
		Channel originalChannel = originalChannelAttr.get();
		if (null != originalChannel) {
			Attribute<String> traceIdAttr = originalChannel
					.attr(ChannelAttributeKeys.TRACE_ID_KEY);
			LogUtils.clientChannelInactive(traceIdAttr.get());
		}
		ctx.fireChannelInactive();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		Channel channel = ctx.channel();
		Attribute<ChannelPool> channelPoolAttr = channel
				.attr(ChannelAttributeKeys.CHANNEL_POOL_KEY);
		if (channelPoolAttr.get() != null) {
			channelPoolAttr.get().release(channel);
		}

		Attribute<Channel> originalChannelAttr = channel
				.attr(ChannelAttributeKeys.ORIGINAL_CHANNEL_KEY);
		Channel originalChannel = originalChannelAttr.get();
		String traceId = "";
		if (null != originalChannel) {
			Attribute<String> traceIdAttr = originalChannel
					.attr(ChannelAttributeKeys.TRACE_ID_KEY);
			traceId = traceIdAttr.get();
		}
		Attribute<FullHttpRequest> originalRequestAttr = channel
				.attr(ChannelAttributeKeys.REQUEST_KEY);
		HttpRequest request = originalRequestAttr.get();
		SocketAddress remoteAddress = channel.remoteAddress();
		SocketAddress localAddress = channel.localAddress();
		LogUtils.serverProxyException(traceId, request.uri(), localAddress, remoteAddress,
				cause);

		if (originalChannel == null) {
			LogUtils.originalChannelEmpty(request.uri(), localAddress, remoteAddress,
					null, null);
			return;
		}

		String timeoutMsg = "";
		HttpResponse response = ResponseEnum.DEFAULT_ERROR.build();
		if (cause instanceof ReadTimeoutException) {
			response = ResponseEnum.READ_TIMEOUT_ERROR.build();
			timeoutMsg = "ReadTimeOut-Client";
		}
		else if (cause instanceof WriteTimeoutException) {
			response = ResponseEnum.WRITE_TIMEOUT_ERROR.build();
			timeoutMsg = "WriteTimeOut-Client";
		}
		if (StringUtils.isNotEmpty(timeoutMsg)) {
			LogUtils.timeout(traceId, timeoutMsg, request.uri(), cause);
		}
		originalChannel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

}
