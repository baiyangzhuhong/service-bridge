package cn.baiyang.apigateway.dao.component.vo;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

@Validated
public class RouteEntity {

	@NotBlank
	private int idc;

	@NotBlank
	private String path;

	@NotBlank
	private String serviceId;

	private String server;

	private Boolean enableEureka = true;

	// 0 否 1 Zuul 2 AI_Team
	private int stripPrefix = 0;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public Boolean getEnableEureka() {
		return enableEureka;
	}

	public void setEnableEureka(Boolean enableEureka) {
		this.enableEureka = enableEureka;
	}

	public int getStripPrefix() {
		return stripPrefix;
	}

	public void setStripPrefix(int stripPrefix) {
		this.stripPrefix = stripPrefix;
	}

	public int getIdc() {
		return idc;
	}

	public void setIdc(int idc) {
		this.idc = idc;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idc, path);
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}

		if (!(anObject instanceof RouteEntity)) {
			return false;
		}

		RouteEntity anotherRouteEntity = (RouteEntity) anObject;
		return anotherRouteEntity.getIdc() == getIdc()
				&& StringUtils.equals(anotherRouteEntity.getPath(), getPath());
	}

}
