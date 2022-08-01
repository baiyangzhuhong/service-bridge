package cn.baiyang.apigateway.init.discovery;

import cn.baiyang.apigateway.dao.routeconfig.model.RouteConfigDO;

import java.util.Observable;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RouteEntityObservable extends Observable {

	@Autowired
	private NacosListenerInit nacosListenerInit;

	@PostConstruct
	public void init() {
		this.addObserver(nacosListenerInit);

	}

	public void routeChanged(RouteConfigDO routeConfigDO) {
		super.setChanged();
		super.notifyObservers(routeConfigDO);
	}

}
