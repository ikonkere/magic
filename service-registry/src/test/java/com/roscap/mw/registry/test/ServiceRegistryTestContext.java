package com.roscap.mw.registry.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.roscap.mw.registry.context.ServiceRegistryContext;
import com.roscap.mw.registry.management.StorageManager;
import com.roscap.mw.registry.management.impl.infinispan.InfinispanStorage;

@Configuration
@Import(ServiceRegistryContext.class)
public class ServiceRegistryTestContext {
//	@Bean
//	public StorageManager storageManager(@Value("${hotrod.address}") String bootstrapAddress) {
//		return new InfinispanStorage(bootstrapAddress);
//	}
}
