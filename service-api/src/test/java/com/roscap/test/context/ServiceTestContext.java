package com.roscap.test.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.roscap.mw.service.annotation.ConfigureServices;
import com.roscap.test.TestService;
import com.roscap.test.TestServiceImpl;

@Configuration
@ConfigureServices(bootstrap="ngwee:9092")
public class ServiceTestContext {
	@Bean
	public TestService testService() {
		return new TestServiceImpl();
	}
}
