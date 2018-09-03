package com.roscap.context.test;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.roscap.mw.remoting.client.RemoteServiceInvoker;
import com.roscap.mw.remoting.client.RemoteServiceProxyFactoryBean;
import com.roscap.mw.transport.TransportAdapter;
import com.roscap.test.TestService;

@Configuration
public class RemotingClientTestContext {
	@Bean
	public RemoteServiceProxyFactoryBean TestServiceClient(RemoteServiceInvoker arg0) {
		return new RemoteServiceProxyFactoryBean(arg0);
	}

	@Bean
	public RemoteServiceInvoker TestServiceInvoker(TransportAdapter arg0) {
		RemoteServiceInvoker pfb = new RemoteServiceInvoker(TestService.SID);
		pfb.setServiceInterface(TestService.class);
		pfb.setTransportAdapter(arg0);
		return pfb;
	}
	
	@Bean
	public String testBean(@Qualifier("TestServiceClient") TestService testService) {
		return "test";
	}
}
