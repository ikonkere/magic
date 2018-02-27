package com.roscap.mw.registry.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.roscap.mw.registry.management.InstanceManagementPolicy;
import com.roscap.mw.registry.management.StorageEnum;
import com.roscap.mw.registry.management.StorageManager;

/**
 * A management policy that is based on iterating over elements
 * 
 * @author is.zharinov
 *
 */
abstract class AbstractIteratingPolicy<T, U> implements InstanceManagementPolicy<T,U> {
	private static final Logger logger = LoggerFactory.getLogger(AbstractIteratingPolicy.class);
	
	private final Map<T, Set<U>> manifold/* = new HashMap<>()*/;
	//iterators are not storage-managed
	private final Map<T, AbstractIterator<U>> iterators = new HashMap<>();
	
	AbstractIteratingPolicy(StorageManager arg0) {
		manifold = arg0.createStorage(StorageEnum.INSTANCE);
		invalidate();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.registry.management.InstanceManagementPolicy#offer(java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean offer(T key, U value) {
		Set<U> instances = manifold.containsKey(key) ? manifold.get(key) : new HashSet<>();

		//we always allow at least one element
		if (instances.size() == 0 || allow()) {
			instances.add(value);
			manifold.put(key, instances);
			
			//rebuild iterator for this key
			invalidate(key, instances);
			
			return true;
		}
		else {
			return false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.registry.management.InstanceManagementPolicy#withdraw(java.lang.Object)
	 */
	@Override
	public void withdraw(U value) {
		//streaming replaceAll might not work in certain cases
		//so we don't use it here
		for (T key : manifold.keySet()) {
			Set<U> instances = manifold.get(key);
			
			//remove manifold elements
			if (instances.remove(value)) {
				//rebuild iterators for keys if needs be
				invalidate(key, instances);
			}
			
			manifold.put(key, instances);
		}

/*		
		manifold.replaceAll((p, q) -> {
			//remove manifold elements
			if (q.remove(value)) {
				//rebuild iterators for keys if needs be
				invalidate(p, q);
			}

			return q;
		});
*/
		//remove empty keys
		manifold.entrySet().removeIf(p -> p.getValue() == null || p.getValue().isEmpty());
	}
	
	/**
	 * invalidate value iterator state for given key
	 * 
	 * @param key
	 */
	void invalidate(T key, Set<U> instances) {
		AbstractIterator<U> i = iterators.remove(key);
		
		if (!instances.isEmpty()) {
			List<U> base = new ArrayList<>(instances);
			
			//create a new iterator, possibly retaining state
			AbstractIterator<U> j = (i != null ?
					iterator(base, i) :
						iterator(base));
			
			iterators.put(key, j);
		}

		logger.info("service " + key + " now has " + instances.size() + " instances");
	}
	
	/**
	 * invalidate all
	 */
	void invalidate() {
		manifold.forEach((p, q) -> {
			invalidate(p, q);
		});
	}
	
	/**
	 * create a new iterator
	 * 
	 * @param base
	 * @return
	 */
	abstract AbstractIterator<U> iterator(List<U> base);
	
	/**
	 * create a new iterator based on an existing one (to possibly retain state)
	 * 
	 * @param base
	 * @param i
	 * @return
	 */
	abstract AbstractIterator<U> iterator(List<U> base, AbstractIterator<U> i);

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.registry.management.InstanceManagementPolicy#choose()
	 */
	public U choose(T key) throws NoSuchElementException {
		if (iterators.containsKey(key)) {
			return iterators.get(key).next();
		}
		else {
			throw new NoSuchElementException(key.toString());			
		}
	}
}
