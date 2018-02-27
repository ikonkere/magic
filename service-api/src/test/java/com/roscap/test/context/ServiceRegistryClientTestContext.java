package com.roscap.test.context;

import org.springframework.context.annotation.Configuration;

import com.roscap.mw.service.annotation.ConfigureServices;

@Configuration
@ConfigureServices(bootstrap="ngwee:9092", services=ServiceTestContext2.class)
public class ServiceRegistryClientTestContext {
}
