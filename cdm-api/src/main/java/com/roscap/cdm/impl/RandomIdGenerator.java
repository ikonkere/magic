package com.roscap.cdm.impl;

import java.util.Iterator;
import java.util.UUID;

import com.roscap.cdm.api.IdGenerator;

/**
 * Default id generator that produces
 * random ids
 * 
 * @author is.zharinov
 *
 */
public class RandomIdGenerator implements IdGenerator, Iterator<UUID> {
	/*
	 * (non-Javadoc)
	 * @see com.roscap.cdm.api.IdGenerator#generateId()
	 */
	@Override
	public UUID generateId() {
		return UUID.randomUUID();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public UUID next() {
		return generateId();
	}
}
