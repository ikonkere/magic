package com.roscap.mw.transport.correlation;

/**
 * Correlation policy for responses
 * 
 * @author is.zharinov
 *
 */
@Deprecated
public interface CorrelationPolicy<T> {
	/**
	 * Destination chosen by this policy
	 * @return
	 */
	public String destination();
	
	/**
	 * 
	 * @return
	 */
	public T correlation();
	
	/**
	 * 
	 * @return
	 */
	public CorrelationType type();
}
