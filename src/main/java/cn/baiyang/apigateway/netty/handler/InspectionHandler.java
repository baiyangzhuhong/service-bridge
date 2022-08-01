package cn.baiyang.apigateway.netty.handler;

import cn.baiyang.apigateway.constant.SysConstant;
import cn.baiyang.apigateway.context.RequestContext;
import cn.baiyang.apigateway.netty.enums.ResponseEnum;

import java.net.SocketAddress;
import java.net.URI;

import cn.baiyang.apigateway.constant.ChannelAttributeKeys;
import io.netty.handler.timeout.ReadTimeoutException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

@Component
@ChannelHandler.Sharable
public class InspectionHandler extends BasicHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(InspectionHandler.class);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (!(msg instanceof HttpRequest)) {
			throw new RuntimeException("目前仅支持http请求方式");
		}

		FullHttpRequest request = (FullHttpRequest) msg;
		URI uri = new URI(request.uri());
		String traceId = request.headers().get(SysConstant.TRACE_ID_NAME);
		String host = request.headers().get(HttpHeaderNames.HOST);
		SocketAddress remoteAddress = ctx.channel().remoteAddress();
		SocketAddress localAddress = ctx.channel().localAddress();
		LOGGER.info(
				"client request coming, traceId={}, remoteAddress={},localAddress={}, host={}, uri={}",
				traceId, remoteAddress, localAddress, host, uri);

		String path = uri.getPath();
		boolean isIgnorePath = StringUtils.isBlank(path) || path.equals("/")
				|| path.equals(SysConstant.FAVICON_ICO)
				|| path.startsWith(SysConstant.MANAGEMENT_CONTEXT_PATH);
		boolean isBlankHost = StringUtils.isBlank(host);
		if (isBlankHost || isIgnorePath) {
			HttpResponse response = ResponseEnum.INSPECTION_SUCCESS.build(); // 拨测监控用
			endResponse(response, ctx, request);
			return;
		}

		RequestContext requestContext = RequestContext.getCurrentContext();
		requestContext.setRequest(request);
		ctx.fireChannelRead(msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		if (cause instanceof ReadTimeoutException) {
			LOGGER.warn("traceId={}, read timeout from client, client ip={}",
					ctx.channel().attr(ChannelAttributeKeys.TRACE_ID_KEY).get(),
					ctx.channel().remoteAddress());
			return;
		}
		super.exceptionCaught(ctx, cause);
	}

}
