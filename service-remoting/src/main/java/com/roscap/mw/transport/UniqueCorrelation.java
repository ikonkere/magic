package com.roscap.mw.transport;

import java.util.UUID;

import com.roscap.mw.transport.correlation.CorrelationPolicy;
import com.roscap.mw.transport.correlation.CorrelationType;

/**
 * Simple correlation policy that uses UUID to ensure unique
 * correlation
 * 
 * @author is.zharinov
 *
 */
public class UniqueCorrelation implements CorrelationPolicy<UUID> {
	private static final CorrelationType type = CorrelationType.UNIQUE;

	final UUID correlationId = UUID.randomUUID();
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.CorrelationPolicy#destination()
	 */
	@Override
	public String destination() {
		return type.replyTo(correlation().toString());
	}

	@Override
	public UUID correlation() {
		return correlationId;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return correlationId.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.CorrelationPolicy#type()
	 */
	@Override
	public CorrelationType type() {
		return type;
	}
}
