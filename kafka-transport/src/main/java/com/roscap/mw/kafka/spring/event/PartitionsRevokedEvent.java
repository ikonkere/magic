package com.roscap.mw.kafka.spring.event;

import org.springframework.kafka.event.KafkaEvent;

public class PartitionsRevokedEvent extends KafkaEvent {
	public PartitionsRevokedEvent(Object source) {
		super(source);
	}
}
