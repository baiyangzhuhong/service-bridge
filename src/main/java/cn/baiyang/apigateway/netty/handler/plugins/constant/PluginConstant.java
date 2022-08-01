package cn.baiyang.apigateway.netty.handler.plugins.constant;

import cn.baiyang.apigateway.dao.component.vo.VersionEntity;

import io.netty.util.AttributeKey;

public class PluginConstant {

	public static final String HEADER_APPID = "appid";

	public static final String HEADER_WEIYI_APPID = "weiyi-appid";

	/**
	 * 用户版appId前缀
	 */
	public static final String APPID_PATIENT = "p_";

	/**
	 * 医生版appId前缀
	 */
	public static final String APPID_DOCTOR = "d_";

	/**
	 * 版本
	 */
	public static final String HEADER_VERSION = "version";

	public static final String HEADER_WEIYI_VERSION = "weiyi-version";

	public static final String HEADER_VERSION_TYPE = "version-type";

	public static final String VERSION_TYPE_PATIENT = "patient";

	public static final String VERSION_TYPE_DOCTOR = "doctor";

	/**
	 * User-Agent
	 */
	public static final String HEADER_USER_AGENT = "User-Agent";

	public static final String UA_ANDROID = "android";

	public static final String UA_IOS = "iphone";

	/**
	 * Injection Header
	 */
	public static final String HEADER_SOURCE_ID = "source-id";

	public static final String HEADER_SMS_SIGNATURE = "sms-signature";

	public static final String DEFAULT_SOURCE_ID = "32";

	public static final String DEFAULT_SMS_SIGNATURE = "微医";

	/**
	 * Auth Token
	 */
	public static final String HEADER_SIGN = "sign";

	public static final String HEADER_WEIYI_AUTHTOKEN = "weiyi-authtoken";

	public static final String HEADER_AUTHTOKEN = "authtoken";

	public static final String HEADER_AUTH = "authentication";

	/**
	 * Sign
	 */
	public static final String HEADER_PHONE_MODEL = "phone-model";

	public static final String HEADER_OS_VERSION = "os-version";

	public static final String HEADER_OS_TOKEN_ID = "os-token-id";

	public static final String HEADER_REQ_UUID = "req-uuid";

	/**
	 * CORS
	 */
	public static final String HEADER_SPAN_ID_NAME = "X-B3-SpanId";

	public static final String HEADER_PARENT_SPAN_ID_NAME = "X-B3-ParentSpanId";

	public static final String HEADER_SAMPLED_NAME = "X-B3-Sampled";

	public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";

	public static final String HEADER_TONGDUN_FP = "tongdun-fp";

	public static final String HEADER_X_GT_B_BIZ_SOURCE_ID = "x-gt-b-biz-source-id";

	public static final String HEADER_X_GT_B_BIZ_PARENT_SOURCE_ID = "x-gt-b-biz-parent-source-id";

	public static final String HEADER_AB_BARREL_LD = "ab-barrel-ld";

	public static final String HEADER_MONITOR_TAG = "monitor-tag";

	public static final String HEADER_X_GT_B_FLOWID = "x-gt-b-flowId";

	public static final String HEADER_X_FP_CODE = "x-fp-code";

	public static final String HEADER_X_GT_B_PAGEID = "x-gt-b-pageId";

	/**
	 * Channel Attribute Key
	 */
	public static final AttributeKey<String> APPID_KEY = AttributeKey.valueOf("appId");

	public static final AttributeKey<String> VERSION_KEY = AttributeKey
			.valueOf("version");

	public static final AttributeKey<Float> VERSION_NUM_KEY = AttributeKey
			.valueOf("versionNum");

	public static final AttributeKey<Boolean> WHITELIST_KEY = AttributeKey
			.valueOf("whitelist");

	public static final AttributeKey<Boolean> WEBH5CLIENT_KEY = AttributeKey
			.valueOf("webH5client");

	public static final AttributeKey<Boolean> MOBILECLIENT_KEY = AttributeKey
			.valueOf("mobileclient");

	public static final AttributeKey<VersionEntity> VERSION_ENTITY_KEY = AttributeKey
			.valueOf("version_entity");

}
