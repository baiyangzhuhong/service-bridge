package cn.baiyang.apigateway.netty.enums;

import cn.baiyang.apigateway.constant.SysConstant;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

public enum ResponseEnum {

	DEFAULT_ERROR("系统异常!"),
	INSPECTION_SUCCESS("0", "no host or path is ignored"),
	READ_TIMEOUT_ERROR("读数据超时!"),
	WRITE_TIMEOUT_ERROR("写数据超时!"),
	ROUTE_ERROR,
	SERVER_UNREACHABLE_ERROR("业务服务连接不上或不可用!");

	private final static String SERIALIZE_FORMAT = "{\"code\":\"%s\",\"message\":\"%s\",\"stage\":\"%s\"}";

	private String code = "-1";

	private String message;

	private String stage = "API-Gateway";

	ResponseEnum() {
	}

	ResponseEnum(String message) {
		this.message = message;
	}

	ResponseEnum(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public HttpResponse build() {
		return getResponse(this.toString());
	}

	public HttpResponse build(String message) {
		return getResponse(String.format(SERIALIZE_FORMAT, code, message, stage));
	}

	private HttpResponse getResponse(String responseBody) {
		DefaultFullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
				Unpooled.wrappedBuffer(responseBody.getBytes()));
		response.headers()
				.set(HttpHeaderNames.CONTENT_TYPE, SysConstant.CONTENT_TYPE_JSON)
				.setInt(HttpHeaderNames.CONTENT_LENGTH,
						response.content().readableBytes());
		return response;
	}

	@Override
	public String toString() {
		return String.format(SERIALIZE_FORMAT, code, message, stage);
	}

}
