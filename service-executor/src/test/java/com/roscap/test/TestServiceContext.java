package com.roscap.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.roscap.mw.service.annotation.ConfigureServices;

@Configuration
@ConfigureServices(bootstrap="ngwee:9092")
public class TestServiceContext {
	@Bean
	public TestService testService() {
		return new TestServiceImpl();
	}
}
