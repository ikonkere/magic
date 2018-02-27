package com.roscap.context.test;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.roscap.mw.remoting.client.RemoteServiceProxyFactoryBean;
import com.roscap.mw.transport.TransportAdapter;
import com.roscap.test.TestService;

@Configuration
public class RemotingClientTestContext {
	@Bean
	public RemoteServiceProxyFactoryBean TestServiceClient(TransportAdapter arg0) {
		RemoteServiceProxyFactoryBean pfb = new RemoteServiceProxyFactoryBean(TestService.SID);
		pfb.setServiceInterface(TestService.class);
		pfb.setTransportAdapter(arg0);
		return pfb;
	}
	
	@Bean
	public String testBean(@Qualifier("TestServiceClient") TestService testService) {
		return "test";
	}
}
