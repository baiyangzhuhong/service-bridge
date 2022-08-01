package cn.baiyang.apigateway.util;

import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class LogUtils {

	private static final Logger ACCESS_LOGGER = LoggerFactory.getLogger("ACCESS_LOGGER");

	private static final Logger CLIENT_LOGGER = LoggerFactory.getLogger("CLIENT_LOGGER");

	private static final Logger SYS_LOGGER = LoggerFactory.getLogger("SYS_LOGGER");

	public static void systemError(String msg, Throwable t) {
		SYS_LOGGER.error("system error occurred, error={}", msg, t);
	}

	public static void timeout(String traceId, String errorMsg, String uri, Throwable t) {
		SYS_LOGGER.error("traceId={}, timeout, errorMsg={}, uri={}", traceId, errorMsg,
				uri, t);
	}

	public static void exceptionGot(String traceId, HttpRequest request, Throwable t) {
		SYS_LOGGER.error("traceId={}, got exception while working, uri={}, request={}",
				traceId, request.uri(), formatRequest(request), t);
	}

	public static void failedRoute(String traceId, String uri, String serviceId,
			String message, Throwable t) {
		SYS_LOGGER.error("traceId={}, failed to route, uri={}, error={}, serviceId={}",
				traceId, uri, message, serviceId, t);
	}

	public static void serverProxyException(String traceId, String uri,
			SocketAddress localAddress, SocketAddress remoteAddress, Throwable t) {
		CLIENT_LOGGER.error(
				"traceId={}, server proxy invocation failed, uri={}, localAddress={}, remoteAddress={}",
				traceId, uri, localAddress, remoteAddress, t);
	}

	public static void clientChannelInactive(String traceId) {
		CLIENT_LOGGER.error("traceId={}, remote server channel is inactive", traceId);
	}

	public static void failedProxyOutput(String traceId, SocketAddress localAddress,
			SocketAddress remoteAddress, String uri, long connectElapsed,
			long handleElapsed, long outputElapsed, Throwable t) {
		ACCESS_LOGGER.error(
				"traceId={}, proxy failed to output from={} for server={}, uri={}, proxyElapsed={}",
				traceId, localAddress, remoteAddress, uri,
				getProxyElapsed(connectElapsed, handleElapsed, outputElapsed), t);
	}

	public static void notOkReturned(String traceId, SocketAddress localAddress,
			SocketAddress remoteAddress, String uri, int statusCode) {
		ACCESS_LOGGER.warn(
				"traceId={}, not ok return from={} to server={}, uri={}, statusCode={}",
				traceId, localAddress, remoteAddress, uri, statusCode);
	}

	public static void accessTimeout(String traceId, SocketAddress localAddress,
			SocketAddress remoteAddress, String uri, long connectElapsed,
			long handleElapsed, long outputElapsed) {
		ACCESS_LOGGER.warn(
				"traceId={}, access timeout from={} to server={}, uri={}, proxyElapsed={}",
				traceId, localAddress, remoteAddress, uri,
				getProxyElapsed(connectElapsed, handleElapsed, outputElapsed));
	}

	public static void originalChannelEmpty(String uri, SocketAddress localAddress,
			SocketAddress remoteAddress, HttpRequest request, HttpResponse response) {
		SYS_LOGGER.error(
				"uri={}, system errored as original channel is empty, from={} to server={}, request={}, response={}",
				uri, localAddress, remoteAddress, formatRequest(request),
				formatResponse(response));
	}

	public static void access(String traceId, SocketAddress localAddress,
			SocketAddress remoteAddress, HttpRequest request, HttpResponse response) {
		ACCESS_LOGGER.info(
				"traceId={}, access from={} to server={}, request={}, response={}",
				traceId, localAddress, remoteAddress, formatRequest(request),
				formatResponse(response));
	}

	private static String getProxyElapsed(long connectElapsed, long handleElapsed,
			long outputElapsed) {
		StringBuilder sb = new StringBuilder();
		sb.append("{connect(ms):").append(connectElapsed).append(", handled(ms):")
				.append(handleElapsed).append(", output(ms):").append(outputElapsed)
				.append("}");
		return sb.toString();
	}

	private static String formatRequest(HttpRequest request) {
		if (null == request) {
			return "{request:null}";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("{uri:").append(request.uri()).append(", headers:{");
		request.headers().forEach(e -> {
			sb.append(e.getKey()).append(":").append(e.getValue()).append(",");
		});
		sb.append("}}");
		return sb.toString();
	}

	private static String formatResponse(HttpResponse response) {
		if (null == response) {
			return "{response:null}";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("{statusCode:").append(response.status().code()).append(", headers:{");
		response.headers().forEach(e -> {
			sb.append(e.getKey()).append(":").append(e.getValue()).append(",");
		});
		sb.append("}}");
		return sb.toString();
	}

}
