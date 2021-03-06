package com.roscap.mw.reflection;

import java.io.Serializable;

import org.springframework.remoting.support.RemoteInvocationResult;

import com.roscap.mw.transport.correlation.TransportHeader;
import com.roscap.mw.transport.header.Attributes;
import com.roscap.mw.transport.header.HeaderContainer;

/**
 * convenient extension of default RIR that provides
 * transport headers functionality
 * 
 * @author is.zharinov
 *
 */
public class RemoteInvocationResultExt extends RemoteInvocationResult implements HeaderContainer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5791275281418509859L;

	private Attributes headers = new Attributes();
	
	/**
	 * Create a new RemoteInvocationResult for the given result value.
	 * @param value the result value returned by a successful invocation
	 * of the target method
	 */
	public RemoteInvocationResultExt(Object value) {
		super(value);
	}

	/**
	 * Create a new RemoteInvocationResult for an existing one
	 * @param arg0
	 */
	RemoteInvocationResultExt(RemoteInvocationResult arg0) {
		setValue(arg0.getValue());
		setException(arg0.getException());
		
		if (arg0 instanceof RemoteInvocationResultExt) {
			setHeaders(((RemoteInvocationResultExt)arg0).getHeaders());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.HeaderContainer#getHeader(com.roscap.mw.transport.TransportHeader)
	 */
	@Override
	public Serializable getHeader(TransportHeader header) {
		return containsHeader(header) ? headers.getAttribute(header.toString()) : null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.HeaderContainer#addHeader(com.roscap.mw.transport.TransportHeader, java.io.Serializable)
	 */
	@Override
	public void addHeader(TransportHeader header, Serializable value) {
		headers.addAttribute(header.toString(), value);
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.HeaderContainer#containsHeader(com.roscap.mw.transport.TransportHeader)
	 */
	@Override
	public boolean containsHeader(TransportHeader header) {
		return headers.getAttributes().containsKey(header.toString());
	}
	
	/**
	 * 
	 * @return
	 */
	public Attributes getHeaders() {
		return headers;
	}
	
	/**
	 * 
	 * @param arg0
	 */
	public void setHeaders(Attributes arg0) {
		this.headers = arg0;
	}
}
