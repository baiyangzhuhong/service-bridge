package cn.baiyang.apigateway.dao.routeconfig;

import cn.baiyang.apigateway.dao.routeconfig.model.RouteConfigDO;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RouteConfigMapper {

	/**
	 * 加载所有路由配置
	 * @return 路由配置列表
	 */
	List<RouteConfigDO> listByPage(Date lastModifiedTime, int offset, int pageSize);

}
