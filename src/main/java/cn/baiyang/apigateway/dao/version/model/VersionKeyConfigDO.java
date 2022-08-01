package cn.baiyang.apigateway.dao.version.model;

import java.util.Date;

public class VersionKeyConfigDO {

	/**
	 * 自增ID
	 */
	private Long id;

	/**
	 * app标识
	 */
	private String appId;

	/**
	 * signKey_store表主键
	 */
	private Integer signKeyStoreId;

	/**
	 * 内部版本号
	 */
	private String versionCode;

	/**
	 * 版本名称
	 */
	private String versionName;

	/**
	 * 是否删除
	 */
	private Integer isDeleted;

	/**
	 * 创建时间
	 */
	private Date gmtCreated;

	/**
	 * 修改时间
	 */
	private Date gmtModified;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public Integer getSignKeyStoreId() {
		return signKeyStoreId;
	}

	public void setSignKeyStoreId(Integer signKeyStoreId) {
		this.signKeyStoreId = signKeyStoreId;
	}

	public String getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(String versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public Integer getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Integer isDeleted) {
		this.isDeleted = isDeleted;
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
