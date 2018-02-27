package com.roscap.mw.kafka.spring;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpoint;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * A necessary extension to KafkaListenerEndpointRegistry that allows for dinamicity
 * 
 * @author is.zharinov
 *
 */
public class ConfigurableKafkaListenerEndpointRegistry extends KafkaListenerEndpointRegistry {
	private ApplicationContext applicationContext;
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.kafka.config.KafkaListenerEndpointRegistry#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		super.setApplicationContext(applicationContext);
		
		this.applicationContext = applicationContext;
	}
	
	protected Map<String, MessageListenerContainer> getListenerContainersMap() {
		try {
			Field f = KafkaListenerEndpointRegistry.class.getDeclaredField("listenerContainers");
			ReflectionUtils.makeAccessible(f);
			return (Map)f.get(this);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Destroy a message listener container for the given {@link KafkaListenerEndpoint}.
	 * <p>This removes the necessary infrastructure to honor that endpoint
	 * with regards to its configuration.
	 * @param endpoint the endpoint to remove
	 * @see #registerListenerContainer(KafkaListenerEndpoint, KafkaListenerContainerFactory)
	 */
	public void unregisterListnerContainer(KafkaListenerEndpoint endpoint) {
		Map<String, MessageListenerContainer> listenerContainers = getListenerContainersMap();

		synchronized (listenerContainers) {
			MessageListenerContainer mlc = listenerContainers.remove(endpoint.getId());
			
			if (mlc != null) {
				if (StringUtils.hasText(endpoint.getGroup()) &&
						this.applicationContext.containsBean(endpoint.getGroup())) {
					List<MessageListenerContainer> containerGroup =
							this.applicationContext.getBean(endpoint.getGroup(), List.class);
					containerGroup.remove(mlc);				
				}
	
				mlc.stop();
			}
		}
	}
}
