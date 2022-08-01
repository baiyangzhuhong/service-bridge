package cn.baiyang.apigateway.dao.redirectconfig;

import cn.baiyang.apigateway.dao.redirectconfig.model.RedirectConfigDO;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RedirectConfigMapper {

	/**
	 * 查询所有需要重定向的链接配置
	 * @return List
	 */
	List<RedirectConfigDO> selectAll();

}
