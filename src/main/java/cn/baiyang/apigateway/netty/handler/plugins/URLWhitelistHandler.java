package cn.baiyang.apigateway.netty.handler.plugins;

import org.springframework.stereotype.Component;

import cn.baiyang.apigateway.netty.handler.plugins.constant.PluginConstant;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

@Component
@ChannelHandler.Sharable
public class URLWhitelistHandler extends ChannelInboundHandlerAdapter {

	private static final String[] URL_WHITELIST = { "/cic/patn/ord/pay/callback",
			"/cic/patn/ord/refund/callback", "/common/appdevice/collect.json",
			"/common/appstartpage/getimgurl.json", "/common/area/allarealist.json",
			"/common/area/getareabyip.json", "/common/area/getchildlist.json",
			"/common/area/getprovincelist.json", "/common/area/listopenedcitys.json",
			"/common/area/toastplatform.json", "/common/businessconfig/geth5url.json",
			"/common/businessconfig/remindmenulist.json",
			"/common/searchtopwords/default.json", "/common/version/downurl.json",
			"/common/version/getinfo.json", "/common/version/lastupgradeinfo.json",
			"/common/version/startcheck.json", "/modulehc/promotion/adclick",
			"/modulelog/loopbackcdns.json", "/modulelog/trace", "/mtc/halove/login.json",
			"/notify", "/ordercentre/refund/rest", "/ordercentre/rest/coupon/list",
			"/ordercentre/rest/pay", "/ordercentre/rest/refund", "/paynotify",
			"/refundnotify" };

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		FullHttpRequest request = (FullHttpRequest) msg;
		String uri = request.uri();
		for (String whiteUrl : URL_WHITELIST) {
			if (uri.contains(whiteUrl)) {
				ctx.channel().attr(PluginConstant.WHITELIST_KEY).set(true);
				ctx.fireChannelRead(msg);
				return;
			}
		}

		ctx.channel().attr(PluginConstant.WHITELIST_KEY).set(false);
		ctx.fireChannelRead(msg);
	}

}
