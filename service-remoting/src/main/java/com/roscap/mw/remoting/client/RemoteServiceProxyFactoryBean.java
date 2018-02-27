package com.roscap.mw.remoting.client;

import java.util.UUID;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;

/**
 * Ordinary PFB responsible for creating proxy object with it's superclass
 * as method interceptor
 * @author is.zharinov
 *
 */
public class RemoteServiceProxyFactoryBean extends RemoteServiceInvoker
		implements FactoryBean<Object>, BeanClassLoaderAware {
	private Object serviceProxy;

	public RemoteServiceProxyFactoryBean() {
		
	}

	public RemoteServiceProxyFactoryBean(UUID arg0) {
		super(arg0);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		if (this.getServiceInterface() == null) {
			throw new IllegalArgumentException("Property 'serviceInterface' is required");
		}
		this.serviceProxy = new ProxyFactory(this.getServiceInterface(), this).getProxy(this.getBeanClassLoader());

		super.afterPropertiesSet();
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
		return this.getServiceInterface();
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
