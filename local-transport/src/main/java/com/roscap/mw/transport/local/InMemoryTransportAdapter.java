package com.roscap.mw.transport.local;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.roscap.mw.transport.TransportAdapter;
import com.roscap.mw.transport.correlation.TransportHeader;
import com.roscap.mw.transport.endpoint.RoutedEndpoint;
import com.roscap.mw.transport.endpoint.TransportEndpoint;
import com.roscap.mw.transport.header.HeaderContainer;

/**
 * Simple in-memory transport for use within a single JVM.
 * 
 * Destinations are treated as FIFO queues, pub-sub is not really supported.
 * 
 * @author is.zharinov
 *
 */
public class InMemoryTransportAdapter implements TransportAdapter, RoutedEndpoint<HeaderContainer> {
	private static final Logger logger = LoggerFactory.getLogger(InMemoryTransportAdapter.class);

	private final Map<String, BlockingQueue<Serializable>> destinations = new HashMap<>();
	private final Map<String, Map<UUID, DelegatingQueueListener<Serializable>>> listeners = new HashMap<>();
	
	private final ExecutorService executor = Executors.newFixedThreadPool(10);
	private final UUID clientId = UUID.randomUUID();
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportAdapter#clientId()
	 */
	@Override
	public UUID clientId() {
		return clientId;
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportAdapter#initialize(java.lang.String[])
	 */
	@Override
	public void initialize(String... properties) {
		registerEndpoint((TransportEndpoint)this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportAdapter#close()
	 */
	@Override
	public void close() {
		executor.shutdown();

		listeners.values().forEach((s) -> {
			s.forEach((k, v) -> {
				v.deactivate();
			});
		});
	}
	
	public boolean isEndpointRegistered(TransportEndpoint<Serializable> endpoint) {
		return destinations.containsKey(endpoint.destination());
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportAdapter#registerEndpoint(com.roscap.mw.transport.endpoint.TransportEndpoint)
	 */
	@Override
	public void registerEndpoint(TransportEndpoint<Serializable> endpoint) {
		String d = endpoint.destination();

		if (isEndpointRegistered(endpoint)) {
			logger.warn(String.format("attempt to re-register endpoint %s for %s, ignoring", endpoint.id(), d));
			return;
		}
		
		destinations.put(d, new LinkedBlockingQueue<Serializable>());
		
		if (!listeners.containsKey(d)) {
			listeners.put(d, new HashMap<>());
		}
		
		DelegatingQueueListener<Serializable> listener =
				new DelegatingQueueListener<Serializable>(endpoint, destinations.get(d));
		
		listeners.get(d).put(endpoint.id(), listener);
		
		executor.execute(listener);

		logger.debug(String.format("registered listener %s for %s", endpoint.id(), d));
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportAdapter#unregisterEndpoint(com.roscap.mw.transport.endpoint.TransportEndpoint)
	 */
	@Override
	public void unregisterEndpoint(TransportEndpoint<Serializable> endpoint) {
		String d = endpoint.destination();
		
		if (listeners.containsKey(d)) {
			DelegatingQueueListener<Serializable> listener = listeners.get(d).remove(endpoint.id());

			if (listener != null) {
				listener.deactivate();

				logger.debug(String.format("unregistered listener %s for %s", endpoint.id(), d));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportAdapter#send(java.lang.String, java.lang.Object)
	 */
	@Override
	public void send(String toDestination, Object payload) {
		if (!destinations.containsKey(toDestination)) {
			logger.warn("can't send payload, destination not registered " + toDestination);
		}
		else if (payload instanceof Serializable) {
			logger.debug(String.format("sending %s to %s", payload, toDestination));
			destinations.get(toDestination).offer((Serializable)payload);
			logger.debug(String.format("sent %s to %s", payload, toDestination));
		}
		else {
			logger.warn("can't send payload, not serializable");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.endpoint.TransportEndpoint#receive(java.lang.String, java.io.Serializable)
	 */
	@Override
	public void receive(String fromDestination, HeaderContainer payload) {
		Serializable route = payload.getHeader(TransportHeader.CORRELATION);
		send(route.toString(), payload);
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.endpoint.TransportEndpoint#id()
	 */
	@Override
	public UUID id() {
		return clientId();
	}
}
