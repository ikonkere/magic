package com.roscap.mw.registry.management.impl;

import java.util.List;

import com.roscap.mw.registry.management.StorageManager;

/**
 * Round-robin policy. Courtesy of stackoverflow.
 * 
 * @author is.zharinov
 *
 * @param <T>
 * @param <U>
 */
public class RoundRobin<T, U> extends AbstractIteratingPolicy<T, U> {
	public RoundRobin(StorageManager arg0) {
		super(arg0);
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.registry.management.impl.AbstractIteratingPolicy#iterator(java.util.List)
	 */
	@Override
	AbstractIterator<U> iterator(List<U> base) {
		return new AbstractIterator<U>(base) {
			@Override
			void increment() {
				this.index = ++this.index % base.size();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.registry.management.impl.AbstractIteratingPolicy#iterator(java.util.List, com.roscap.mw.registry.management.impl.AbstractIterator)
	 */
	@Override
	AbstractIterator<U> iterator(List<U> base, AbstractIterator<U> j) {
		AbstractIterator<U> i = iterator(base);
		i.index = (j.index < i.base.size() ? j.index : -1);
		return i;
	}
}
