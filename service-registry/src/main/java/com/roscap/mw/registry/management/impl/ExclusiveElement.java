package com.roscap.mw.registry.management.impl;

import java.util.List;

import com.roscap.mw.registry.management.StorageManager;

/**
 * Policy that imposes exclusivity, one instance per service
 * 
 * @author is.zharinov
 *
 * @param <T>
 * @param <U>
 */
public class ExclusiveElement<T, U> extends AbstractIteratingPolicy<T, U> {
	public ExclusiveElement(StorageManager arg0) {
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
				this.index = 0;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.registry.management.impl.AbstractIteratingPolicy#iterator(java.util.List, com.roscap.mw.registry.management.impl.AbstractIterator)
	 */
	@Override
	AbstractIterator<U> iterator(List<U> base, AbstractIterator<U> i) {
		//essentially we don't care
		return i;
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.registry.management.InstanceManagementPolicy#allow()
	 */
	@Override
	public final boolean allow() {
		//this policy doesn't allow additional elements
		return false;
	}
}
