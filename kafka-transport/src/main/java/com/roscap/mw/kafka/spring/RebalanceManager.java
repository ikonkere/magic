package com.roscap.mw.kafka.spring;

import java.util.Collection;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import com.roscap.mw.kafka.spring.event.PartitionsAssignedEvent;
import com.roscap.mw.kafka.spring.event.PartitionsRevokedEvent;

/**
 * This is a simple Kafka consumer rebalance event listener
 * that translates those into spring events for transport adapter to listen to
 * 
 * @author is.zharinov
 *
 */
@Deprecated
public class RebalanceManager implements ConsumerRebalanceListener, ApplicationEventPublisherAware {
	private ApplicationEventPublisher eventPublisher;
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.kafka.clients.consumer.ConsumerRebalanceListener#onPartitionsRevoked(java.util.Collection)
	 */
	@Override
	public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
		eventPublisher.publishEvent(new PartitionsRevokedEvent(partitions));
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.kafka.clients.consumer.ConsumerRebalanceListener#onPartitionsAssigned(java.util.Collection)
	 */
	@Override
	public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
		eventPublisher.publishEvent(new PartitionsAssignedEvent(partitions));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher(org.springframework.context.ApplicationEventPublisher)
	 */
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		eventPublisher = applicationEventPublisher;
	}
}