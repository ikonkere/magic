package com.roscap.util;

import java.io.Serializable;

/**
 * This is a tuple
 * 
 * @author is.zharinov
 *
 * @param <T> left type
 * @param <U> right type
 */
public class Tuple<T, U> implements Serializable {
	public final T left;
	public final U right;
	
	public Tuple(T arg0, U arg1) {
		left = arg0;
		right = arg1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return left.hashCode() ^ right.hashCode();
	}
}
