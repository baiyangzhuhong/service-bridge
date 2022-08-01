package cn.baiyang.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apigateway.default.routing")
public class DefaultRoutingProperties {

	/**
	 * 是否开启默认路由配置
	 */
	private boolean enable = false;

	/**
	 * 默认路由使用的机房
	 */
	private int idc = 100;

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public int getIdc() {
		return idc;
	}

	public void setIdc(int idc) {
		this.idc = idc;
	}

}
