package com.roscap.mw.transport.endpoint;

import com.roscap.mw.transport.header.HeaderContainer;

/**
 * Indicates an endpoint that uses transport headers for routing
 * or is itself routed using headers
 * 
 * @author is.zharinov
 *
 * @param <T>
 */
public interface RoutedEndpoint<T extends HeaderContainer> extends TransportEndpoint<T> {
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportEndpoint#destination()
	 */
	default public String destination() {
		return id().toString();
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportEndpoint#isRouted()
	 */
	@Override
	default public boolean isRouted() {
		return true;
	}
}
