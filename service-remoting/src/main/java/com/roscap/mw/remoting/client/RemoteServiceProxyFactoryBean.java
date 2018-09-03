package com.roscap.mw.remoting.client;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Ordinary PFB responsible for creating proxy object 
 * for a given service invoker that acts as method interceptor
 * @author is.zharinov
 *
 */
public class RemoteServiceProxyFactoryBean<T extends RemoteServiceInvoker>
		implements FactoryBean<Object>, InitializingBean {
	private Object serviceProxy;
	private final T interceptor;

	public RemoteServiceProxyFactoryBean(T arg0) {
		interceptor = arg0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		if (interceptor.getServiceInterface() == null) {
			throw new IllegalArgumentException("Property 'serviceInterface' is required");
		}

		this.serviceProxy = new ProxyFactory(interceptor.getServiceInterface(), interceptor).getProxy(interceptor.getBeanClassLoader());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	@Override
	public Object getObject() {
		return this.serviceProxy;
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.remoting.client.KafkaInvokerClientInterceptor#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return interceptor.getServiceInterface();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}
}
