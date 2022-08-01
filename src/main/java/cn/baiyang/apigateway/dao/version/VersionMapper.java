package cn.baiyang.apigateway.dao.version;

import cn.baiyang.apigateway.common.PageQuery;
import cn.baiyang.apigateway.dao.version.model.SignKeyDO;
import cn.baiyang.apigateway.dao.version.model.VersionDO;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VersionMapper {

	/** 获取所有版本数据 */
	List<VersionDO> listVersionInfo(PageQuery pageQuery);

	/**
	 * 获取签名列表
	 * @return 签名列表
	 */
	List<SignKeyDO> listSignKeys();

}
