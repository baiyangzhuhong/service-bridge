package cn.baiyang.apigateway.util;

import java.net.InetSocketAddress;

import org.apache.commons.lang.StringUtils;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import sun.net.util.IPAddressUtil;

public class HttpUtils {

	public static boolean isClose(HttpRequest request) {
		if (request == null) {
			return true;
		}

		if (request.headers().contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE,
				true) || request.protocolVersion().equals(HttpVersion.HTTP_1_0)) {
			return true;
		}

		return false;
	}

	public static InetSocketAddress getSocketAddressFromHost(String host) {
		if (StringUtils.isBlank(host)) {
			throw new IllegalArgumentException("empty host.");
		}
		int port = host.contains("https://") ? 443 : 80;
		String[] hostInfo = host.replace("http://", "").replace("https://", "")
				.split(":");

		if (hostInfo.length == 1) {
			if (IPAddressUtil.isIPv4LiteralAddress(hostInfo[0])) {
				throw new IllegalArgumentException("port undefined");
			}
			else {
				return new InetSocketAddress(hostInfo[0], port);
			}
		}

		if (!StringUtils.isNumeric(hostInfo[1])) {
			throw new IllegalArgumentException("invalid host: " + host);
		}

		return new InetSocketAddress(hostInfo[0], Integer.valueOf(hostInfo[1]));
	}

	public static String getHostName(String host) {
		if (StringUtils.isBlank(host)) {
			throw new IllegalArgumentException("empty host.");
		}

		String[] hostInfo = host.replace("http://", "").replace("https://", "")
				.split(":");

		if (hostInfo.length < 1) {
			throw new IllegalArgumentException("invalid host");
		}

		return hostInfo[0];
	}

}
