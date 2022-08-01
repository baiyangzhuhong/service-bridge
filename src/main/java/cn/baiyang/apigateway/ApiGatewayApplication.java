package cn.baiyang.apigateway;

import cn.baiyang.apigateway.config.ApiGatewayProperties;
import cn.baiyang.apigateway.config.DefaultRoutingProperties;
import cn.baiyang.apigateway.constant.SysConstant;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties({ ApiGatewayProperties.class, DefaultRoutingProperties.class })
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ApiGatewayApplication.class);
		Map<String, Object> defaultProperties = new HashMap<>(1 << 4);
		defaultProperties.put("spring.application.name", "${apigateway.name}");

		defaultProperties.put("management.security.enabled", "false");
		defaultProperties.put("management.contextPath",
				SysConstant.MANAGEMENT_CONTEXT_PATH);
		defaultProperties.put("management.health.redis.enabled", "false");
		defaultProperties.put("endpoints.health.sensitive", "false");
		defaultProperties.put("spring.jackson.serialization.indent_output", "true");

		defaultProperties.put("eureka.instance.preferIpAddress", "true");
		defaultProperties.put("eureka.instance.instance-id",
				"${spring.cloud.client.ipAddress}:${apigateway.port}");
		defaultProperties.put("eureka.instance.healthCheckUrlPath",
				SysConstant.MANAGEMENT_CONTEXT_PATH + "/health");
		defaultProperties.put("eureka.instance.statusPageUrlPath",
				SysConstant.MANAGEMENT_CONTEXT_PATH + "/info");
		defaultProperties.put("eureka.instance.metadata-map.management.context-path",
				SysConstant.MANAGEMENT_CONTEXT_PATH);

		defaultProperties.put("eureka.client.registry-fetch-interval-seconds", "5");

		defaultProperties.put("spring.cloud.inetutils.ignored-interfaces[0]", "v.*");
		defaultProperties.put("spring.cloud.inetutils.ignored-interfaces[1]", "lo.*");
		app.setDefaultProperties(defaultProperties);
		app.run(args);
	}

	@RestController
	public static class EchoTest {

		@GetMapping("/")
		public String echo() {
			return "{\"code\":0,\"message\":\"success\",\"data\":\"ApiGatewayApplication\"}";
		}

		@GetMapping("/api-gateway-web/healthy/check")
		public String healthyCheck() {
			return "{\"code\":0,\"message\":\"success\",\"data\":\"ApiGatewayApplication healthyCheck ok\"}";
		}

	}

}
