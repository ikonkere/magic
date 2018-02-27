package com.roscap.mw.remoting.config;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Meta-annotation for Spring java config files
 * that enables Magic's client
 * 
 * @author is.zharinov
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Import(MagicClientBootstrapConfiguration.class)
public @interface EnableMagicClient {
	/**
	 * Packages to scan for @Service interfaces to build
	 * remote proxies for. It is recommended to be most specific
	 * to avoid clashes and excessive classpath scans.
	 * 
	 * @return
	 */
	public String packages() default "";
	
	/**
	 * transport bootstrap (usually MOM broker address)
	 * 
	 * @return
	 */
	@Deprecated
	public String bootstrap() default "";
}
