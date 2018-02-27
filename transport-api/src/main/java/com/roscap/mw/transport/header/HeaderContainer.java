package com.roscap.mw.transport.header;

import java.io.Serializable;

import com.roscap.mw.transport.correlation.TransportHeader;

/**
 * Header container for TransportHeader
 * 
 * @author is.zharinov
 *
 */
public interface HeaderContainer extends Serializable {
	/**
	 * returns header value
	 * 
	 * @param header
	 * @return null if no such header in this container
	 */
	public Serializable getHeader(TransportHeader header);
	
	/**
	 * adds header value, replacing current (if any)
	 * 
	 * @param header
	 * @param value
	 */
	public void addHeader(TransportHeader header, Serializable value);
	
	/**
	 * indicates if there's a header in this container
	 * 
	 * @param header
	 * @return
	 */
	public boolean containsHeader(TransportHeader header);
}
