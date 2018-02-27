package com.roscap.mw.service.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.AnnotationMetadata;

import com.roscap.mw.service.annotation.ConfigureServices;

/**
 * helper class with which we make annotation attributes
 * available as properties
 * 
 * @author is.zharinov
 *
 */
public class ServiceBootstrapConfigurer implements ImportBeanDefinitionRegistrar, EnvironmentAware {
	ConfigurableEnvironment env;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		AnnotationAttributes aa =
				AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(ConfigureServices.class.getName(), false));
		
		Map<String, Object> p = new HashMap<String, Object>();
		p.put("bootstrap.address", aa.getString("bootstrap"));
		MapPropertySource ps = new MapPropertySource("service.properties", p);
		
		env.getPropertySources().addFirst(ps);
		
		registry.registerBeanDefinition("servicePropertiesConfigurer", createPropertyConfigurerBeanDefinition(env));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.EnvironmentAware#setEnvironment(org.springframework.core.env.Environment)
	 */
	@Override
	public void setEnvironment(Environment environment) {
		if (environment instanceof ConfigurableEnvironment) {
			env = (ConfigurableEnvironment)environment;
		}
		else {
			//well, it's no use to us
			env = new StandardEnvironment();
		}
	}

	/**
	 * build a BD for ${bootstrap.address} property evaluation further in the merged context
	 * 
	 * @param env
	 * @return
	 */
	private static BeanDefinition createPropertyConfigurerBeanDefinition(Environment env) {
		BeanDefinitionBuilder bdb = BeanDefinitionBuilder.genericBeanDefinition(PropertySourcesPlaceholderConfigurer.class);
		bdb.addPropertyValue("ignoreUnresolvablePlaceholders", true);
		bdb.addPropertyValue("environment", env);
		return bdb.getBeanDefinition();
	}
}
