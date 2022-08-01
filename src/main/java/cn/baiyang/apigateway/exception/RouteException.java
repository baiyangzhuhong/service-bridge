package cn.baiyang.apigateway.exception;

public class RouteException extends RuntimeException {

	private String serviceId;

	public RouteException(String serviceId, String message) {
		super(message);
		this.serviceId = serviceId;
	}

	public String getServiceId() {
		return serviceId;
	}

}
