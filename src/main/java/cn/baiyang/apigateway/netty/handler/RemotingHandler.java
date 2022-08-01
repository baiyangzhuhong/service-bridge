package cn.baiyang.apigateway.netty.handler;

import cn.baiyang.apigateway.context.RequestContext;
import cn.baiyang.apigateway.exception.RouteException;
import cn.baiyang.apigateway.netty.enums.ResponseEnum;
import cn.baiyang.apigateway.remote.route.Route;
import cn.baiyang.apigateway.remote.transport.NettyHttpClient;
import cn.baiyang.apigateway.util.LogUtils;
import cn.baiyang.apigateway.constant.ChannelAttributeKeys;
import cn.baiyang.apigateway.constant.SysConstant;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.Attribute;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ChannelHandler.Sharable
public class RemotingHandler extends ChannelInboundHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(RemotingHandler.class);

	@Resource
	private Route route;

	private NettyHttpClient httpClient;

	public void setHttpClient(NettyHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		FullHttpRequest request = (FullHttpRequest) msg;

		Attribute<String> traceIdAttr = ctx.channel()
				.attr(ChannelAttributeKeys.TRACE_ID_KEY);
		traceIdAttr.set(request.headers().get(SysConstant.TRACE_ID_NAME));

		String server = route.getRouteServer(request);
		httpClient.send(server, request, ctx.channel(), System.currentTimeMillis());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		HttpRequest request = RequestContext.getCurrentContext().getRequest();
		String traceId = ctx.channel().attr(ChannelAttributeKeys.TRACE_ID_KEY).get();
		HttpResponse response = ResponseEnum.DEFAULT_ERROR.build();
		if (cause instanceof RouteException) {
			logger.error("traceId={}, uri={}, error={}", traceId, request.uri(),
					cause.getMessage());
			response = ResponseEnum.ROUTE_ERROR.build(cause.getMessage());
		}
		else {
			LogUtils.exceptionGot(traceId, request, cause);
		}

		ReferenceCountUtil.release(request);
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

}
