package cn.baiyang.apigateway.netty.handler.plugins;

import cn.baiyang.apigateway.netty.handler.BasicHandler;
import cn.baiyang.apigateway.netty.handler.plugins.enums.PluginErrorEnum;

import cn.baiyang.apigateway.netty.handler.plugins.helper.PluginErrorResponseFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class PluginBasicHandler extends BasicHandler {

	protected void endErrorResponse(PluginErrorEnum errorEnum, ChannelHandlerContext ctx,
			FullHttpRequest request) {
		HttpResponse response = PluginErrorResponseFactory.buildErrorResponse(errorEnum);
		endResponse(response, ctx, request);
	}

}
