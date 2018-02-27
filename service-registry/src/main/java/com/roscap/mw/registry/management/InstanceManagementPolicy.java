package com.roscap.mw.registry.management;

import java.util.NoSuchElementException;

/**
 * A generic class that introduces service
 * choosing policy for scaling.
 * 
 * @author is.zharinov
 *
 */
public interface InstanceManagementPolicy<T, U> {
	/**
	 * offer a pair for this policy to manage
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	boolean offer(T key, U value);
	
	/**
	 * Perform the policy
	 * 
	 * @return
	 * @throws NoSuchElementException when can't choose
	 */
	U choose(T key) throws NoSuchElementException;
	
	/**
	 * indicates whether this policy is ready for offers
	 * 
	 * @return
	 */
	default boolean allow() {
		return true;
	}

	/**
	 * withdraw a value from this policy
	 * 
	 * @param value
	 */
	void withdraw(U value);
}
