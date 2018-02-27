package com.roscap.mw.reflection;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AbstractSingletonProxyFactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * This is a context-context bridge that exposes a bean of {@code targetBeanFactory}
 * as proxy in current
 * 
 * @author is.zharinov
 *
 */
public class DelegatingProxyFactoryBean extends AbstractSingletonProxyFactoryBean implements MethodInterceptor {
	private ListableBeanFactory targetBeanFactory;
	private String targetBeanName;
	private Object target;
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.aop.framework.AbstractSingletonProxyFactoryBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		//it is assumed that target factory is already initialized,
		//otherwise this doesn't work
		this.target = targetBeanFactory.getBean(targetBeanName);
		setOpaque(true);
		setTarget(target);
		
		super.afterPropertiesSet();
	}
	
	/**
	 * Bean factory to use for delegate lookup
	 * 
	 * @param targetBeanFactory
	 */
	public void setTargetBeanFactory(ListableBeanFactory targetBeanFactory) {
		this.targetBeanFactory = targetBeanFactory;
	}

	/**
	 * Bean name to use for delegation
	 * @param targetBeanName
	 */
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.aop.framework.AbstractSingletonProxyFactoryBean#createMainInterceptor()
	 */
	@Override
	protected Object createMainInterceptor() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		//this proxy does nothing special
		return invocation.proceed();
	}
}