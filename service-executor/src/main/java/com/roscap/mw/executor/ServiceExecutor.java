package com.roscap.mw.executor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.roscap.cdm.api.CdmUtils;
import com.roscap.mw.reflection.Argument;
import com.roscap.mw.registry.ServiceRegistry;
import com.roscap.mw.remoting.client.DynamicRemoteServiceInvoker;
import com.roscap.mw.transport.TransportAdapter;

/**
 * Service executor that uses service registry to execute methods
 * 
 * @author is.zharinov
 *
 */
public class ServiceExecutor {
	private static ServiceExecutor instance;
	
	@Autowired
//	@Qualifier("0a398127-f403-4cce-80e0-1c754901736c")
	@Qualifier("ServiceRegistryClient")
	private ServiceRegistry serviceRegistry;

	@Autowired
	private TransportAdapter ta;
	
	private boolean cacheInstances = true;
	
	private final Map<URI, DynamicRemoteServiceInvoker> serviceInstanceCache = new HashMap<>();

	public ServiceExecutor() {
		instance = this;
	}
	
	public static ServiceExecutor getInstance() {
		return instance;
	}
	
	/**
	 * execute a programmatic call of a service when there's no local client present
	 * 
	 * @param cdmUri
	 * @param args
	 * @return
	 * @throws IllegalArgumentException
	 */
	public Object execute(URI cdmUri, Object... args) throws IllegalArgumentException {
		String methodName = cdmUri.getQuery();
		
		if (methodName == null) {
			throw new IllegalArgumentException("uri is not specific enough");
		}
		
		URI cdmServiceUri = URI.create(cdmUri.toString().replaceFirst("\\?" + methodName, ""));

		DynamicRemoteServiceInvoker serviceInvoker;
		
		if (!this.cacheInstances || !serviceInstanceCache.containsKey(cdmServiceUri)) {
			serviceInvoker = createInvokerInstance(cdmServiceUri);
		}
		else {
			serviceInvoker= serviceInstanceCache.get(cdmServiceUri);
		}
		
		Class<?>[] argTypes = CdmUtils.resolveArguments(methodName, serviceInvoker.getSpec());
		
		try {
			return serviceInvoker.invoke(methodName,
					CdmUtils.resolveType(methodName, serviceInvoker.getSpec()),
					Argument.wrap(argTypes, args));
		}
		catch (Throwable t) {
			throw new RuntimeException(t);
		}
		finally {
			if (this.cacheInstances) {
				serviceInstanceCache.put(cdmServiceUri, serviceInvoker);
			}
		}
	}
	
	/**
	 * create a 
	 * 
	 * @param cdmServiceUri
	 * @return
	 */
	private DynamicRemoteServiceInvoker createInvokerInstance(URI cdmServiceUri) {
		DynamicRemoteServiceInvoker serviceInvoker =
				new DynamicRemoteServiceInvoker(cdmServiceUri.toString());
		serviceInvoker.setTransportAdapter(ta);
		
		try {
			serviceInvoker.afterPropertiesSet();
		}
		catch (Exception e) {
			throw new IllegalArgumentException("unable to create invoker", e);
		}
		
		serviceInvoker.invalidate();
		
		return serviceInvoker;
	}

	/**
	 * Allows to optimize this executor's behaviour by caching
	 * service instance specs received from the Service Registry
	 * 
	 * @param arg0
	 */
	public void setCacheInstances(boolean arg0) {
		this.cacheInstances = arg0;
	}
}
