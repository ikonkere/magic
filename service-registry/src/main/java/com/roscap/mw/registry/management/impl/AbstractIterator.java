package com.roscap.mw.registry.management.impl;

import java.util.Iterator;
import java.util.List;

/**
 * Simple list iterator that allows
 * customizing the next index value.
 * Always has a next element, it's
 * up to implementations to maintain
 * this via index manipulation.
 * Not thread-safe
 * 
 * @author is.zharinov
 *
 * @param <U>
 */
abstract class AbstractIterator<U> implements Iterator<U> {
	final List<U> base;
	int index = -1;

	AbstractIterator(List<U> arg0) {
		base = arg0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	@Override
	public U next() {
		increment();
		return base.get(this.index);
	}

	/**
	 * perform index increment
	 */
	abstract void increment();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new IllegalArgumentException("remove not allowed");
	}
}