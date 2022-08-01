package cn.baiyang.apigateway.common;

import org.springframework.context.ApplicationEvent;

public class DbInitEvent extends ApplicationEvent {

	/**
	 * Create a new ApplicationEvent.
	 * @param source the object on which the event initially occurred (never {@code null})
	 */
	public DbInitEvent(Object source) {
		super(source);
	}

}
