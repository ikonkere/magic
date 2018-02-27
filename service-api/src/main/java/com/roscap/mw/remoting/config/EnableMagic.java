package com.roscap.mw.remoting.config;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Meta-annotation for Spring java config files
 * that enables Magic - framework for remote service
 * invocation.
 * 
 * @author is.zharinov
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Import(MagicBootstrapConfiguration.class)
public @interface EnableMagic {
	
	/**
	 * transport bootstrap (usually MOM broker address)
	 * 
	 * @return
	 */
	@Deprecated
	public String bootstrap() default "";

}
