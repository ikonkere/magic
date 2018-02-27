package com.roscap.cdm.api;

import java.util.UUID;

/**
 * Represents CDM service id generation
 * strategies
 * 
 * @author is.zharinov
 *
 */
public interface IdGenerator {
	/**
	 * generate a next id
	 * 
	 * @return
	 */
	public UUID generateId();
}
