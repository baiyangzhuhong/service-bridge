package cn.baiyang.apigateway.init.discovery;

import cn.baiyang.apigateway.dao.routeconfig.model.RouteConfigDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Observable;
import java.util.Observer;

@Component
public class NacosListenerInit implements Observer {

	public static final Logger logger = LoggerFactory.getLogger(NacosListenerInit.class);

	@Override
	public void update(Observable o, Object arg) {
		// TODO
	}

}
