package com.roscap.mw.transport.correlation;

/**
 * Defines transport headers that we can exchange
 * internally, using Magic
 * 
 * @author is.zharinov
 *
 */
public enum TransportHeader {
	/**
	 * Client-Id
	 */
	CLIENT,
	/**
	 * Return-Type of invocation
	 */
	RETURN_TYPE,
	/**
	 * Correlation-Type that is used to correlate
	 * between Magic clients and servers
	 */
	CORRELATION_TYPE,
	/**
	 * Correlation-Id that is used for internal
	 * per-invocation transport routing
	 */
	CORRELATION;
}
