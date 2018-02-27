package com.roscap.test.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.roscap.test.TestService;
import com.roscap.test.TestServiceImpl;

@Configuration
public class ServiceTestContext2 {
	@Bean
	public TestService testService() {
		return new TestServiceImpl();
	}
}
