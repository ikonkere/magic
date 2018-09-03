package com.roscap.mw.transport.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.roscap.mw.transport.correlation.TransportHeader;
import com.roscap.mw.transport.endpoint.RoutedEndpoint;
import com.roscap.mw.transport.endpoint.TransportEndpoint;
import com.roscap.mw.transport.header.HeaderContainer;

/**
 * Aggregating endpoint that performs content-based routing to actual listeners
 * using Correlation-Id header
 * 
 * @internal
 * @author is.zharinov
 *
 */
public class AggregatingRoutingEndpoint implements TransportEndpoint<HeaderContainer> {
	private static final Logger logger = LoggerFactory.getLogger(AggregatingRoutingEndpoint.class);

	private final UUID endpointId;
	private final Map<UUID, RoutedEndpoint<HeaderContainer>> endpoints = 
			new HashMap<UUID, RoutedEndpoint<HeaderContainer>>();
	
	private final ExecutorService executor = Executors.newFixedThreadPool(10);
	
	public AggregatingRoutingEndpoint(UUID arg0) {
		endpointId = arg0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportEndpoint#receive(java.lang.String, com.roscap.mw.transport.HeaderContainer)
	 */
	@Override
	public synchronized void receive(String fromDestination, HeaderContainer payload) {
		UUID correlation = (UUID)payload.getHeader(TransportHeader.CORRELATION);
		
		if (correlation != null && endpoints.containsKey(correlation)) {
			logger.info(String.format("aggregator %s routing %s to %s", endpointId, payload, correlation));

			TransportEndpoint<HeaderContainer> endpoint = endpoints.get(correlation);
			executor.execute(() -> {
				endpoint.receive(fromDestination, payload);
			});
		}
		else {
			//do nothing
		}
	}
	
	/**
	 * Gracefully closese this endpoint.
	 * 
	 * This is called directly from spring context 
	 */
	public void close() {
		executor.shutdown();
		try {
			executor.awaitTermination(5l, TimeUnit.SECONDS);
		}
		catch (InterruptedException ie) {
			//not much we can do
		}
	}
	
	/**
	 * add an endpoint to this container
	 * 
	 * @param e
	 */
	public synchronized void add(RoutedEndpoint<HeaderContainer> e) {
		endpoints.put(e.id(), e);
	}
	
	/**
	 * remove an endpoint from this container
	 * 
	 * @param e
	 */
	public synchronized void remove(RoutedEndpoint<HeaderContainer> e) {
		endpoints.remove(e.id());
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportEndpoint#destination()
	 */
	@Override
	public String destination() {
		return endpointId.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportEndpoint#id()
	 */
	@Override
	public UUID id() {
		return endpointId;
	}

	/*
	 * this endpoint is centralized and therefore owns
	 * transport adapter's destination
	 * 
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.endpoint.TransportEndpoint#isDestinationOwner()
	 */
	@Override
	public boolean isDestinationOwner() {
		return true;
	}
}
