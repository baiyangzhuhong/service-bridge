package cn.baiyang.apigateway.dao.version;

import cn.baiyang.apigateway.dao.version.model.VersionKeyConfigDO;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VersionKeyConfigMapper {

	/**
	 * 获取版本key配置列表
	 * @return
	 */
	List<VersionKeyConfigDO> listVersionKeyConfig();

}
