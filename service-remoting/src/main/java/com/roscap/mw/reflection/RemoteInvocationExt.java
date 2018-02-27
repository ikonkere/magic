package com.roscap.mw.reflection;

import java.io.Serializable;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.remoting.support.RemoteInvocation;

import com.roscap.mw.transport.correlation.TransportHeader;
import com.roscap.mw.transport.header.HeaderContainer;

/**
 * convenient extension of default RI that provides
 * transport headers functionality
 * 
 * @author is.zharinov
 *
 */
public class RemoteInvocationExt extends RemoteInvocation implements HeaderContainer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1848853629637589941L;

	/**
	 * Create a new RemoteInvocation for the given AOP method invocation.
	 * @param methodInvocation the AOP invocation to convert
	 */
	public RemoteInvocationExt(MethodInvocation arg0) {
		super(arg0);
	}

	/**
	 * Create a new RemoteInvocation for the given parameters.
	 * @param methodName the name of the method to invoke
	 * @param parameterTypes the parameter types of the method
	 * @param arguments the arguments for the invocation
	 */
	public RemoteInvocationExt(String arg0, Class<?>[] arg1, Object[] arg2) {
		super(arg0, arg1, arg2);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.HeaderContainer#getHeader(com.roscap.mw.transport.TransportHeader)
	 */
	@Override
	public Serializable getHeader(TransportHeader header) {
		return containsHeader(header) ? getAttribute(header.toString()) : null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.HeaderContainer#addHeader(com.roscap.mw.transport.TransportHeader, java.io.Serializable)
	 */
	@Override
	public void addHeader(TransportHeader header, Serializable value) {
		addAttribute(header.toString(), value);
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.HeaderContainer#containsHeader(com.roscap.mw.transport.TransportHeader)
	 */
	@Override
	public boolean containsHeader(TransportHeader header) {
		return getAttributes().containsKey(header.toString());
	}
}
