package cn.baiyang.apigateway.netty.handler.plugins;

import cn.baiyang.apigateway.dao.component.DbComponent;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import cn.baiyang.apigateway.netty.handler.plugins.constant.PluginConstant;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

@Component
@ChannelHandler.Sharable
public class URLRedirectHandler extends ChannelInboundHandlerAdapter {

	@Resource
	private DbComponent dbComponent;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Boolean whitelist = ctx.channel().attr(PluginConstant.WHITELIST_KEY).get();
		FullHttpRequest request = (FullHttpRequest) msg;
		if (whitelist) {
			ctx.fireChannelRead(msg);
			return;
		}

		String uri = request.uri();
		String redirectUrl = dbComponent.getRedirectUrl(uri);
		if (StringUtils.isNotBlank(redirectUrl)) {
			request.setUri(redirectUrl);
		}
		ctx.fireChannelRead(msg);
	}

}
