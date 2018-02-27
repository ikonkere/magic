package com.roscap.mw.reflection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.remoting.support.RemoteInvocationResult;

import com.roscap.mw.transport.correlation.TransportHeader;
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

	private Map<String, Serializable> attributes;
	
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
			setAttributes(((RemoteInvocationResultExt)arg0).getAttributes());
		}
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
	
	/* copied from RemoteInvocation */
	
	/**
	 * Add an additional invocation attribute. Useful to add additional
	 * invocation context without having to subclass RemoteInvocation.
	 * <p>Attribute keys have to be unique, and no overriding of existing
	 * attributes is allowed.
	 * <p>The implementation avoids to unnecessarily create the attributes
	 * Map, to minimize serialization size.
	 * @param key the attribute key
	 * @param value the attribute value
	 * @throws IllegalStateException if the key is already bound
	 */
	public void addAttribute(String key, Serializable value) throws IllegalStateException {
		if (this.attributes == null) {
			this.attributes = new HashMap<String, Serializable>();
		}
		if (this.attributes.containsKey(key)) {
			throw new IllegalStateException("There is already an attribute with key '" + key + "' bound");
		}
		this.attributes.put(key, value);
	}

	/**
	 * Retrieve the attribute for the given key, if any.
	 * <p>The implementation avoids to unnecessarily create the attributes
	 * Map, to minimize serialization size.
	 * @param key the attribute key
	 * @return the attribute value, or {@code null} if not defined
	 */
	public Serializable getAttribute(String key) {
		if (this.attributes == null) {
			return null;
		}
		return this.attributes.get(key);
	}

	/**
	 * Set the attributes Map. Only here for special purposes:
	 * Preferably, use {@link #addAttribute} and {@link #getAttribute}.
	 * @param attributes the attributes Map
	 * @see #addAttribute
	 * @see #getAttribute
	 */
	public void setAttributes(Map<String, Serializable> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Return the attributes Map. Mainly here for debugging purposes:
	 * Preferably, use {@link #addAttribute} and {@link #getAttribute}.
	 * @return the attributes Map, or {@code null} if none created
	 * @see #addAttribute
	 * @see #getAttribute
	 */
	public Map<String, Serializable> getAttributes() {
		return this.attributes;
	}
}
