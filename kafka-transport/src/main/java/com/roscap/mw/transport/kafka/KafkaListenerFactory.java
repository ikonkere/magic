package com.roscap.mw.transport.kafka;

import com.roscap.mw.kafka.spring.DelegatingKafkaListener;

/**
 * Simple listener factory abstraction for creating kafka listeners
 * 
 * @param <V> payload type
 * @author is.zharinov
 *
 */
public interface KafkaListenerFactory {
	/**
	 * register a kafka listener
	 * 
	 * @param listener
	 */
	public void registerInvokerListener(DelegatingKafkaListener listener);
	
	/**
	 * unregister an existing kafka listener
	 * 
	 * @param listener
	 */
	public void unregisterInvokerListener(DelegatingKafkaListener listener);
}
