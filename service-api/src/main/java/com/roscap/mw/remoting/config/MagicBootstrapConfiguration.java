package com.roscap.mw.remoting.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.roscap.mw.remoting.spring.TransportEndpointBeanPostProcessor;
import com.roscap.mw.transport.TransportAdapter;

/**
 * Magic bootstrap that contains
 * everything necessary to enable Magic
 * 
 * @author is.zharinov
 *
 */
@Configuration
public class MagicBootstrapConfiguration {
	/**
	 * service instances discovery and processing
	 * 
	 * @return
	 */
	@Bean
	public BeanFactoryPostProcessor remoteServiceFactoryProcessor() {
		return new RemoteServiceFactoryPostProcessor();
	}
	
	/**
	 * endpoint auto-registration enablement
	 * 
	 * @param arg0
	 * @return
	 */
	@Bean
	public BeanPostProcessor transportEndpointProcessor(TransportAdapter arg0) {
		TransportEndpointBeanPostProcessor bpp = new TransportEndpointBeanPostProcessor();
		bpp.setTransportAdapter(arg0);
		return bpp;
	}
}
