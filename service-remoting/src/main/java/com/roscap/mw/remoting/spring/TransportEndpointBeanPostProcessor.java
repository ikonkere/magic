package com.roscap.mw.remoting.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

import com.roscap.mw.transport.TransportAdapter;
import com.roscap.mw.transport.endpoint.TransportEndpoint;

/**
 * this BPP allows for auto-registration and graceful destruction
 * of transport endpoints
 * 
 * @author is.zharinov
 *
 */
public class TransportEndpointBeanPostProcessor implements BeanPostProcessor, DestructionAwareBeanPostProcessor {
	private TransportAdapter transportAdapter;

	/**
	 * 
	 * @param transportAdapter
	 */
	public void setTransportAdapter(TransportAdapter transportAdapter) {
		this.transportAdapter = transportAdapter;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
		//we do it for any endpoint registered as spring bean
		if (isApplicable(bean)) {
			transportAdapter.registerEndpoint((TransportEndpoint)bean);
		}

		return bean;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor#postProcessBeforeDestruction(java.lang.Object, java.lang.String)
	 */
	@Override
	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		//we do it for any endpoint registered as spring bean
		if (isApplicable(bean)) {
			transportAdapter.unregisterEndpoint((TransportEndpoint)bean);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor#requiresDestruction(java.lang.Object)
	 */
	@Override
	public boolean requiresDestruction(Object bean) {
		return isApplicable(bean);
	}
	
	/**
	 * determines whether this BPP is applicable to bean
	 * 
	 * @param bean
	 * @return
	 */
	private static boolean isApplicable(Object bean) {
		return (TransportEndpoint.class.isAssignableFrom(bean.getClass()));
	}
}