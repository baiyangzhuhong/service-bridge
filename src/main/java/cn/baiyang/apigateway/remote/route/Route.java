package cn.baiyang.apigateway.remote.route;

import cn.baiyang.apigateway.config.ApiGatewayProperties;
import cn.baiyang.apigateway.config.DefaultRoutingProperties;
import cn.baiyang.apigateway.constant.SysConstant;
import cn.baiyang.apigateway.dao.component.DbComponent;
import cn.baiyang.apigateway.dao.component.vo.RouteEntity;
import cn.baiyang.apigateway.exception.RouteException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Set;

@Component
public class Route {

	private static final Logger LOGGER = LoggerFactory.getLogger(Route.class);

	private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

	@Resource
	private DbComponent dbComponent;

	@Resource
	private ApiGatewayProperties apiGatewayProperties;

	@Resource
	private DefaultRoutingProperties defaultRoutingProperties;

	@Resource
	private LoadBalancerClient loadBalancer;

	@Value("${pre.route.tag}")
	private String defaultPreRouteTag;

	public String getRouteServer(FullHttpRequest request) throws Exception {
		Integer idc = apiGatewayProperties.getIdc();
		final boolean isProdEnv = isProdEnv(idc);
		String reqPath = new URI(request.uri()).getPath();
		HttpHeaders headers = request.headers();
		String host = headers.get(HttpHeaderNames.HOST);
		host = null == host ? "" : host;

		if (!isProdEnv) {
			idc = getIDC(host, idc, isProdEnv);
		}
		RouteEntity routeEntity = this.getRouteNode(reqPath, idc);
		if (routeEntity == null) {
			throw new RouteException("", "无路由配置信息");
		}

		stripPrefix(request, routeEntity);

		if (!routeEntity.getEnableEureka()) { // 直连
			return routeEntity.getServer();
		}

		String routeTag = headers.get(SysConstant.X_GLOBAL_ROUTE_TAG);
		String serviceId = routeEntity.getServiceId();
		ServiceInstance serviceInstance = loadBalancer.choose(serviceId + ":" + idc);
		// 不存在隔离服务，使用通用服务
		if (serviceInstance == null) {
			serviceInstance = loadBalancer.choose(serviceId);
		}
		if(serviceInstance == null) {
			LOGGER.error("没获取到eureka节点，uri={}, serviceId={}", request.uri(), serviceId);
			throw new RouteException(serviceId, "没获取到eureka节点");
		}

		return serviceInstance.getUri().toString();
	}

	private void stripPrefix(FullHttpRequest request, RouteEntity routeEntity) {
		int stripPrefix = routeEntity.getStripPrefix();
		if (stripPrefix == 0) { // 不对uri做任何处理
			return;
		}
		String reqUri = request.uri();
		int prefixBegin = reqUri.indexOf("/");
		if (prefixBegin == -1) {
			return;
		}
		// stripPrefix Zuul 过滤path中第一个字符串
		if (stripPrefix == 1) {
			String remained = reqUri.substring(prefixBegin + 1);
			int prefixEnd = remained.indexOf("/");
			if (prefixEnd == -1) {
				request.setUri("/");
				return;
			}
			request.setUri(remained.substring(prefixEnd));
		}

		// stripPrefix AI_team 过滤path中 /demo/demo2/**中**之前所有的path路径
		if (stripPrefix == 2) {
			String pathTemplate = routeEntity.getPath();
			int indexBegin = pathTemplate.indexOf("/**");
			request.setUri(reqUri.substring(indexBegin));
		}
	}

	private RouteEntity getRouteNode(String uri, int idc) {
		Set<RouteEntity> routeEntities = dbComponent.getRouteEntities();
		// 根据uri返回同机房下配置
		for (RouteEntity routeEntity : routeEntities) {
			// 精准匹配，根据URL和IDC共同决定
			if (PATH_MATCHER.match(routeEntity.getPath(), uri)
					&& idc == routeEntity.getIdc()) {
				return routeEntity;
			}
		}
		// 测试环境下可使用默认IDC
		final boolean enableDefaultRouting = defaultRoutingProperties.isEnable();
		final int defaultIdc = defaultRoutingProperties.getIdc(); // 默认路由使用的机房
		// 是否开启默认路由
		if (enableDefaultRouting && defaultIdc != idc) {
			for (RouteEntity routeEntity : routeEntities) {
				// 精准匹配，根据URL和IDC共同决定
				if (PATH_MATCHER.match(routeEntity.getPath(), uri)
						&& defaultIdc == routeEntity.getIdc()) {
					return routeEntity;
				}
			}
		}
		return null;
	}

	private boolean isProdEnv(Integer idc) {
		if (idc == null) {
			return false;
		}
		return idc == SysConstant.IDC_XINGYI || idc == SysConstant.IDC_BINAN
				|| idc == SysConstant.IDC_TENXUNYUN;
	}

	private Integer getIDC(String host, Integer idc, boolean isProdEnv) {
		if (StringUtils.isBlank(host) || !host.contains("-") || isProdEnv) {
			return idc;
		}

		String idcStr = host.substring(0, host.indexOf("-"));
		if (StringUtils.isBlank(idcStr) || !StringUtils.isNumeric(idcStr)) {
			return idc;
		}

		return Integer.valueOf(idcStr);
	}

}
