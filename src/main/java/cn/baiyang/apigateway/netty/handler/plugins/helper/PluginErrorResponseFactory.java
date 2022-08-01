package cn.baiyang.apigateway.netty.handler.plugins.helper;

import cn.baiyang.apigateway.netty.handler.plugins.enums.PluginErrorEnum;

import cn.baiyang.apigateway.constant.SysConstant;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class PluginErrorResponseFactory {

	public static HttpResponse buildErrorResponse(PluginErrorEnum errorEnum) {
		DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
				Unpooled.wrappedBuffer(errorEnum.toString().getBytes()));
		httpResponse.headers()
				.set(HttpHeaderNames.CONTENT_TYPE, SysConstant.CONTENT_TYPE_JSON)
				.setInt(HttpHeaderNames.CONTENT_LENGTH,
						httpResponse.content().readableBytes());
		return httpResponse;
	}

}
