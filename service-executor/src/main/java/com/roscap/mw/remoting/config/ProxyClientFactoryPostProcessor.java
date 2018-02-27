package com.roscap.mw.remoting.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.roscap.cdm.api.annotation.Service;
import com.roscap.mw.executor.ServiceAccessorFactory;

/**
 * a BFPP that will scan given packages on classpath for CDM service interfaces
 * and create remote proxies for them
 *  
 * @author is.zharinov
 *
 */
public class ProxyClientFactoryPostProcessor implements BeanFactoryPostProcessor {
	private static final Logger logger = LoggerFactory.getLogger(ProxyClientFactoryPostProcessor.class);

	private final ClassPathScanningCandidateComponentProvider scanner;
	
	private final String basePackage;
	
	public ProxyClientFactoryPostProcessor(String arg0) {
		basePackage = arg0;
		
		scanner = new ClassPathScanningCandidateComponentProvider(false) {
			protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
				AnnotationMetadata metadata = beanDefinition.getMetadata();
				return metadata.isInterface();
			}
		};
		scanner.addIncludeFilter(new AnnotationTypeFilter(Service.class));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		for (BeanDefinition beanDefinition : scanner.findCandidateComponents(basePackage)) {
			try {
				Class<?> targetClass = Class.forName(beanDefinition.getBeanClassName());
				Service s = targetClass.getAnnotation(Service.class);

				logger.info("found proxy: " + s.uri());
				
				if (beanFactory instanceof BeanDefinitionRegistry) {
					((BeanDefinitionRegistry)beanFactory).registerBeanDefinition(targetClass.getSimpleName() + "Client",
							createClientProxyBeanDefinition(s, targetClass));
					
					if (!s.id().isEmpty()) {
						((BeanDefinitionRegistry)beanFactory).registerAlias(targetClass.getSimpleName() + "Client", s.id());
					}
				}
			}
			catch (ClassNotFoundException t) {
				logger.warn("scanned a class, but can't instantiate", t);
			}
		}
	}
	
	/**
	 * create a BD of PFB for given class and CDM meta
	 * 
	 * @param targetClass
	 * @param s
	 * @return
	 */
	private static BeanDefinition createClientProxyBeanDefinition(Service s, Class<?> targetClass) {
		BeanDefinitionBuilder bdb = BeanDefinitionBuilder.genericBeanDefinition(ServiceAccessorFactory.class);
		bdb.setFactoryMethod("createProxyInstance");
		bdb.addConstructorArgValue(s);
		bdb.addPropertyValue("serviceInterface", targetClass);
		bdb.addPropertyReference("transportAdapter", "transportAdapter");
		return bdb.getBeanDefinition();
	}
}
