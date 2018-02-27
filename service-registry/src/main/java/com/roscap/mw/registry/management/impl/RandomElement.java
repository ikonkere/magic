package com.roscap.mw.registry.management.impl;

import java.util.List;
import java.util.Random;

import com.roscap.mw.registry.management.StorageManager;

/**
 * Policy that randomly chooses elements
 * 
 * @author is.zharinov
 *
 * @param <T>
 * @param <U>
 */
public class RandomElement<T, U> extends AbstractIteratingPolicy<T, U> {
	private static final Random r = new Random();

	public RandomElement(StorageManager arg0) {
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
				this.index = r.nextInt(base.size());
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.registry.management.impl.AbstractIteratingPolicy#iterator(java.util.List, com.roscap.mw.registry.management.impl.AbstractIterator)
	 */
	@Override
	AbstractIterator<U> iterator(List<U> base, AbstractIterator<U> i) {
		//no state to retain
		return iterator(base);
	}
}
