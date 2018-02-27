package com.roscap.mw.kafka.spring.event;

import org.springframework.kafka.event.KafkaEvent;

public class PartitionsAssignedEvent extends KafkaEvent {
	public PartitionsAssignedEvent(Object source) {
		super(source);
	}
}
