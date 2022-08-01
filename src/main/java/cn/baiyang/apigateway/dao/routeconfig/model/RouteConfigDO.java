package cn.baiyang.apigateway.dao.routeconfig.model;

import java.util.Date;

public class RouteConfigDO {

	private int idc;

	private int enableEureka;

	private int stripPrefix;

	private int sort;

	private String path;

	private String serviceId;

	private String server;

	private Date gmtCreated;

	private Date gmtModified;

	private Integer isDeleted;

	/**
	 * 冗余数据
	 */
	private String ext;

	public int getIdc() {
		return idc;
	}

	public void setIdc(int idc) {
		this.idc = idc;
	}

	public int getEnableEureka() {
		return enableEureka;
	}

	public void setEnableEureka(int enableEureka) {
		this.enableEureka = enableEureka;
	}

	public int getStripPrefix() {
		return stripPrefix;
	}

	public void setStripPrefix(int stripPrefix) {
		this.stripPrefix = stripPrefix;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

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

	public Date getGmtCreated() {
		return gmtCreated;
	}

	public void setGmtCreated(Date gmtCreated) {
		this.gmtCreated = gmtCreated;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}

	public Integer getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Integer isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

}
