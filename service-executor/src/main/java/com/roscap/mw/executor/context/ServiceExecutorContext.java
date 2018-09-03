package com.roscap.mw.executor.context;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.roscap.mw.executor.ServiceDiscoveryCallbackFactory;
import com.roscap.mw.executor.ServiceExecutor;
import com.roscap.mw.remoting.config.EnableMagicClient;

/**
 * Service executor context that is the main entry
 * point for Magic clients. No transport is specified here,
 * it should be externally configured
 * 
 * @author is.zharinov
 *
 */
@Configuration
@EnableMagicClient(packages="com.roscap")
public class ServiceExecutorContext {
	/**
	 * Dependent beans are autowired due to certain instantiation-time restrictions
	 * 
	 * @return
	 */
	@Bean
	public ServiceDiscoveryCallbackFactory callbackFactory() {
		return ServiceDiscoveryCallbackFactory.instance;
	}

	/**
	 * Service executor instance. Dependent beans are
	 * autowired due to certain instantiation-time restrictions
	 * 
	 * @return
	 */
	@Bean
	public ServiceExecutor serviceExecutor(@Value("${cache.service.instances:true}") boolean cacheInstances) {
		ServiceExecutor se = new ServiceExecutor();
		se.setCacheInstances(cacheInstances);
		return se;
	}
}
