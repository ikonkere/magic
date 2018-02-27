package com.roscap.mw.registry.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.roscap.mw.registry.ServiceRegistry;
import com.roscap.mw.registry.ServiceRegistryImpl;
import com.roscap.mw.registry.management.PolicyEnum;
import com.roscap.mw.registry.management.StorageManager;
import com.roscap.mw.remoting.config.EnableMagic;
import com.roscap.mw.transport.TransportAdapter;
import com.roscap.mw.transport.TransportFactory;

/**
 * Service Registry context. It is a limited-CDM service
 * and therefore uses @EnableMagic directly
 * 
 * @author is.zharinov
 *
 */
@Configuration
@EnableScheduling
@EnableMagic
public class ServiceRegistryContext {
	@Bean
	public ServiceRegistry serviceRegistry(@Value("${management.policy:EXCLUSIVE}") PolicyEnum policy,
			@Value("${client.timeout:1800}") long timeout, @Value("${client.cache:true}") boolean cacheClients,
			@Autowired(required=false) StorageManager sm) {
		ServiceRegistryImpl sr = new ServiceRegistryImpl();
		sr.setStorageManager(sm);
		sr.setPolicy(policy);
		sr.setClientStaleTimeout(timeout);
		sr.setCacheClients(cacheClients);
		return sr;
	}
	
	@Bean
	public BeanFactoryPostProcessor transportConfigurer() {
		PropertySourcesPlaceholderConfigurer pc = new PropertySourcesPlaceholderConfigurer();
		pc.setLocation(new ClassPathResource("service-registry.properties"));
		pc.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return pc;
	}

	@Bean
	public TransportAdapter transportAdapter(@Value("${bootstrap.address}") String bootstrapAddress) {
		TransportAdapter ta = TransportFactory.getTransport();
		ta.initialize(bootstrapAddress);
		return ta;
	}
}
