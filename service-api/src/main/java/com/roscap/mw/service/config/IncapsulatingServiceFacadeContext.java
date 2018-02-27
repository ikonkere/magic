package com.roscap.mw.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import com.roscap.mw.registry.client.context.ServiceContextHolder;
import com.roscap.mw.service.annotation.ConfigureServices;

/**
 * Configuration for cases when services are defined
 * in a separate context
 * 
 * @author is.zharinov
 *
 */
@Configuration
public class IncapsulatingServiceFacadeContext implements ImportAware {
	private Class<?>[] serviceContexts;
	
	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		AnnotationAttributes aa =
				AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(ConfigureServices.class.getName(), false));
		
		serviceContexts = aa.getClassArray("services");
	}

	/**
	 * service context holder when it's defined as separate
	 * 
	 * @return
	 */
	@Bean
	public ServiceContextHolder ctxHolder() {
		return ServiceContextHolder.createServiceContext(serviceContexts);
	}
}
