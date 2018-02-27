package com.roscap.mw.service.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import com.roscap.mw.registry.client.context.ServiceRegistryClientContext;
import com.roscap.mw.remoting.config.EnableMagic;
import com.roscap.mw.service.config.ServiceBootstrapConfigurer;
import com.roscap.mw.service.config.ServiceContextImportSelector;
import com.roscap.mw.service.config.TransportContext;

/**
 * Meta-annotation for Spring java config files
 * that enables CDM service support.
 * That is - service export, Service Registry client
 * 
 * @author is.zharinov
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@EnableMagic
@Import({ServiceBootstrapConfigurer.class, ServiceContextImportSelector.class,
	TransportContext.class, ServiceRegistryClientContext.class})
@ImportResource("classpath:jmx-context.xml")
public @interface ConfigureServices {
	
	/**
	 * transport bootstrap (usually MOM broker address)
	 * 
	 * @return
	 */
	public String bootstrap() default "";
	
	/**
	 * Spring context classes containing service definitions
	 * that will be instantiated as a separate application context.
	 * @return
	 */
	public Class<?>[] services() default {};

}
