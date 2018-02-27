package com.roscap.context.test;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.roscap.mw.remoting.exporter.TransportEndpointServiceExporter;
import com.roscap.mw.remoting.spring.TransportEndpointBeanPostProcessor;
import com.roscap.mw.transport.TransportAdapter;
import com.roscap.test.TestService;

@Configuration
@ImportResource("classpath:remoting-context-test.xml")
public class RemotingTestContext {
	@Bean
	public TransportEndpointServiceExporter endpoint(TestService arg0, TransportAdapter arg1) {
		TransportEndpointServiceExporter e = new TransportEndpointServiceExporter(TestService.SID);
		e.setService(arg0);
		e.setServiceInterface(TestService.class);
		e.setTransportAdapter(arg1);
		return e;
	}

	@Bean
	public BeanPostProcessor transportEndpointProcessor(TransportAdapter arg0) {
		TransportEndpointBeanPostProcessor bpp = new TransportEndpointBeanPostProcessor();
		bpp.setTransportAdapter(arg0);
		return bpp;
	}
}
