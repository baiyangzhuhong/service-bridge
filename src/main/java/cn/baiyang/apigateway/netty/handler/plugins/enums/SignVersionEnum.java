package cn.baiyang.apigateway.netty.handler.plugins.enums;

public enum SignVersionEnum {

	ZERO(0, "body_token"), ONE(1, "body_header_uri");

	SignVersionEnum(Integer val, String desc) {
		this.val = val;
		this.desc = desc;
	}

	private Integer val;

	private String desc;

	public Integer getVal() {
		return val;
	}

	public String getDesc() {
		return desc;
	}

}
