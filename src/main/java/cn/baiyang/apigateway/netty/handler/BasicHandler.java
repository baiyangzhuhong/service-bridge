package cn.baiyang.apigateway.netty.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;

public class BasicHandler extends ChannelInboundHandlerAdapter {

	protected void endResponse(HttpResponse response, ChannelHandlerContext ctx,
			FullHttpRequest request) {
		HttpHeaders responseHeaders = response.headers();
		boolean keepAlive = HttpUtil.isKeepAlive(request);
		if (keepAlive) {
			if (!request.protocolVersion().isKeepAliveDefault()) {
				responseHeaders.set(CONNECTION, KEEP_ALIVE);
			}
		}
		else {
			responseHeaders.set(CONNECTION, CLOSE);
		}
		ChannelFuture f = ctx.writeAndFlush(response);
		if (!keepAlive) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

}
