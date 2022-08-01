package cn.baiyang.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apigateway")
public class ApiGatewayProperties {

	private String name;

	private int port = 8080;

	private Integer idc = 0; // 机房,0兴议1滨安

	private HttpClient httpClient = new HttpClient();

	private Server server = new Server();

	// 版本信息内存更新检查频率，单位是毫秒
	private Integer versionConfigTime = 600000;

	private int workThreadsSize = 512;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getIdc() {
		return idc;
	}

	public void setIdc(Integer idc) {
		this.idc = idc;
	}

	public Integer getVersionConfigTime() {
		return versionConfigTime;
	}

	public void setVersionConfigTime(Integer versionConfigTime) {
		this.versionConfigTime = versionConfigTime;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getWorkThreadsSize() {
		return workThreadsSize;
	}

	public void setWorkThreadsSize(int workThreadsSize) {
		this.workThreadsSize = workThreadsSize;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public static class HttpClient {

		private int connectTimeout = 10_000;

		public int getConnectTimeout() {
			return connectTimeout;
		}

		public void setConnectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
		}

	}

	public static class Server {

		private int readTimeout = 60_000;

		private int writeTimeout = 60_000;

		public int getReadTimeout() {
			return readTimeout;
		}

		public void setReadTimeout(int readTimeout) {
			this.readTimeout = readTimeout;
		}

		public int getWriteTimeout() {
			return writeTimeout;
		}

		public void setWriteTimeout(int writeTimeout) {
			this.writeTimeout = writeTimeout;
		}

	}

}