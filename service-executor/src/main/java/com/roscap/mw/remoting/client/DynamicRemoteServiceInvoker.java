package com.roscap.mw.remoting.client;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import com.roscap.cdm.api.CdmUtils;
import com.roscap.mw.executor.ServiceDiscoveryCallbackFactory;
import com.roscap.mw.registry.ClientControlImperative;
import com.roscap.mw.registry.ControlImperative;
import com.roscap.mw.transport.endpoint.RoutedEndpoint;
import com.roscap.mw.transport.endpoint.TransportEndpoint;

/**
 * A service invoker that caches its target and is able to
 * receive control imperatives from the Service Registry
 * 
 * @author is.zharinov
 *
 */
public class DynamicRemoteServiceInvoker extends RemoteServiceInvoker
		implements BeanFactoryAware, RoutedEndpoint<ClientControlImperative> {
	private static final Logger logger = LoggerFactory.getLogger(DynamicRemoteServiceInvoker.class);

	private BeanFactory beanFactory;
	protected final String serviceUri;
	protected Object serviceSpec;

	/**
	 * 
	 * @param arg0 CDM URI of a service
	 */
	public DynamicRemoteServiceInvoker(String arg0) {
		this.serviceUri = arg0;
	}

	/**
	 * 
	 * @param arg0 CDM URI of a service
	 * @param arg1 Instance id when known
	 */
	public DynamicRemoteServiceInvoker(String arg0, UUID arg1) {
		super(arg1);
		this.serviceUri = arg0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	public void setBeanFactory(BeanFactory arg0) throws BeansException {
		this.beanFactory = arg0;
	}

	/**
	 * 
	 * @param serviceId
	 */
	private void updateServiceId(UUID serviceId) {
		UUID oldServiceId = super.getServiceId();

		if (oldServiceId != null) {
			if (beanFactory != null) {
				((BeanDefinitionRegistry)beanFactory).removeAlias(oldServiceId.toString());
			}
			transportAdapter.unregisterEndpoint((TransportEndpoint)this);
		}

		setServiceId(serviceId);
		
		//we repeat this for deferred or changing service ids
		if (beanFactory != null) {
			((BeanDefinitionRegistry)beanFactory).registerAlias(this.getServiceInterface().getSimpleName() + "Client", serviceId.toString());
		}
		transportAdapter.registerEndpoint((TransportEndpoint)this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.endpoint.TransportEndpoint#id()
	 */
	@Override
	public UUID id() {
		return getServiceId();
	}

	/**
	 * current service spec that this invoker uses
	 * @return
	 */
	public Object getSpec() {
		return serviceSpec;
	}
	
	/**
	 * generate an asynchronous request for service spec
	 * @return
	 */
	protected Future<Object> requestSpec() {
		return ServiceDiscoveryCallbackFactory.queryInstance(serviceUri);
	}

	/**
	 * invalidates this invoker's state, that is - its
	 * inner CDM spec cache
	 * 
	 * @throws IllegalStateException
	 */
	public void invalidate() throws IllegalStateException{
		invalidate(requestSpec());
	}

	/**
	 * invalidates this invoker's state, that is - its
	 * inner CDM spec cache
	 * 
	 * @param request a future to use
	 * @throws IllegalStateException
	 */
	protected void invalidate(Future<Object> request) throws IllegalStateException{
		try {
			serviceSpec = request.get();
			updateServiceId(CdmUtils.searchForId(serviceSpec));
		}
		catch (InterruptedException | ExecutionException e) {
			logger.error("can't set-up this proxy", e);
			throw new IllegalStateException(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.endpoint.TransportEndpoint#receive(java.lang.String, java.io.Serializable)
	 */
	@Override
	public synchronized void receive(String fromDestination, ClientControlImperative payload) {
		ControlImperative imperative = payload.getImperative();
		
		logger.info("received control imperative: " + imperative);
		
		switch (imperative) {
		case REFRESH:
			invalidate(); break;
		default:
		}
	}
}
