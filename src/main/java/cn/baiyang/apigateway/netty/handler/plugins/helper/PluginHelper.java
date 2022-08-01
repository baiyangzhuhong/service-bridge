package cn.baiyang.apigateway.netty.handler.plugins.helper;

import static cn.baiyang.apigateway.netty.handler.plugins.constant.PluginConstant.APPID_DOCTOR;
import static cn.baiyang.apigateway.netty.handler.plugins.constant.PluginConstant.APPID_PATIENT;
import static cn.baiyang.apigateway.netty.handler.plugins.constant.PluginConstant.HEADER_APPID;
import static cn.baiyang.apigateway.netty.handler.plugins.constant.PluginConstant.HEADER_AUTH;
import static cn.baiyang.apigateway.netty.handler.plugins.constant.PluginConstant.HEADER_AUTHTOKEN;
import static cn.baiyang.apigateway.netty.handler.plugins.constant.PluginConstant.HEADER_VERSION;
import static cn.baiyang.apigateway.netty.handler.plugins.constant.PluginConstant.HEADER_WEIYI_APPID;
import static cn.baiyang.apigateway.netty.handler.plugins.constant.PluginConstant.HEADER_WEIYI_AUTHTOKEN;
import static cn.baiyang.apigateway.netty.handler.plugins.constant.PluginConstant.HEADER_WEIYI_VERSION;

import org.apache.commons.lang3.StringUtils;

import io.netty.handler.codec.http.HttpHeaders;

public class PluginHelper {

	public static String getToken(HttpHeaders headers) {
		String token = headers.get(HEADER_WEIYI_AUTHTOKEN);
		if (StringUtils.isBlank(token)) {
			token = headers.get(HEADER_AUTH);
		}
		if (StringUtils.isBlank(token)) {
			token = headers.get(HEADER_AUTHTOKEN);
		}
		return StringUtils.isBlank(token) ? "" : token;
	}

	public static float parseVersion(String version) {
		float versionNum = 0.0f;
		if (StringUtils.isNotBlank(version)) {
			try {
				versionNum = Float.parseFloat(version);
			}
			catch (NumberFormatException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return versionNum;
	}

	public static String getVersion(HttpHeaders headers) {
		String version = headers.get(HEADER_WEIYI_VERSION);
		return StringUtils.isNotBlank(version) ? version : headers.get(HEADER_VERSION);
	}

	public static String getAppId(HttpHeaders headers) {
		String appId = headers.get(HEADER_WEIYI_APPID);
		return StringUtils.isNotBlank(appId) ? appId : headers.get(HEADER_APPID);
	}

	public static boolean legalClient(String appId) {
		return appId.startsWith(APPID_PATIENT) || appId.startsWith(APPID_DOCTOR);
	}

	public static boolean webH5Client(String appId) {
		if (StringUtils.isBlank(appId)) {
			return false;
		}
		return appId.startsWith("p_h5_") || appId.startsWith("p_web_")
				|| appId.startsWith("d_h5_") || appId.startsWith("d_web_");
	}

	public static boolean mobileClient(String appId) {
		if (StringUtils.isBlank(appId)) {
			return false;
		}
		return appId.startsWith("p_android_") || appId.startsWith("p_ios_")
				|| appId.startsWith("d_android_") || appId.startsWith("d_ios_");
	}

}
