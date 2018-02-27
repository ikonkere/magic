package com.roscap.mw.kafka.spring;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.roscap.mw.transport.endpoint.TransportEndpoint;
import com.roscap.mw.transport.kafka.KafkaListenerFactory;

/**
 * An extension to original BPP that allows for creating kafka listeners
 * in runtime according to service metadata
 * 
 * @author is.zharinov
 *
 * @param <V>
 */
public class ReflectiveKafkaListenerFactory<V> extends KafkaListenerAnnotationBeanPostProcessor<String, V>
		implements KafkaListenerFactory, DestructionAwareBeanPostProcessor {
	private static final Logger logger = LoggerFactory.getLogger(ReflectiveKafkaListenerFactory.class);
	private KafkaTopicFactory topicFactory;
	private int partitionFactor = 1;

	/**
	 * topic factory to use
	 * 
	 * @param arg0
	 */
	public void setTopicFactory(KafkaTopicFactory arg0) {
		this.topicFactory = arg0;
	}
	
	/**
	 * partition amount multiplier to use for destination owners.
	 * 
	 * 1 by default.
	 * 
	 * @param arg0
	 */
	public void setPartitionFactor(int arg0) {
		partitionFactor = arg0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.kafka.spring.KafkaListenerFactory#registerInvokerListener(com.roscap.mw.kafka.spring.ServiceMetadataProcessor)
	 */
	@Override
	public void registerInvokerListener(DelegatingKafkaListener listener) {
		KafkaListener fakeListener = listener.listenerMetadata();
		
		//this is a silly way of increasing topic partition amount
		topicFactory.createTopic(fakeListener.topics()[0],
				listener.delegate.isDestinationOwner() ? partitionFactor : 1);
		
		try {
			Set<Method> methodsWithHandler = MethodIntrospector.selectMethods(DelegatingKafkaListener.class,
					new ReflectionUtils.MethodFilter() {
						@Override
						public boolean matches(Method method) {
							return AnnotationUtils.findAnnotation(method, KafkaHandler.class) != null;
						}
					});

			processMultiMethodListeners(Collections.singletonList(fakeListener),
					new ArrayList<Method>(methodsWithHandler),
					listener,
					listener.toString());
			logger.debug(String.format("registered kafka listener %s for %s", fakeListener.id(), Arrays.toString(fakeListener.topics())));
		}
		catch (Exception e) {
			logger.warn("can't register listener", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.kafka.spring.KafkaListenerFactory#unregisterInvokerListener(com.roscap.mw.kafka.spring.ServiceMetadataProvider)
	 */
	@Override
	public void unregisterInvokerListener(DelegatingKafkaListener listener) {
		KafkaListener fakeListener = listener.listenerMetadata();
		
		MethodKafkaListenerEndpoint<String, V> endpoint = new MethodKafkaListenerEndpoint<String, V>();
		endpoint.setBean(listener);
		endpoint.setId(fakeListener.id());
		
		String group = fakeListener.containerGroup();
		if (StringUtils.hasText(group)) {
			endpoint.setGroup(group);
		}

		((ConfigurableKafkaListenerEndpointRegistry)getEndpointRegistry()).unregisterListnerContainer(endpoint);

		topicFactory.removeTopic(fakeListener.topics()[0]);

		logger.debug(String.format("unregistered kafka listener %s for %s", fakeListener.id(), Arrays.toString(fakeListener.topics())));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
		//we do it for any endpoint registered as spring bean
		if (TransportEndpoint.class.isAssignableFrom(bean.getClass())) {
			registerInvokerListener(new DelegatingKafkaListener((TransportEndpoint)bean));
		}
		//and we don't use @KafkaListener at all, even that it's similar,
		//to not instigate interoperability issues between the two mechanisms 
//		else {
//			return super.postProcessAfterInitialization(bean, beanName);
//		}

		return bean;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor#postProcessBeforeDestruction(java.lang.Object, java.lang.String)
	 */
	@Override
	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		//we do it for any endpoint registered as spring bean
		if (TransportEndpoint.class.isAssignableFrom(bean.getClass())) {
			unregisterInvokerListener(new DelegatingKafkaListener((TransportEndpoint)bean));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor#requiresDestruction(java.lang.Object)
	 */
	@Override
	public boolean requiresDestruction(Object bean) {
		return (TransportEndpoint.class.isAssignableFrom(bean.getClass()));
	}
	
	/**
	 * no idea why this particular method was made private in the superclass,
	 * so in order to avoid copy-paste we have to improvise
	 * 
	 * @see KafkaListenerAnnotationBeanPostProcessor#processMultiMethodListeners
	 */
	protected void processMultiMethodListeners(Collection<KafkaListener> classLevelListeners, List<Method> multiMethods,
			Object bean, String beanName) {
		try {
			Method processMultiMethodListeners = KafkaListenerAnnotationBeanPostProcessor.class.getDeclaredMethod("processMultiMethodListeners",
					Collection.class, List.class, Object.class, String.class);
			ReflectionUtils.makeAccessible(processMultiMethodListeners);
			processMultiMethodListeners.invoke(this, classLevelListeners, multiMethods, bean, beanName);
		}
		catch (Exception e) {
			//this is literally not possible
		}
	}
	
	/**
	 * funny that, you can register a listener, but not unregister it. So in order to do that
	 * we need a reference to endpointRegistry from superclass.
	 * 
	 * @return
	 */
	protected KafkaListenerEndpointRegistry getEndpointRegistry() {
		try {
			Field f = KafkaListenerAnnotationBeanPostProcessor.class.getDeclaredField("endpointRegistry");
			ReflectionUtils.makeAccessible(f);
			return (KafkaListenerEndpointRegistry)f.get(this);
		}
		catch (Exception e) {
			return null;
		}
	}
}
