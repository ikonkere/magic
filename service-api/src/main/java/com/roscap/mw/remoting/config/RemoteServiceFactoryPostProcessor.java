package com.roscap.mw.remoting.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;

import com.roscap.cdm.api.annotation.Service;
import com.roscap.mw.reflection.DelegatingProxyFactoryBean;
import com.roscap.mw.reflection.ServiceInstanceProxyFactory;
import com.roscap.mw.remoting.ServiceInstanceExporter;

/**
 * A BFPP that will create exporter beans for CDM services, registered as Spring beans
 * 
 * @author is.zharinov
 *
 */
public class RemoteServiceFactoryPostProcessor implements BeanFactoryPostProcessor {
	private static final Logger logger = LoggerFactory.getLogger(RemoteServiceFactoryPostProcessor.class);

	private ConfigurableApplicationContext targetContext;

	/**
	 * set application context that will be target for infrastructure beans
	 * 
	 * @param targetContext
	 */
	public void setTargetContext(ConfigurableApplicationContext targetContext) {
		this.targetContext = targetContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		for (String beanName : beanFactory.getBeanNamesForAnnotation(Service.class)) {
			logger.info("found service: " + beanName);
			
			ListableBeanFactory targetBeanFactory = null;
			
			if (targetContext != null) {
				//must place infrastructure beans in a context different than the current
				targetBeanFactory = targetContext.getBeanFactory();

				if (targetBeanFactory instanceof BeanDefinitionRegistry) {
					//register bean bridge
					((BeanDefinitionRegistry)targetBeanFactory).registerBeanDefinition(beanName,
							createServiceBridge(beanName,
									beanFactory.getType(beanName),
									beanFactory));
				}
			}
			else {
				targetBeanFactory = beanFactory;
			}

			if (targetBeanFactory instanceof BeanDefinitionRegistry) {
				String instanceBeanName = "__" + beanName + "Instance";
				((BeanDefinitionRegistry)targetBeanFactory).registerBeanDefinition(instanceBeanName,
						createServiceInstanceBeanDefinition(beanName,
								targetBeanFactory.getType(beanName),
								targetBeanFactory.findAnnotationOnBean(beanName, Service.class)));

				((BeanDefinitionRegistry)targetBeanFactory).registerBeanDefinition("__" + beanName + "Exporter",
						createProxyListenerBeanDefinition(instanceBeanName));
			}

			logger.info("created service end-point");
		}
	}
	
	/**
	 * create a DB for bean bridge between current bean factory and given bean factory
	 * 
	 * @param beanName
	 * @param targetClass
	 * @param originalBeanFactory
	 * @return
	 */
	private static BeanDefinition createServiceBridge(String beanName, Class<?> targetClass, ListableBeanFactory originalBeanFactory) {
		BeanDefinitionBuilder bdb = BeanDefinitionBuilder.genericBeanDefinition(DelegatingProxyFactoryBean.class);
		bdb.addPropertyValue("proxyInterfaces", new Class<?>[] {targetClass});
		bdb.addPropertyValue("targetBeanName", beanName);
		bdb.addPropertyValue("targetBeanFactory", originalBeanFactory);
		return bdb.getBeanDefinition();
	}
	
	
	/**
	 * create a BD for given service instance bean name
	 * 
	 * @param targetBeanName
	 * @param targetClass
	 * @param s
	 * @return
	 */
	private static BeanDefinition createProxyListenerBeanDefinition(String targetBeanName) {
		BeanDefinitionBuilder bdb = BeanDefinitionBuilder.genericBeanDefinition(ServiceInstanceExporter.class);
		bdb.addConstructorArgReference(targetBeanName);
		bdb.addPropertyReference("transportAdapter", "transportAdapter");
		return bdb.getBeanDefinition();
	}

	/**
	 * create a BD for service instance proxy
	 * 
	 * @param targetBeanName
	 * @param s
	 * @return
	 */
	private static BeanDefinition createServiceInstanceBeanDefinition(String targetBeanName, Class<?> targetClass, Service s) {
		BeanDefinitionBuilder bdb = BeanDefinitionBuilder.genericBeanDefinition(ServiceInstanceProxyFactory.class);
		bdb.setFactoryMethod("newInstance");
		bdb.addConstructorArgValue(s);
		bdb.addConstructorArgValue(targetClass);
		bdb.addConstructorArgReference(targetBeanName);
		return bdb.getBeanDefinition();
	}
}
