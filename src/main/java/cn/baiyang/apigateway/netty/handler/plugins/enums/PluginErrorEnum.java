package cn.baiyang.apigateway.netty.handler.plugins.enums;

public enum PluginErrorEnum {

	APPID_EMPTY("-9", "appId为空"),
	APPID_WRONG("-9", "appId不正确"),
	APPID_UA_EMPTY("-9",  "User-Agent为空"),
	APPID_UA_WRONG("-9", "User-Agent不正确"),
	APPID_ILLEGAL("-9", "appId非法"),
	APPID_VERSION_LACK("-9", "appId的版本配置缺失"),

	SIGN_EMPTY("-5", "签名为空"),
	SIGN_WRONG("-5", "签名不正确"),
	SIGN_VERIFY_FAILED("-5", "签名出现错误"),

	VERSION_LOWER_ERR("-4", "您的版本过低，已经无法使用，请立即更新!"),
	VERSION_FORMAT_ERR("-4", "版本号数据格式错误");

	private String code;

	private String message;

	private String stage;

	PluginErrorEnum(String code, String message) {
		this.code = code;
		this.message = message;
		this.stage = "API-Gateway";
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public String getStage() {
		return stage;
	}

	@Override
	public String toString() {
		return String.format("{\"code\":\"%s\",\"message\":\"%s\",\"stage\":\"%s\"}",
				code, message, stage);
	}

}
