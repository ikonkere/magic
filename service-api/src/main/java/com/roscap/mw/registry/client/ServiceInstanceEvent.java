package com.roscap.mw.registry.client;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

public class ServiceInstanceEvent extends ApplicationEvent {
	public ServiceInstanceEvent(UUID arg0) {
		super(arg0);
	}
}
