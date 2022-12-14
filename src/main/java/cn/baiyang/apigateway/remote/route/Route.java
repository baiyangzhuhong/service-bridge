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
			throw new RouteException("", "?????????????????????");
		}

		stripPrefix(request, routeEntity);

		if (!routeEntity.getEnableEureka()) { // ??????
			return routeEntity.getServer();
		}

		String routeTag = headers.get(SysConstant.X_GLOBAL_ROUTE_TAG);
		String serviceId = routeEntity.getServiceId();
		ServiceInstance serviceInstance = loadBalancer.choose(serviceId + ":" + idc);
		// ??????????????????????????????????????????
		if (serviceInstance == null) {
			serviceInstance = loadBalancer.choose(serviceId);
		}
		if(serviceInstance == null) {
			LOGGER.error("????????????eureka?????????uri={}, serviceId={}", request.uri(), serviceId);
			throw new RouteException(serviceId, "????????????eureka??????");
		}

		return serviceInstance.getUri().toString();
	}

	private void stripPrefix(FullHttpRequest request, RouteEntity routeEntity) {
		int stripPrefix = routeEntity.getStripPrefix();
		if (stripPrefix == 0) { // ??????uri???????????????
			return;
		}
		String reqUri = request.uri();
		int prefixBegin = reqUri.indexOf("/");
		if (prefixBegin == -1) {
			return;
		}
		// stripPrefix Zuul ??????path?????????????????????
		if (stripPrefix == 1) {
			String remained = reqUri.substring(prefixBegin + 1);
			int prefixEnd = remained.indexOf("/");
			if (prefixEnd == -1) {
				request.setUri("/");
				return;
			}
			request.setUri(remained.substring(prefixEnd));
		}

		// stripPrefix AI_team ??????path??? /demo/demo2/**???**???????????????path??????
		if (stripPrefix == 2) {
			String pathTemplate = routeEntity.getPath();
			int indexBegin = pathTemplate.indexOf("/**");
			request.setUri(reqUri.substring(indexBegin));
		}
	}

	private RouteEntity getRouteNode(String uri, int idc) {
		Set<RouteEntity> routeEntities = dbComponent.getRouteEntities();
		// ??????uri????????????????????????
		for (RouteEntity routeEntity : routeEntities) {
			// ?????????????????????URL???IDC????????????
			if (PATH_MATCHER.match(routeEntity.getPath(), uri)
					&& idc == routeEntity.getIdc()) {
				return routeEntity;
			}
		}
		// ??????????????????????????????IDC
		final boolean enableDefaultRouting = defaultRoutingProperties.isEnable();
		final int defaultIdc = defaultRoutingProperties.getIdc(); // ???????????????????????????
		// ????????????????????????
		if (enableDefaultRouting && defaultIdc != idc) {
			for (RouteEntity routeEntity : routeEntities) {
				// ?????????????????????URL???IDC????????????
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
