package com.roscap.context.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.roscap.mw.transport.TransportAdapter;
import com.roscap.mw.transport.TransportFactory;

@Configuration
public class TransportTestContext {
	@Bean
	public TransportAdapter transportAdapter() {
		TransportAdapter ta = TransportFactory.getTransport();
		ta.initialize("ngwee:9092");
		return ta;
	}
}
