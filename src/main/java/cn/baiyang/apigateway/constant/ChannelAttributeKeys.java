package cn.baiyang.apigateway.constant;

import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPool;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;

public class ChannelAttributeKeys {

	/**
	 *
	 */
	public static final AttributeKey<Channel> ORIGINAL_CHANNEL_KEY = AttributeKey
			.valueOf("original-channel");

	public static final AttributeKey<ChannelPool> CHANNEL_POOL_KEY = AttributeKey
			.valueOf("channel-pool");

	public static final AttributeKey<FullHttpRequest> REQUEST_KEY = AttributeKey
			.valueOf("http-request");

	public static final AttributeKey<Long> PROXY_START_KEY = AttributeKey
			.valueOf("server-recv-time");

	public static final AttributeKey<Long> PROXY_CONNECTED_KEY = AttributeKey
			.valueOf("connected-time");

	public static final AttributeKey<String> TRACE_ID_KEY = AttributeKey
			.valueOf("trace-id");

}
