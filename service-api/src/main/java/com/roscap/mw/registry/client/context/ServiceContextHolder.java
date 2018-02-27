package com.roscap.mw.registry.client.context;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import com.roscap.mw.remoting.config.RemoteServiceFactoryPostProcessor;

/**
 * 
 * A simple Spring context holder for local services
 * that serves as SoC realization
 * 
 * @author is.zharinov
 *
 */
public class ServiceContextHolder implements InitializingBean, ApplicationContextAware, ApplicationListener<ApplicationContextEvent> {
	private final ConfigurableApplicationContext serviceContext;
	private ConfigurableApplicationContext mainContext;

	/**
	 * factory method for incapsulating service contexts without exposing too much
	 * of internal logic
	 * 
	 * @param contextClass
	 * @return
	 */
	public static ServiceContextHolder createServiceContext(Class<?>... contextClass) {
		if (contextClass != null && contextClass.length > 0) {
			AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
			ctx.register(contextClass);
			return new ServiceContextHolder(ctx);
		}
		else {
			throw new IllegalStateException("service context must not be null");
		}

	}

	ServiceContextHolder(ConfigurableApplicationContext arg0) {
		serviceContext = arg0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		mainContext = (ConfigurableApplicationContext)applicationContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (serviceContext != null) {
			//this means we have a separate service context
			//and we need to process its beans to expose services
			RemoteServiceFactoryPostProcessor bfpp = new RemoteServiceFactoryPostProcessor();
			bfpp.setTargetContext(mainContext);
			serviceContext.addBeanFactoryPostProcessor(bfpp);
			//due to internal logic peculiarities we must refresh
			//the context before the main one
			serviceContext.refresh();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		if (event instanceof ContextRefreshedEvent) {
//			serviceContext.refresh();
		}
		else if (event instanceof ContextClosedEvent) {
			serviceContext.close();
		}
	}
}