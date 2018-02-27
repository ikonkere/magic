package com.roscap.mw.transport;

import java.io.Serializable;
import java.util.UUID;

import com.roscap.mw.transport.endpoint.TransportEndpoint;

/**
 * Transport layer abstraction (this is in fact <i>asynchronous</i> transport).
 * Implementations should be consistent with automatic endpoint registration.
 * 
 * @author is.zharinov
 * @see {@link TransportEndpoint}
 *
 */
public interface TransportAdapter {
	/**
	 * Client id of this adapter instance.
	 * 
	 * @return
	 */
	public UUID clientId();
	
	/**
	 * Initialize this adapter with properties. It's up to implementations
	 * to specify actual properties and the way they are processed.
	 * 
	 * @param properties
	 */
	public void initialize(String... properties);
	
	/**
	 * Gracefully stop this adapter, finishing all necessary threads
	 * and clearing all state variables.
	 */
	public void close();
	
	/**
	 * Register an endpoint with this adapter. Implementations are
	 * responsible to perform all necessary checks and transport-level
	 * initialization.
	 * 
	 * @param endpoint transport endpoint
	 */
	public void registerEndpoint(TransportEndpoint<Serializable> endpoint);
	
	/**
	 * Unregister an endpoint gracefully, finishing all spawned threads
	 * and performing everything necessary for this listener to be
	 * re-registered again later.
	 * 
	 * @param endpoint
	 */
	public void unregisterEndpoint(TransportEndpoint<Serializable> endpoint);
	
	/**
	 * Send payload to specified destination. This is the only way
	 * this adapter allows proactive use of transport.
	 * 
	 * @param toDestination never null
	 * @param payload never null
	 */
	public void send(String toDestination, Object payload);
	
	/**
	 * transport type of this adapter
	 * 
	 * @return null by default (means all types)
	 */
	default public TransportType type() {
		return null;
	}
}
