package com.roscap.mw.registry.client;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.roscap.mw.registry.ControlImperative;
import com.roscap.mw.registry.ServiceRegistry;
import com.roscap.mw.transport.endpoint.TransportEndpoint;

/**
 * simple static listener that receives
 * control imperatives on shared topic
 * 
 * @author is.zharinov
 *
 */
public class ServiceControlListener implements TransportEndpoint<ControlImperative> {
	private static final Logger logger = LoggerFactory.getLogger(ServiceControlListener.class);

	//we can't use clientId here, as it's really a separate listener
	private final UUID listenerId = UUID.randomUUID();

	@Autowired
	private ServiceInstanceFactory serviceFactory;
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportEndpoint#receive(java.lang.String, java.io.Serializable)
	 */
	@Override
	public void receive(String fromDestination, ControlImperative imperative) {
		logger.info("received control imperative: " + imperative);
		
		switch (imperative) {
		case KEEP_ALIVE:
			serviceFactory.keepAlive(); break;
		case REFRESH:
			serviceFactory.register(); break;
		default:
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportEndpoint#destination()
	 */
	@Override
	public String destination() {
		return ServiceRegistry.INFRASTRUCTURE_CHANNEL;
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportEndpoint#id()
	 */
	@Override
	public UUID id() {
		return listenerId;
	}
}
