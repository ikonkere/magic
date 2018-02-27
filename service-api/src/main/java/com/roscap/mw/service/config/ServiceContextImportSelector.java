package com.roscap.mw.service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import com.roscap.mw.service.annotation.ConfigureServices;

/**
 * Helper class that selects proper context to import
 * based on @ConfigureServices attributes
 * 
 * @author is.zharinov
 *
 */
public class ServiceContextImportSelector implements ImportSelector {
	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.annotation.ImportSelector#selectImports(org.springframework.core.type.AnnotationMetadata)
	 */
	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		AnnotationAttributes aa =
				AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(ConfigureServices.class.getName(), false));
		return new String[] { ((aa.getClassArray("services") != null && aa.getClassArray("services").length > 0)
				? IncapsulatingServiceFacadeContext.class
				: EmptyContext.class).getCanonicalName()
		};
	}
	
	@Configuration
	public static class EmptyContext {}
}
