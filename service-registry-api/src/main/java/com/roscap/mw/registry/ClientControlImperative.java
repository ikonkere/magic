package com.roscap.mw.registry;

import java.io.Serializable;

import com.roscap.mw.transport.correlation.TransportHeader;
import com.roscap.mw.transport.header.Attributes;
import com.roscap.mw.transport.header.HeaderContainer;

/**
 * Extended control imperative for managed clients (only
 * when client cahing is on).
 * 
 * @author is.zharinov
 *
 */
public class ClientControlImperative implements HeaderContainer {
	private static final long serialVersionUID = -5117645430909664787L;
	private Attributes headers = new Attributes();
	private final ControlImperative imperative;

	public ClientControlImperative(ControlImperative arg0) {
		imperative = arg0;
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
	 * Which control imperative this container has
	 * @return
	 */
	public ControlImperative getImperative() {
		return imperative;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return imperative.toString();
	}
}
