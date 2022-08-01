package cn.baiyang.apigateway.context;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.FullHttpRequest;

public class RequestContext extends ConcurrentHashMap<String, Object> {

	private static final long serialVersionUID = -3536073785642080369L;

	private static final Logger logger = LoggerFactory.getLogger(RequestContext.class);

	private static Class<? extends RequestContext> contextClass = RequestContext.class;

	private static RequestContext currentContext = null;

	private static final ThreadLocal<? extends RequestContext> threadLocal = ThreadLocal
			.withInitial(() -> {
				try {
					return contextClass.newInstance();
				}
				catch (Throwable e) {
					logger.error(e.getMessage());
					throw new RuntimeException(e);
				}
			});

	public static RequestContext getCurrentContext() {
		if (currentContext != null) {
			return currentContext;
		}

		return threadLocal.get();
	}

	public RequestContext() {
		super();
	}

	public void setRequest(FullHttpRequest request) {
		put("request", request);
	}

	public FullHttpRequest getRequest() {
		return (FullHttpRequest) get("request");
	}

}
