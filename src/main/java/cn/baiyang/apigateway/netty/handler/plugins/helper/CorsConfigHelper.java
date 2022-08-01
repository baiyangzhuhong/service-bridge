package cn.baiyang.apigateway.netty.handler.plugins.helper;

import cn.baiyang.apigateway.constant.SysConstant;
import cn.baiyang.apigateway.netty.handler.plugins.constant.PluginConstant;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;

public class CorsConfigHelper {

	private static final Long MAX_AGE = 7 * 24 * 60 * 60L;

	private static final String[] ALLOWED_HEADERS = { "Content-Type",
			PluginConstant.HEADER_VERSION_TYPE, PluginConstant.HEADER_OS_TOKEN_ID,
			PluginConstant.HEADER_AB_BARREL_LD, PluginConstant.HEADER_REQ_UUID,
			SysConstant.TRACE_ID_NAME, PluginConstant.HEADER_SAMPLED_NAME,
			PluginConstant.HEADER_SPAN_ID_NAME, PluginConstant.HEADER_PARENT_SPAN_ID_NAME,
			PluginConstant.HEADER_ACCEPT_LANGUAGE, PluginConstant.HEADER_TONGDUN_FP,
			PluginConstant.HEADER_X_GT_B_BIZ_PARENT_SOURCE_ID,
			PluginConstant.HEADER_X_GT_B_BIZ_SOURCE_ID, SysConstant.X_GLOBAL_ROUTE_TAG,
			PluginConstant.HEADER_WEIYI_AUTHTOKEN, PluginConstant.HEADER_AUTHTOKEN,
			PluginConstant.HEADER_AUTH, PluginConstant.HEADER_APPID,
			PluginConstant.HEADER_WEIYI_APPID, PluginConstant.HEADER_VERSION,
			PluginConstant.HEADER_WEIYI_VERSION, PluginConstant.HEADER_MONITOR_TAG,
			PluginConstant.HEADER_X_GT_B_FLOWID, PluginConstant.HEADER_X_FP_CODE,
			PluginConstant.HEADER_X_GT_B_PAGEID };

	private static final String[] EXPOSED_HEADERS = {
			PluginConstant.HEADER_AB_BARREL_LD };

	public static CorsConfig getCorsConfig() {
		return CorsConfigBuilder.forAnyOrigin().shortCircuit()
				.allowedRequestHeaders(ALLOWED_HEADERS).exposeHeaders(EXPOSED_HEADERS)
				.allowedRequestMethods(HttpMethod.OPTIONS, HttpMethod.POST,
						HttpMethod.GET)
				.maxAge(MAX_AGE).build();
	}

}
