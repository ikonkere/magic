package com.roscap.mw.registry.client;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import com.roscap.cdm.api.ServiceInstance;
import com.roscap.mw.registry.ServiceRegistry;

/**
 * service registry client that provides basic functionality
 * with local services.
 * 
 * @author is.zharinov
 *
 */
public class ServiceInstanceFactory implements ApplicationContextAware, ApplicationListener<ApplicationContextEvent> {
	private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceFactory.class);

	@Autowired
//	@Qualifier("0a398127-f403-4cce-80e0-1c754901736c")
	@Qualifier("ServiceRegistryClient")
	private ServiceRegistry serviceRegistry;

	private ConfigurableApplicationContext ctx;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		ctx = (ConfigurableApplicationContext)applicationContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			register();
		}
	}
	
	/**
	 * returns real service object, not it's CDM instance
	 * 
	 * @param serviceUri
	 * @return
	 */
	public Object findByUri(URI serviceUri) {
		return findAll().stream().filter((e) -> e.serviceUri().equals(serviceUri)).findFirst().get().service();
	}

	/**
	 * all CDM instances created in context
	 * @return
	 */
	Set<ServiceInstance> findAll() {
		return new HashSet<>(ctx.getBeansOfType(ServiceInstance.class).values());
	}
	
	/**
	 * registers all local services with service registry
	 */
	public void register() {
		findAll().stream().forEach((e) -> {
				serviceRegistry.register(e.instanceId(), e.serviceSpec());
				logger.info("registering service: " + e);
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
