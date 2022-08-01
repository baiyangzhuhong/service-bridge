package cn.baiyang.apigateway.dao.component.vo;

public class VersionEntity {

	/** version_client表主键 */
	private Long versionClientId;

	private String appId;

	/** 来源Id */
	private String sourceId;

	/** 最低版本 */
	private float lowestVersion;

	/** 短信签名 */
	private String smsSignature;

	private Integer signVersion;

	public Long getVersionClientId() {
		return versionClientId;
	}

	public void setVersionClientId(Long versionClientId) {
		this.versionClientId = versionClientId;
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

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public float getLowestVersion() {
		return lowestVersion;
	}

	public void setLowestVersion(float lowestVersion) {
		this.lowestVersion = lowestVersion;
	}

	public Integer getSignVersion() {
		return signVersion;
	}

	public void setSignVersion(Integer signVersion) {
		this.signVersion = signVersion;
	}

}
