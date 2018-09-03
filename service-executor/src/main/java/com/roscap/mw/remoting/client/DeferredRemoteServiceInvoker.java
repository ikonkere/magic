package com.roscap.mw.remoting.client;

import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Convenient service invoker that gets its service id after it has been initialized.
 * This is to compensate the fact that bean instantiation and
 * transport initialization happen asynchronously and there's
 * no insurance of their order of execution  
 * 
 * @author is.zharinov
 *
 */
public class DeferredRemoteServiceInvoker extends DynamicRemoteServiceInvoker {
	private final Future<Object> deferredServiceSpec;
	private boolean initialized = false;
	
	/**
	 * construct this invoker with a promise that a serviceId
	 * will be available some time in future
	 * 
	 * @param arg0
	 */
	public DeferredRemoteServiceInvoker(String arg0) {
		super(arg0);
		deferredServiceSpec = requestSpec();
	}	
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.remoting.client.client.RemoteServiceInvoker#getServiceId()
	 */
	@Override
	public synchronized UUID getServiceId() {
		if (!initialized) {
			initialized = true;
			invalidate(deferredServiceSpec);
		}
		
		return super.getServiceId();
	}
}
