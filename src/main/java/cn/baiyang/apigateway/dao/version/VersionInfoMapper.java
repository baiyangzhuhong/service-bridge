package cn.baiyang.apigateway.dao.version;

import cn.baiyang.apigateway.common.PageQuery;
import cn.baiyang.apigateway.dao.version.model.VersionInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VersionInfoMapper {

	/**
	 * 获取版本信息
	 * @param param
	 * @return
	 */
	List<VersionInfoDO> listVersionInfo(PageQuery param);

}
