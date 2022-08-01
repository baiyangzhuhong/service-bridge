package cn.baiyang.apigateway.netty.handler.plugins;

import cn.baiyang.apigateway.dao.component.DbComponent;
import cn.baiyang.apigateway.dao.component.vo.VersionEntity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.baiyang.apigateway.netty.handler.plugins.constant.PluginConstant;
import cn.baiyang.apigateway.netty.handler.plugins.enums.PluginErrorEnum;
import cn.baiyang.apigateway.netty.handler.plugins.helper.PluginHelper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

@Component
@ChannelHandler.Sharable
public class DefaultAuthenticationHandler extends PluginBasicHandler {

	private static Logger LOGGER = LoggerFactory
			.getLogger(DefaultAuthenticationHandler.class);

	@Resource
	private DbComponent dbComponent;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Boolean whitelist = ctx.channel().attr(PluginConstant.WHITELIST_KEY).get();
		FullHttpRequest request = (FullHttpRequest) msg;
		HttpHeaders headers = request.headers();
		if (whitelist) {
			headers.add(PluginConstant.HEADER_SOURCE_ID,
					PluginConstant.DEFAULT_SOURCE_ID);
			headers.add(PluginConstant.HEADER_SMS_SIGNATURE,
					PluginConstant.DEFAULT_SMS_SIGNATURE);
			ctx.fireChannelRead(msg);
			return;
		}

		String appId = PluginHelper.getAppId(headers);
		if (StringUtils.isBlank(appId)) {
			endErrorResponse(PluginErrorEnum.APPID_EMPTY, ctx, request);
			return;
		}
		boolean mobileClient = PluginHelper.mobileClient(appId);
		boolean webH5Client = PluginHelper.webH5Client(appId);
		if (!(PluginHelper.legalClient(appId) || mobileClient || webH5Client)) {
			endErrorResponse(PluginErrorEnum.APPID_WRONG, ctx, request);
			return;
		}

		String versionType = appId.startsWith(PluginConstant.APPID_PATIENT)
				? PluginConstant.VERSION_TYPE_PATIENT
				: PluginConstant.VERSION_TYPE_DOCTOR;
		if (headers.contains(PluginConstant.HEADER_VERSION_TYPE)) {
			headers.set(PluginConstant.HEADER_VERSION_TYPE, versionType);
		}
		else {
			headers.add(PluginConstant.HEADER_VERSION_TYPE, versionType);
		}

		if (mobileClient) {
			String userAgent = headers.get(PluginConstant.HEADER_USER_AGENT);
			if (StringUtils.isBlank(userAgent)) {
				endErrorResponse(PluginErrorEnum.APPID_UA_EMPTY, ctx, request);
				return;
			}
			else if (userAgent.equals(PluginConstant.UA_ANDROID)) {
				if (!appId.contains("_android_")) {
					endErrorResponse(PluginErrorEnum.APPID_ILLEGAL, ctx, request);
					return;
				}
			}
			else if (userAgent.equals(PluginConstant.UA_IOS)) {
				if (!appId.contains("_ios_")) {
					endErrorResponse(PluginErrorEnum.APPID_ILLEGAL, ctx, request);
					return;
				}
			}
			else {
				endErrorResponse(PluginErrorEnum.APPID_UA_WRONG, ctx, request);
				return;
			}
		}

		VersionEntity versionEntity = dbComponent.getVersionByAppId(appId);
		if (null == versionEntity) {
			endErrorResponse(PluginErrorEnum.APPID_VERSION_LACK, ctx, request);
			return;
		}
		if (!webH5Client) {
			String version = PluginHelper.getVersion(headers);
			ctx.channel().attr(PluginConstant.VERSION_KEY).set(version);
			try {
				float versionNum = PluginHelper.parseVersion(version);
				if (versionNum < versionEntity.getLowestVersion()) {
					endErrorResponse(PluginErrorEnum.VERSION_LOWER_ERR, ctx, request);
					return;
				}
				ctx.channel().attr(PluginConstant.VERSION_NUM_KEY).set(versionNum);
			}
			catch (IllegalArgumentException e) {
				LOGGER.error("exception happened when verify version number, appId={}",
						appId, e);
				endErrorResponse(PluginErrorEnum.VERSION_FORMAT_ERR, ctx, request);
				return;
			}
		}

		ctx.channel().attr(PluginConstant.APPID_KEY).set(appId);
		ctx.channel().attr(PluginConstant.WEBH5CLIENT_KEY).set(webH5Client);
		ctx.channel().attr(PluginConstant.MOBILECLIENT_KEY).set(mobileClient);
		ctx.channel().attr(PluginConstant.VERSION_ENTITY_KEY).set(versionEntity);
		headers.add(PluginConstant.HEADER_SOURCE_ID, versionEntity.getSourceId());
		headers.add(PluginConstant.HEADER_SMS_SIGNATURE,
				encode(versionEntity.getSmsSignature()));

		ctx.fireChannelRead(msg);
	}

	private String encode(String smsSignature) throws UnsupportedEncodingException {
		if (StringUtils.isBlank(smsSignature)) {
			return "";
		}

		String value = smsSignature.replace("\n", "");
		for (int i = 0, length = value.length(); i < length; i++) {
			char c = value.charAt(i);
			if (c <= '\u001f' || c >= '\u007f') {
				return URLEncoder.encode(value, "UTF-8");
			}
		}
		return value;
	}

}
