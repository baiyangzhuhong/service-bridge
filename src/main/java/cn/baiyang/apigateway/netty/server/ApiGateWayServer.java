package cn.baiyang.apigateway.netty.server;

import cn.baiyang.apigateway.common.DbInitEvent;
import cn.baiyang.apigateway.config.ApiGatewayProperties;
import cn.baiyang.apigateway.netty.handler.HttpServerChannelInitializer;
import cn.baiyang.apigateway.netty.server.lifecycle.AggregateLifeCycle;
import cn.baiyang.apigateway.util.LogUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ApiGateWayServer extends AggregateLifeCycle {

	@Resource
	private ApiGatewayProperties apiGateway;

	@Resource
	private HttpServerChannelInitializer httpServerChannelInitializer;

	private EventLoopGroup bossGroup;

	private EventLoopGroup workerGroup;

	private volatile AtomicBoolean isStarted = new AtomicBoolean(false);

	@Override
	public void doStart() throws Exception {
		super.doStart();
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 1024)
					.childOption(ChannelOption.SO_KEEPALIVE, false)
					.childOption(ChannelOption.TCP_NODELAY, true)
					.childHandler(httpServerChannelInitializer).bind(apiGateway.getPort())
					.sync().channel().closeFuture().sync();
		}
		catch (InterruptedException e) {
			LogUtils.systemError("failed to start netty server", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void doStop() throws Exception {
		bossGroup.shutdownGracefully();
		httpServerChannelInitializer.getHttpClient().shutdownGracefully().await();
		workerGroup.shutdownGracefully();
		super.doStop();
	}

	/**
	 * 启动netty server
	 * @param event 预加载数据准备就绪
	 */
	@EventListener
	public void startServer(DbInitEvent event) throws Exception {
		if (!isStarted.compareAndSet(false, true)) {
			return;
		}
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup(apiGateway.getWorkThreadsSize());
		// 启动不加锁，因为jetty start会阻塞线程
		setSynchronizeStart(false);
		start();
	}

}