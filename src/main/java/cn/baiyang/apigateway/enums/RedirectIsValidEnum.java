package cn.baiyang.apigateway.enums;

public enum RedirectIsValidEnum {

	NO(0, "不生效"), YES(1, "生效");
	/**
	 * 是否生效
	 */
	private Integer isValid;

	/**
	 * 描述
	 */
	private String desc;

	RedirectIsValidEnum(Integer isValid, String desc) {
		this.isValid = isValid;
		this.desc = desc;
	}

	public Integer getIsValid() {
		return isValid;
	}

	public String getDesc() {
		return desc;
	}

}
