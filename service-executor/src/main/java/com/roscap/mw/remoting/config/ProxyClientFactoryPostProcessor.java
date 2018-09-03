package com.roscap.mw.remoting.config;

import java.util.UUID;

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
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.roscap.cdm.api.annotation.Service;
import com.roscap.mw.remoting.client.DeferredRemoteServiceInvoker;
import com.roscap.mw.remoting.client.DynamicRemoteServiceInvoker;
import com.roscap.mw.remoting.client.RemoteServiceProxyFactoryBean;

/**
 * a BFPP that will scan given packages on classpath for CDM service interfaces
 * and create remote proxies for them
 *  
 * @author is.zharinov
 *
 */
public class ProxyClientFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {
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

				logger.info("found service interface: " + s.uri());
				
				if (beanFactory.containsBean(targetClass.getSimpleName() + "Client")) {
					logger.warn("proxy already exists for: " + s.uri());
				}
				else {
					if (beanFactory instanceof BeanDefinitionRegistry) {
						((BeanDefinitionRegistry)beanFactory).registerBeanDefinition(targetClass.getSimpleName() + "Invoker",
								createClientInvokerBeanDefinition(s, targetClass));
						((BeanDefinitionRegistry)beanFactory).registerBeanDefinition(targetClass.getSimpleName() + "Client",
								createClientProxyBeanDefinition(targetClass));
						
						if (!s.id().isEmpty()) {
							((BeanDefinitionRegistry)beanFactory).registerAlias(targetClass.getSimpleName() + "Client", s.id());
						}

						logger.debug("created proxies for: " + s.uri());
					}
				}
			}
			catch (ClassNotFoundException t) {
				logger.warn("scanned a class, but can't instantiate", t);
			}
		}
	}

	/**
	 * create a BD of service invoker for given class and CDM meta
	 * 
	 * @param s
	 * @param targetClass
	 * @return
	 */
	private static BeanDefinition createClientInvokerBeanDefinition(Service s, Class<?> targetClass) {
		String serviceId = s.id();
		BeanDefinitionBuilder bdb;
		
		if (!serviceId.isEmpty()) {
			bdb = BeanDefinitionBuilder.genericBeanDefinition(DynamicRemoteServiceInvoker.class);
			bdb.addConstructorArgValue(s.uri());
			bdb.addConstructorArgValue(UUID.fromString(serviceId));
		}
		else {
			bdb = BeanDefinitionBuilder.genericBeanDefinition(DeferredRemoteServiceInvoker.class);
			bdb.addConstructorArgValue(s.uri());
		}

		bdb.addPropertyValue("serviceInterface", targetClass);
		bdb.addPropertyReference("transportAdapter", "transportAdapter");
		return bdb.getBeanDefinition();
	}
	
	/**
	 * create a BD of PFB for given class and CDM meta
	 * 
	 * @param targetClass
	 * @return
	 */
	private static BeanDefinition createClientProxyBeanDefinition(Class<?> targetClass) {
		BeanDefinitionBuilder bdb = BeanDefinitionBuilder.genericBeanDefinition(RemoteServiceProxyFactoryBean.class);
		bdb.addConstructorArgReference(targetClass.getSimpleName() + "Invoker");
		return bdb.getBeanDefinition();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
