package com.roscap.cdm.api;

import java.net.URI;
import java.util.UUID;

/**
 * Service instance metadata
 * 
 * @author is.zharinov
 *
 * @param <T>
 */
public interface ServiceInstance<T> {
	/**
	 * 
	 * service instance id
	 * @return
	 */
	public UUID instanceId();
	
	/**
	 * service URI
	 * @return
	 */
	public URI serviceUri();
	
	/**
	 * service class
	 * @return
	 */
	public Class<T> serviceClass();
	
	/**
	 * CDM specification of this service
	 * @return
	 */
	public Object serviceSpec();
	
	/**
	 * actual service instance
	 * 
	 * @return
	 */
	public T service();
}
