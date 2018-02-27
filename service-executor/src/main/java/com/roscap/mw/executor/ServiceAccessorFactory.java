package com.roscap.mw.executor;

import java.util.UUID;

import com.roscap.cdm.api.annotation.Service;
import com.roscap.mw.remoting.client.DeferredRemoteServiceProxyFactoryBean;
import com.roscap.mw.remoting.client.RemoteServiceInvoker;
import com.roscap.mw.remoting.client.RemoteServiceProxyFactoryBean;
import com.roscap.mw.remoting.config.ProxyClientFactoryPostProcessor;
import com.roscap.mw.transport.TransportAdapter;
import com.roscap.mw.transport.TransportFactory;

/**
 * This is a helper class that creates different kinds
 * of remote service accessors
 * 
 * @author is.zharinov
 *
 */
public class ServiceAccessorFactory {
	//we assume that transport was initialized elsewhere
	private final TransportAdapter ta = TransportFactory.getTransport();
	
	/**
	 * create a programmatic service invoker for a service instance
	 * 
	 * @param serviceId
	 * @return
	 */
	public RemoteServiceInvoker createSynchronousInvoker(final UUID sid) throws IllegalArgumentException {
		RemoteServiceInvoker invoker = new RemoteServiceInvoker(sid);
		invoker.setTransportAdapter(ta);
		
		try {
			invoker.afterPropertiesSet();
		}
		catch (Exception e) {
			throw new IllegalArgumentException("unable to create invoker", e);
		}
		
		return invoker;
	}
	
	/**
	 * create a PFB that will provide a facade for remote service.
	 * 
	 * @internal
	 * @param s CDM service def
	 * @return
	 * @see ProxyClientFactoryPostProcessor
	 */
	public static RemoteServiceProxyFactoryBean createProxyInstance(Service s) {
		String serviceId = s.id();
		
		if (!serviceId.isEmpty()) {
			return new RemoteServiceProxyFactoryBean(UUID.fromString(serviceId));			
		}
		else {
			return new DeferredRemoteServiceProxyFactoryBean(ServiceDiscoveryCallbackFactory.queryInstanceId(s));
		}
	}
}
