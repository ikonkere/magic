package com.roscap.mw.registry.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import com.roscap.mw.reflection.ServiceInstanceLocator;
import com.roscap.mw.registry.ServiceRegistry;

/**
 * service registry client that provides basic functionality
 * with local services.
 * 
 * @author is.zharinov
 *
 */
public class ServiceInstanceFactory extends ServiceInstanceLocator implements ApplicationListener<ApplicationContextEvent> {
	private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceFactory.class);

	@Autowired
//	@Qualifier("0a398127-f403-4cce-80e0-1c754901736c")
	@Qualifier("ServiceRegistryClient")
	private ServiceRegistry serviceRegistry;
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		//we do it only for our own context and skip child refresh events
		if (event.getApplicationContext().equals(ctx)) {
			if (event instanceof ContextRefreshedEvent) {
				register();
			}
			else if (event instanceof ContextClosedEvent) {
				unregister();
			}
		}
	}
	
	/**
	 * registers all local services with service registry
	 */
	public void register() {
		findAll().stream().forEach((e) -> {
				serviceRegistry.register(e.instanceId(), e.serviceSpec());
				ctx.publishEvent(new ServiceInstanceEvent(e.instanceId()));
				logger.info("registering service: " + e);
			}
		);
	}

	/**
	 * unregisters all local services with service registry
	 */
	public void unregister() {
		findAll().stream().forEach((e) -> {
				serviceRegistry.unregister(e.instanceId());
//				ctx.publishEvent(new ServiceInstanceEvent(e.instanceId()));
				logger.info("unregistering service: " + e);
			}
		);
	}
	
	/**
	 * sends keep-alives for all available local services to service registry.
	 * this does assume <i>infrastructure</i> availability, as in:
	 * service is available if it is properly instantiated and present in
	 * application context
	 */
	public void keepAlive() {
		findAll().stream().forEach((e) -> {
				serviceRegistry.keepAlive(e.instanceId());
				logger.info("keep-alive for service: " + e);
			}
		);
	}
}
