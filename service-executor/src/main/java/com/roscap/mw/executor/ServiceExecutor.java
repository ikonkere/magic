package com.roscap.mw.executor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.roscap.cdm.api.CdmUtils;
import com.roscap.mw.reflection.Argument;
import com.roscap.mw.registry.ServiceRegistry;
import com.roscap.mw.remoting.client.RemoteServiceInvoker;

/**
 * Service executor that uses service registry to execute methods
 * 
 * @author is.zharinov
 *
 */
public class ServiceExecutor {
	@Autowired
//	@Qualifier("0a398127-f403-4cce-80e0-1c754901736c")
	@Qualifier("ServiceRegistryClient")
	private ServiceRegistry serviceRegistry;

	@Autowired
	private ServiceAccessorFactory invokerFactory;
	
	private boolean cacheInstances = true;
	
	private final Map<URI, Object> serviceInstanceCache = new HashMap<>();

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
		
		Object spec;
		
		if (!this.cacheInstances || !serviceInstanceCache.containsKey(cdmServiceUri)) {
			spec = serviceRegistry.getSpec(cdmServiceUri);
		}
		else {
			spec = serviceInstanceCache.get(cdmServiceUri);
		}
		
		Class<?>[] argTypes = CdmUtils.resolveArguments(methodName, spec);
		RemoteServiceInvoker serviceInvoker =
				invokerFactory.createSynchronousInvoker(CdmUtils.searchForId(spec));
		
		try {
			return serviceInvoker.invoke(methodName,
					CdmUtils.resolveType(methodName, spec),
					Argument.wrap(argTypes, args));
		}
		catch (Throwable t) {
			throw new RuntimeException(t);
		}
		finally {
			if (this.cacheInstances) {
				serviceInstanceCache.put(cdmServiceUri, spec);
			}
		}
	}
	
	public void setCacheInstances(boolean arg0) {
		this.cacheInstances = arg0;
	}
}
