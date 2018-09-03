package com.roscap.mw.registry.client.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.roscap.mw.registry.ServiceRegistry;
import com.roscap.mw.registry.client.ServiceControlListener;
import com.roscap.mw.registry.client.ServiceInstanceFactory;
import com.roscap.mw.remoting.client.RemoteServiceInvoker;
import com.roscap.mw.remoting.client.RemoteServiceProxyFactoryBean;
import com.roscap.mw.transport.TransportAdapter;

/**
 * service registry client context
 * 
 * @author is.zharinov
 *
 */
@Configuration
public class ServiceRegistryClientContext {
	/**
	 * infrastructure message listener
	 * 
	 * @return
	 */
	@Bean
	public ServiceControlListener serviceControlListener() {
		return new ServiceControlListener();
	}
	
	/**
	 * central entity that manages all necessary operations
	 * service instances need
	 * 
	 * @return
	 */
	@Bean
	public ServiceInstanceFactory serviceFactory() {
		return new ServiceInstanceFactory();
	}
	
	/**
	 * manual Service Registry proxy
	 * 
	 * @param arg0
	 * @return
	 */
	@Bean
	public RemoteServiceProxyFactoryBean ServiceRegistryClient(RemoteServiceInvoker arg0) {
		return new RemoteServiceProxyFactoryBean<>(arg0);
	}
	
	/**
	 * 
	 * @param arg0
	 * @return
	 */
	@Bean
	public RemoteServiceInvoker ServiceRegistryInvoker(TransportAdapter arg0) {
		RemoteServiceInvoker i = new RemoteServiceInvoker(ServiceRegistry.SID);
		i.setServiceInterface(ServiceRegistry.class);
		i.setTransportAdapter(arg0);
		return i;
	}	
}
