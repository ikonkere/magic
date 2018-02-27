package com.roscap.mw.remoting;

import com.roscap.cdm.api.ServiceInstance;
import com.roscap.mw.remoting.exporter.TransportEndpointServiceExporter;

/**
 * Convenient transport endpoint extension
 * that is created based on a service instance
 *  
 * @author is.zharinov
 *
 */
public class ServiceInstanceExporter extends TransportEndpointServiceExporter {
	/**
	 * 
	 * @param arg0
	 */
	public ServiceInstanceExporter(ServiceInstance arg0) {
		super(arg0.instanceId());
		setService(arg0.service());
		setServiceInterface(arg0.serviceClass());
	}
}
