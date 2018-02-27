package com.roscap.mw.jmx.spring;

import org.springframework.jmx.export.assembler.AutodetectCapableMBeanInfoAssembler;
import org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler;

import com.roscap.cdm.api.ServiceInstance;

/**
 * JMX MBean assembler for CDM service instances
 * 
 * @author is.zharinov
 *
 */
public class ServiceInstanceMBeanAssembler extends InterfaceBasedMBeanInfoAssembler
		implements AutodetectCapableMBeanInfoAssembler {
	public ServiceInstanceMBeanAssembler() {
		setManagedInterfaces(new Class[] {ServiceInstance.class});
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.jmx.export.assembler.AutodetectCapableMBeanInfoAssembler#includeBean(java.lang.Class, java.lang.String)
	 */
	@Override
	public boolean includeBean(Class<?> beanClass, String beanName) {
		return ServiceInstance.class.isAssignableFrom(beanClass);
	}
}
