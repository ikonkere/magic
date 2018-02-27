package com.roscap.mw.reflection;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.UUID;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AbstractSingletonProxyFactoryBean;
import org.springframework.util.ReflectionUtils;

import com.roscap.cdm.api.IdGenerator;
import com.roscap.cdm.api.ServiceInstance;
import com.roscap.cdm.api.CdmUtils;
import com.roscap.cdm.api.annotation.Service;

/**
 * A PFB that creates service instance proxies. It is not prototype,
 * so essentially there's one PFB (and one proxy) for each service
 * 
 * @author is.zharinov
 *
 * @param <T>
 */
public class ServiceInstanceProxyFactory<T> extends AbstractSingletonProxyFactoryBean implements MethodInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceProxyFactory.class);
	
	private final UUID instanceId;
	private final URI serviceUri;
	private final Class<T> serviceClass;
	private final Object descriptor;

	/**
	 * create a CDM service instance for a given service
	 *  
	 * @param serviceDescriptor
	 * @param serviceClass
	 * @param service
	 * @return
	 * @throws InstantiationException
	 */
	public static <T> ServiceInstanceProxyFactory<T> newInstance(Service serviceDescriptor, Class<T> serviceClass, T service) throws InstantiationException {
		ServiceInstanceProxyFactory<T> i = new ServiceInstanceProxyFactory<T>(serviceDescriptor, serviceClass);
		i.setTarget(service);
		return i;
	}

	/**
	 * populates this POJO with @Service metadata 
	 * 
	 * @param arg0
	 * @throws InstantiationException
	 */
	@SuppressWarnings("unchecked")
	ServiceInstanceProxyFactory(Service arg0, Class<?> arg1) throws InstantiationException {
		try {
			if (!arg0.id().isEmpty()) {
				instanceId = UUID.fromString(arg0.id());
			}
			else {
				IdGenerator g = arg0.idGeneratorClass().newInstance();
				instanceId = g.generateId();
			}

			this.serviceUri = new URI(arg0.uri());
			this.serviceClass = (Class<T>)CdmUtils.findTargetServiceInterface(arg1);
			this.descriptor = CdmUtils.extractDescriptor(instanceId, serviceClass);
			
			setProxyInterfaces(new Class[] {serviceClass, ServiceInstance.class});
			setOpaque(true);
		}
		catch (Exception e) {
			logger.error("unexpected exception during initialization", e);
			throw new InstantiationException("can't create service instance: " + arg0);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.aop.framework.AbstractSingletonProxyFactoryBean#createMainInterceptor()
	 */
	@Override
	protected Object createMainInterceptor() {
		return this;
	}
	
	private Object getTarget() {
		try {
			Field f = AbstractSingletonProxyFactoryBean.class.getDeclaredField("target");
			ReflectionUtils.makeAccessible(f);
			return f.get(this);
		}
		catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException nsfe) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		switch(invocation.getMethod().getName()) {
		case "instanceId": return instanceId;
		case "serviceUri": return serviceUri;
		case "serviceClass": return serviceClass;
		case "serviceSpec": return descriptor;
		case "service": return /*this.getObject()*/ this.getTarget();
			default:
				return invocation.proceed();
		}
	}
}
