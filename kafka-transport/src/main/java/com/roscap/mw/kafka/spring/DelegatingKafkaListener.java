package com.roscap.mw.kafka.spring;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import com.roscap.mw.transport.endpoint.TransportEndpoint;

/**
 * Simple spring-kafka listener that delegates to more generic
 * {@link TransportEndpoint}
 * 
 * @author is.zharinov
 *
 * @param <T> Payload type
 */
public class DelegatingKafkaListener<T extends Serializable> {
	private static final Logger logger = LoggerFactory.getLogger(DelegatingKafkaListener.class);

	final TransportEndpoint<T> delegate;
	
	public DelegatingKafkaListener(TransportEndpoint<T> arg0) {
		this.delegate = arg0;
	}

	/**
	 * Spring-kafka listerer method that
	 * is used to receive payloads from topics
	 * 
	 * @param topic
	 * @param partition
	 * @param offset
	 * @param payload
	 */
	@KafkaHandler
	public void onMessage(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, @Header(KafkaHeaders.OFFSET) long offset,
			@Payload T payload) {
		logger.debug(String.format("inbound transport %s:%s:%s", topic, partition, offset));
		delegate.receive(topic, payload);
	}
	
	/**
	 * fakes KafkaListener annotation to better reuse spring-kafka code
	 * 
	 * @param serviceId
	 * @param side
	 * @return
	 */
	public KafkaListener listenerMetadata() {
		return new KafkaListener() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return KafkaListener.class;
			}

			@Override
			public String id() {
				return String.valueOf(delegate.id());
			}

			@Override
			public String containerFactory() {
				return "";
			}

			@Override
			public String[] topics() {
				return new String[] {
					delegate.destination()
//					delegate.id().toString()
				};
			}

			@Override
			public String topicPattern() {
				return "";
			}

			@Override
			public TopicPartition[] topicPartitions() {
				return new TopicPartition[] {};
			}

			@Override
			public String group() {
				return "";
			}

			@Override
			public String containerGroup() {
				return "";
			}

			@Override
			public String errorHandler() {
				return "";
			}

			@Override
			public String groupId() {
				return "";
			}

			@Override
			public boolean idIsGroup() {
				return true;
			}
		};
	}
}
