package com.roscap.mw.reflection;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import com.roscap.cdm.api.ServiceInstance;

/**
 * 
 * service instance helper that locates instances
 * within Spring context
 * 
 * @author is.zharinov
 *
 */
public class ServiceInstanceLocator implements ApplicationContextAware {
	protected ConfigurableApplicationContext ctx;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		ctx = (ConfigurableApplicationContext)applicationContext;
	}

	
	/**
	 * returns real service object, not it's CDM instance
	 * 
	 * @param serviceUri
	 * @return
	 */
	public Object findByUri(URI serviceUri) {
		return findAll().stream().filter((e) ->
			e.serviceUri().equals(serviceUri)).findFirst().get().service();
	}
	
	/**
	 * 
	 * @param serviceUri
	 * @param serviceClass
	 * @return
	 */
	public <T> T findByUri(URI serviceUri, Class<T> serviceClass) {
		return serviceClass.cast(findAll().stream().filter((e) ->
			e.serviceUri().equals(serviceUri) && serviceClass.isAssignableFrom(e.serviceClass())).findFirst().get().service());
	}

	/**
	 * all CDM instances created in context
	 * @return
	 */
	public Set<ServiceInstance> findAll() {
		return new HashSet<>(ctx.getBeansOfType(ServiceInstance.class).values());
	}
}
