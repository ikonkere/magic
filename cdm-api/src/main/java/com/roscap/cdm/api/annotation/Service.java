package com.roscap.cdm.api.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.roscap.cdm.api.IdGenerator;
import com.roscap.cdm.impl.RandomIdGenerator;

/**
 * Annotate an interface to specify that
 * it represents a CDM service
 * 
 * @author is.zharinov
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Service {
	/**
	 * Instance id generator class, set to override default behaviour
	 * 
	 * @see com.roscap.cdm.api.IdGenerator;
	 * @return
	 */
	public Class<? extends IdGenerator> idGeneratorClass() default RandomIdGenerator.class;
	
	/**
	 * explicit instance id if needed. Should be used with care
	 * as it will disable any instance management policies
	 * 
	 * @return
	 */
	public String id() default "";
	
	/**
	 * CDM URI of service
	 * 
	 * @return
	 */
	public String uri();
}
