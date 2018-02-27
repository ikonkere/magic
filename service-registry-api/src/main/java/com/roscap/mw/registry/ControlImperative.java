package com.roscap.mw.registry;
/**
 * "Imperatives" or commands that services exchange with service registry
 * 
 * @author is.zharinov
 *
 */
public enum ControlImperative {
	/**
	 * indicates that services must refresh,
	 * (re)registering themselves with registry
	 */
	REFRESH,
	/**
	 * indicates that services must acknowledge
	 * if they are alive
	 */
	KEEP_ALIVE
}
