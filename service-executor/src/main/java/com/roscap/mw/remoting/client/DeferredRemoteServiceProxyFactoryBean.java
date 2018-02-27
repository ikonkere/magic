package com.roscap.mw.remoting.client;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.roscap.mw.remoting.client.RemoteServiceProxyFactoryBean;

/**
 * Convenient PFB that gets its service id after it has been initialized.
 * This is to compensate the fact that bean instantiation and
 * transport initialization happen asynchronously and there's
 * no insurance of their order of execution  
 * 
 * @author is.zharinov
 *
 */
public class DeferredRemoteServiceProxyFactoryBean extends RemoteServiceProxyFactoryBean {
	private static final Logger logger = LoggerFactory.getLogger(DeferredRemoteServiceProxyFactoryBean.class);

	private final Future<UUID> deferredId;
	
	/**
	 * construct this PFB with a promise that a serviceId
	 * will be available some time in future
	 * 
	 * @param arg0
	 */
	public DeferredRemoteServiceProxyFactoryBean(Future<UUID> arg0) {
		deferredId = arg0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.remoting.client.client.RemoteServiceInvoker#getServiceId()
	 */
	@Override
	public UUID getServiceId() {
		try {
			setServiceId(deferredId.get());
			return super.getServiceId();
		}
		catch (InterruptedException | ExecutionException e) {
			logger.error("can't set-up this proxy");
			throw new IllegalStateException(e);
		}
	}
}
