package com.roscap.mw.transport.header;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.roscap.mw.transport.correlation.TransportHeader;

/**
 * Denotes that a Magic method should be followed
 * by a header-privy execution of another method on
 * its class, passing the original method return
 * value as well.
 * 
 * Processor method signature should follow header Java types
 * exactly, as well as specifying the original method's return type as
 * the last argument.
 * 
 * @author is.zharinov
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface WithHeaders {
	/**
	 * enumeration of Magic headers that need be
	 * passed to processor method
	 * 
	 * @return
	 */
	TransportHeader[] headers();
	
	/**
	 * processor method name 
	 * @return
	 */
	String method();
}
