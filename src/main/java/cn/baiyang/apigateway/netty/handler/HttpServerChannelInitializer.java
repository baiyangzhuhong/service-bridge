package cn.baiyang.apigateway.netty.handler;

import cn.baiyang.apigateway.config.ApiGatewayProperties;
import cn.baiyang.apigateway.remote.transport.NettyHttpClient;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import io.netty.util.concurrent.Future;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

@Component
public class HttpServerChannelInitializer extends ChannelInitializer<SocketChannel>
		implements InitializingBean {

	@Resource
	private ApiGatewayProperties apiGatewayProperties;

	private NettyHttpClient httpClient;

	@Resource
	private InspectionHandler inspectionHandler;

	@Resource
	private RemotingHandler remotingHandler;

	@Value("${plugins.enabled}")
	private Boolean pluginsEnabled;

	@Resource
	private PluginPipeline pluginPipeline;

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		ApiGatewayProperties.Server serverConfig = apiGatewayProperties.getServer();
		pipeline.addLast(new ReadTimeoutHandler(serverConfig.getReadTimeout(),
				TimeUnit.MILLISECONDS));
		pipeline.addLast(new WriteTimeoutHandler(serverConfig.getWriteTimeout(),
				TimeUnit.MILLISECONDS));
		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new HttpObjectAggregator(1024 * 1024 * 1024));

		pipeline.addLast(inspectionHandler);

		if (null != pluginsEnabled && pluginsEnabled) {
			pluginPipeline.assemble(pipeline);
		}

		remotingHandler.setHttpClient(httpClient);
		pipeline.addLast(remotingHandler);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		httpClient = new NettyHttpClient.Builder()
				.connectTimeout(apiGatewayProperties.getHttpClient().getConnectTimeout())
				.build();
	}

	public NettyHttpClient getHttpClient() {
		return httpClient;
	}

}
