package com.roscap.mw.transport.endpoint;

import java.io.Serializable;
import java.util.UUID;

/**
 * Transport endpoint (consumer) abstraction. Endpoints
 * declared as spring beans will be automatically registered with
 * transport.
 * 
 * @author is.zharinov
 *
 * @param <T> payload this endpoint serves
 */
public interface TransportEndpoint<T extends Serializable> {
	/**
	 * Receives payload from destination. It's up to transport adapter
	 * this endpoint is served by to perform all type conversions 
	 * 
	 * @param fromDestination
	 * @param payload
	 */
	public void receive(String fromDestination, T payload);
	
	/**
	 * Returns "destination" this endpoint is attached to
	 * @return never null
	 */
	public String destination();
	
	/**
	 * this endpoint id
	 * 
	 * @return
	 */
	public UUID id();
	
	/**
	 * signifies whether this endpoint has no access to transport
	 * and is governed by another endpoint.
	 * 
	 * @see RoutedEndpoint
	 * @return
	 */
	public default boolean isRouted() {
		return false;
	}
	
	/**
	 * indicates that this endpoint "owns" its destination
	 * as in - may change its transport configuration.
	 * 
	 * @return
	 */
	public default boolean isDestinationOwner() {
		return false;
	}
}
