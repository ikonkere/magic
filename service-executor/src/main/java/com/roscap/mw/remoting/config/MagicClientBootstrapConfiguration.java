package com.roscap.mw.remoting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Magic-client context that performs everything necessary
 * for creating Magic proxies
 * 
 * @author is.zharinov
 *
 */
@Configuration
public class MagicClientBootstrapConfiguration implements ImportAware {
	private String basePackage;

	/**
	 * packages to use for service interface discovery
	 * 
	 * @param arg0
	 */
	private void setPackages(String arg0) {
		this.basePackage = arg0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.annotation.ImportAware#setImportMetadata(org.springframework.core.type.AnnotationMetadata)
	 */
	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		AnnotationAttributes aa =
				AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableMagicClient.class.getName(), false));
		
		setPackages(aa.getString("packages"));
	}
	
	/**
	 * service proxy discovery and processing 
	 * 
	 * @return
	 */
	@Bean
	public ProxyClientFactoryPostProcessor remoteServiceProxyFactoryProcessor() {
		return new ProxyClientFactoryPostProcessor(basePackage);
	}
}
