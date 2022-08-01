package cn.baiyang.apigateway.netty.handler;

import cn.baiyang.apigateway.netty.handler.plugins.DefaultAuthenticationHandler;
import cn.baiyang.apigateway.netty.handler.plugins.SignHandler;
import cn.baiyang.apigateway.netty.handler.plugins.URLRedirectHandler;
import cn.baiyang.apigateway.netty.handler.plugins.URLWhitelistHandler;
import cn.baiyang.apigateway.netty.handler.plugins.helper.CorsConfigHelper;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.cors.CorsHandler;

@Component
public class PluginPipeline {

	@Resource
	private URLWhitelistHandler urlWhitelistHandler;

	@Resource
	private DefaultAuthenticationHandler defaultAuthenticationHandler;

	@Resource
	private SignHandler signHandler;

	@Resource
	private URLRedirectHandler urlRedirectHandler;

	public void assemble(ChannelPipeline pipeline) {
		pipeline.addLast(new CorsHandler(CorsConfigHelper.getCorsConfig()));
		pipeline.addLast(urlWhitelistHandler);
		pipeline.addLast(defaultAuthenticationHandler);
		pipeline.addLast(signHandler);
		pipeline.addLast(urlRedirectHandler);
	}

}
