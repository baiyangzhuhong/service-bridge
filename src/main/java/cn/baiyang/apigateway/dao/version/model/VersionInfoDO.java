package cn.baiyang.apigateway.dao.version.model;

import java.util.Date;

public class VersionInfoDO {

	private Long id;

	private Long versionClientId;

	/** 版本号，比较版本大小用 */
	private String versionNumber;

	/** 状态：0待发布 1已发布 2废弃 */
	private Integer status;

	/** 签名版本 */
	private Integer signVersion;

	private Date gmtCreated;

	private Date gmtModified;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersionClientId() {
		return versionClientId;
	}

	public void setVersionClientId(Long versionClientId) {
		this.versionClientId = versionClientId;
	}

	public String getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(String versionNumber) {
		this.versionNumber = versionNumber;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getSignVersion() {
		return signVersion;
	}

	public void setSignVersion(Integer signVersion) {
		this.signVersion = signVersion;
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
