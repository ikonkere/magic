package com.roscap.mw.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.roscap.mw.transport.TransportAdapter;
import com.roscap.mw.transport.TransportFactory;

/**
 * transport configuration context
 * 
 * @author is.zharinov
 *
 */
@Configuration
public class TransportContext implements EnvironmentAware {
	private Environment env;

	@Value("${bootstrap.address}")
	private String bootstrapAddress;
	
	@Override
	public void setEnvironment(Environment environment) {
		env = environment;

		if (env.containsProperty("bootstrap.address") &&
				(this.bootstrapAddress == null || this.bootstrapAddress.isEmpty())) {
			bootstrapAddress = env.getProperty("bootstrap.address");
		}
	}

	/**
	 * transport adapter instance to use
	 * 
	 * @return
	 */
	@Bean
	public TransportAdapter transportAdapter() {
		TransportAdapter ta = TransportFactory.getTransport();
		ta.initialize(bootstrapAddress);
		return ta;
	}
}
