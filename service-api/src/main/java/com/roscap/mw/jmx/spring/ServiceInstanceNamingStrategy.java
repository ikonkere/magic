package com.roscap.mw.jmx.spring;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.jmx.export.naming.ObjectNamingStrategy;
import org.springframework.jmx.support.ObjectNameManager;

import com.roscap.cdm.api.ServiceInstance;

/**
 * a JMX naming strategy for CDM service instances.
 * 
 * <code>packageName:serviceName=serviceId</code>
 * 
 * @author is.zharinov
 *
 */
public class ServiceInstanceNamingStrategy implements ObjectNamingStrategy {
	/*
	 * (non-Javadoc)
	 * @see org.springframework.jmx.export.naming.ObjectNamingStrategy#getObjectName(java.lang.Object, java.lang.String)
	 */
	@Override
	public ObjectName getObjectName(Object managedBean, String beanKey) throws MalformedObjectNameException {
		ServiceInstance i = (ServiceInstance)managedBean;
		Class<?> serviceClass = i.serviceClass();
		return ObjectNameManager.getInstance(serviceClass.getPackage().getName() + ":" +
				serviceClass.getSimpleName() + "=" +
				i.instanceId().toString());
	}
}
