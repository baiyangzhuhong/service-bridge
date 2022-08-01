package cn.baiyang.apigateway.netty.handler.plugins;

import cn.baiyang.apigateway.dao.component.DbComponent;
import cn.baiyang.apigateway.dao.component.vo.VersionEntity;
import cn.baiyang.apigateway.dao.version.model.VersionInfoDO;
import cn.baiyang.apigateway.netty.handler.plugins.enums.SignVersionEnum;
import cn.baiyang.apigateway.util.Md5Utils;
import cn.baiyang.apigateway.util.RSAUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.baiyang.apigateway.constant.SysConstant;
import cn.baiyang.apigateway.netty.handler.plugins.constant.PluginConstant;
import cn.baiyang.apigateway.netty.handler.plugins.enums.PluginErrorEnum;
import cn.baiyang.apigateway.netty.handler.plugins.helper.PluginHelper;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;

@Component
@ChannelHandler.Sharable
public class SignHandler extends PluginBasicHandler {

	private static Logger LOGGER = LoggerFactory.getLogger(SignHandler.class);

	@Resource
	private DbComponent dbComponent;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Boolean whitelist = ctx.channel().attr(PluginConstant.WHITELIST_KEY).get();
		boolean webH5Client = ctx.channel().attr(PluginConstant.WEBH5CLIENT_KEY).get();
		FullHttpRequest request = (FullHttpRequest) msg;
		HttpHeaders headers = request.headers();
		String contentEncoding = headers.get(HttpHeaderNames.CONTENT_ENCODING);
		boolean contentZipped = StringUtils.isNotBlank(contentEncoding)
				&& contentEncoding.contains(HttpHeaderValues.GZIP);
		String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
		boolean fileUpload = StringUtils.isNotBlank(contentType)
				&& contentType.contains(HttpHeaderValues.MULTIPART_FORM_DATA);
		if (whitelist || webH5Client || contentZipped || fileUpload) {
			ctx.fireChannelRead(msg);
			return;
		}

		String sign = headers.get(PluginConstant.HEADER_SIGN);
		if (StringUtils.isBlank(sign)) {
			endErrorResponse(PluginErrorEnum.SIGN_EMPTY, ctx, request);
			return;
		}

		String uri = request.uri();
		String bodyInfo = getRequestBody(request, uri);
		if (StringUtils.isBlank(bodyInfo) || "{}".equals(bodyInfo.trim())) {
			ctx.fireChannelRead(msg);
			return;
		}

		String appId = ctx.channel().attr(PluginConstant.APPID_KEY).get();
		String data = getDigestData(ctx, headers, uri, bodyInfo);
		String md5 = Md5Utils.getMD5(data);
		String version = ctx.channel().attr(PluginConstant.VERSION_KEY).get();
		String traceId = headers.get(SysConstant.TRACE_ID_NAME);
		try {
			String rsaMd5 = RSAUtils.decrypt(sign,
					dbComponent.getPrivateKey(appId, version));
			if (!md5.equals(rsaMd5)) {
				LOGGER.error(
						"sign is wrong, traceId={}, appId={}, version={}, md5={}, rsaMD5={}, crpto_data={}",
						traceId, appId, version, md5, rsaMd5, data);
				endErrorResponse(PluginErrorEnum.SIGN_WRONG, ctx, request);
				return;
			}
		}
		catch (Exception e) {
			LOGGER.error(
					"exception happened when verify sign, traceId={}, appId={}, version={}, crpto_data={}",
					traceId, appId, version, data, e);
			endErrorResponse(PluginErrorEnum.SIGN_VERIFY_FAILED, ctx, request);
			return;
		}

		ctx.fireChannelRead(msg);
	}

	private String getDigestData(ChannelHandlerContext ctx, HttpHeaders headers,
			String uri, String bodyInfo) throws Exception {
		Float versionNum = ctx.channel().attr(PluginConstant.VERSION_NUM_KEY).get();
		String version = ctx.channel().attr(PluginConstant.VERSION_KEY).get();
		String token = PluginHelper.getToken(headers);
		VersionEntity versionEntity = ctx.channel()
				.attr(PluginConstant.VERSION_ENTITY_KEY).get();
		StringBuilder compose = new StringBuilder();
		compose.append(bodyInfo);
		if (!requireNewSign(versionEntity, versionNum)) {
			compose.append("_").append(token);
		}
		else {
			String headerInfo = populateHeaderInfo(headers, token, version);
			if (StringUtils.isNotBlank(headerInfo)) {
				compose.append("_").append(headerInfo);
			}
			if (StringUtils.isNotBlank(uri)) {
				compose.append("_").append(uri);
			}
		}
		return compose.toString();
	}

	private String getRequestBody(FullHttpRequest request, String uri)
			throws IOException {
		String bodyInfo = "";
		if (StringUtils.isNotBlank(uri)) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			InputStream in = new ByteBufInputStream(request.content());
			IOUtils.copy(in, os);
			in.reset();
			bodyInfo = os.toString();
		}
		return bodyInfo;
	}

	private boolean requireNewSign(VersionEntity versionEntity, Float versionNum) {
		if (Objects.isNull(versionEntity)) {
			return false;
		}
		Long versionClientId = versionEntity.getVersionClientId();
		VersionInfoDO versionInfoDO = dbComponent.getVersionInfoByClientIdAndVersion(
				versionClientId, Objects.toString(versionNum));
		Integer signVersion = Objects.nonNull(versionInfoDO)
				? versionInfoDO.getSignVersion() : versionEntity.getSignVersion();
		return !SignVersionEnum.ZERO.getVal().equals(signVersion);
	}

	private String populateHeaderInfo(HttpHeaders headers, String token, String version) {
		List<String> headerInfo = new ArrayList<>();
		if (StringUtils.isNotBlank(token)) {
			headerInfo.add(token);
		}
		if (StringUtils.isNotBlank(version)) {
			headerInfo.add(version);
		}
		String reqUuid = headers.get(PluginConstant.HEADER_REQ_UUID);
		if (StringUtils.isNotBlank(reqUuid)) {
			headerInfo.add(reqUuid);
		}
		String osVersion = headers.get(PluginConstant.HEADER_OS_VERSION);
		if (StringUtils.isNotBlank(osVersion)) {
			headerInfo.add(osVersion);
		}
		String osTokenId = headers.get(PluginConstant.HEADER_OS_TOKEN_ID);
		if (StringUtils.isNotBlank(osTokenId)) {
			headerInfo.add(osTokenId);
		}
		String phoneModel = headers.get(PluginConstant.HEADER_PHONE_MODEL);
		if (StringUtils.isNotBlank(phoneModel)) {
			headerInfo.add(phoneModel);
		}
		return String.join("_", headerInfo);
	}

}
