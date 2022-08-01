package cn.baiyang.apigateway.dao.version.model;

import java.util.Date;

public class VersionDO {

	/**
	 * version_client表主键
	 */
	private Long versionClientId;

	/**
	 * 标识
	 */
	private String appId;

	/**
	 * 操作系统（1：安卓，2：IOS）
	 */
	private int appOs;

	/**
	 * app版本类型 1:用户版 2:医生版
	 */
	private int appType;

	/**
	 * app名称
	 */
	private String appName;

	/**
	 * 最低版本号
	 */
	private String lowestVersion;

	/**
	 * 创建时间
	 */
	private Date gmtCreated;

	/**
	 * 修改时间
	 */
	private Date gmtModified;

	/**
	 * 来源id标识
	 */
	private String sourceId;

	/**
	 * 短信签名
	 */
	private String smsSignature;

	private String rasPrivateKey;

	private String rasPublicKey;

	private Integer signVersion;

	public Long getVersionClientId() {
		return versionClientId;
	}

	public void setVersionClientId(Long versionClientId) {
		this.versionClientId = versionClientId;
	}

	public String getRasPrivateKey() {
		return rasPrivateKey;
	}

	public void setRasPrivateKey(String rasPrivateKey) {
		this.rasPrivateKey = rasPrivateKey;
	}

	public String getRasPublicKey() {
		return rasPublicKey;
	}

	public void setRasPublicKey(String rasPublicKey) {
		this.rasPublicKey = rasPublicKey;
	}

	public String getSmsSignature() {
		return smsSignature;
	}

	public void setSmsSignature(String smsSignature) {
		this.smsSignature = smsSignature;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
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

	public int getAppOs() {
		return appOs;
	}

	public void setAppOs(int appOs) {
		this.appOs = appOs;
	}

	public int getAppType() {
		return appType;
	}

	public void setAppType(int appType) {
		this.appType = appType;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getLowestVersion() {
		return lowestVersion;
	}

	public void setLowestVersion(String lowestVersion) {
		this.lowestVersion = lowestVersion;
	}

	public Integer getSignVersion() {
		return signVersion;
	}

	public void setSignVersion(Integer signVersion) {
		this.signVersion = signVersion;
	}

}
