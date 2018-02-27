package com.roscap.mw.kafka.spring;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer.AckMode;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.config.ContainerProperties;

/**
 * An extension of concurrent KLCF that allows for more convenient acknowledge
 * mode configuration
 * 
 * @author is.zharinov
 *
 * @param <K>
 * @param <V>
 */
public class AckConcurrentKafkaListenerContainerFactory<K, V> extends ConcurrentKafkaListenerContainerFactory<K, V> {
	private AckMode ackMode;
	
	private ConsumerRebalanceListener rebalanceListener;
	
	/**
	 * (non-Javadoc)
	 * @see ContainerProperties#setAckMode(AckMode) 
	 */
	public void setAckMode(AckMode arg0) {
		this.ackMode = arg0;
	}
	
	/**
	 * Rebalance manager to use
	 * 
	 * @param arg0
	 */
	public void setRebalanceListener(ConsumerRebalanceListener arg0) {
		rebalanceListener = arg0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory#initializeContainer(org.springframework.kafka.listener.ConcurrentMessageListenerContainer)
	 */
	@Override
	protected void initializeContainer(ConcurrentMessageListenerContainer<K, V> instance) {
		super.initializeContainer(instance);
		instance.getContainerProperties().setAckMode(this.ackMode);
		
		if (rebalanceListener != null) {
			instance.getContainerProperties().setConsumerRebalanceListener(this.rebalanceListener);
		}
	}
}
