package com.roscap.mw.executor;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import com.roscap.cdm.api.CdmUtils;
import com.roscap.cdm.api.annotation.Service;
import com.roscap.mw.registry.ServiceRegistry;

/**
 * Convenient class that performs service instance discovery in the registry
 * and provides the internal code with instance ids.
 * 
 * @author is.zharinov
 *
 */
public class ServiceDiscoveryCallbackFactory implements ApplicationListener<ApplicationContextEvent>{
	public static final ServiceDiscoveryCallbackFactory instance =
			new ServiceDiscoveryCallbackFactory();  

	private final ExecutorService executor = Executors.newFixedThreadPool(5);

	private boolean initialized = false;
	private final Object monitor = new Object();
	
	@Autowired
//	@Qualifier("0a398127-f403-4cce-80e0-1c754901736c")
	@Qualifier("ServiceRegistryClient")
	private ServiceRegistry serviceRegistry;
	
	/**
	 * 
	 * @return if this factory is initialized
	 */
	synchronized boolean isInitialized() {
		return this.initialized;
	}
	
	/**
	 * 
	 * internal init-time flag for proper
	 * initialization
	 * 
	 * @param flag
	 */
	synchronized void setInitialized(boolean flag) {
		this.initialized = flag;
	}
	
	/**
	 * make a promise that an instance id will
	 * be returned some time in future
	 * 
	 * @param s
	 * @return
	 */
	public static Future<UUID> queryInstanceId(Service s) {
		return instance.getDeferredId(s);
	}
	
	/**
	 * make a promise that an instance id will
	 * be returned some time in future
	 * 
	 * @param s
	 * @return
	 */
	private Future<UUID> getDeferredId(Service s) {
		return executor.submit(createCallback(URI.create(s.uri())));
	}
	
	/**
	 * create an asynchronous callback to service registry 
	 * @param serviceUri
	 * @return
	 */
	private Callable<UUID> createCallback(URI serviceUri) {
		return () -> {
			//we compensate the fact that application initialization is not instantaneous
			//and serviceRegistry might not be injected at the time we want to call it
			if (!isInitialized()) {
				synchronized (monitor) {
					monitor.wait();
				}
			}

			Object spec = serviceRegistry.getSpec(serviceUri);
			return CdmUtils.searchForId(spec);
		};
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	public synchronized void onApplicationEvent(ApplicationContextEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			//only here we can resume executing the callbacks
			if (!isInitialized()) {
				setInitialized(true);
				
				synchronized (monitor) {
					monitor.notifyAll();
				}
			}
		}
		else if (event instanceof ContextClosedEvent) {
			executor.shutdown();
			try {
				executor.awaitTermination(5l, TimeUnit.SECONDS);
			}
			catch (InterruptedException ie) {
				//not much we can do
			}
		}
	}
}
