package cn.baiyang.apigateway.dao.redirectconfig.model;

import java.util.Date;

public class RedirectConfigDO {

	/**
	 * 主键id
	 */
	private Long id;

	/**
	 * 原链接
	 */
	private String sourceUrl;

	/**
	 * 重定向目标链接
	 */
	private String targetUrl;

	/**
	 * 是否生效 0.不生效 1.生效
	 */
	private Integer isValid;

	/**
	 * 创建时间
	 */
	private Date gmtCreated;

	/**
	 * 最后修改时间
	 */
	private Date gmtModified;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public Integer getIsValid() {
		return isValid;
	}

	public void setIsValid(Integer isValid) {
		this.isValid = isValid;
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

}
