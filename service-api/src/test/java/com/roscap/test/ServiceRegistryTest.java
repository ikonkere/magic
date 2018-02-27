package com.roscap.test;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.roscap.cdm.api.annotation.Service;
import com.roscap.mw.registry.ServiceRegistry;
import com.roscap.test.context.ServiceRegistryContext;
import com.roscap.test.context.ServiceTestContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
	@ContextConfiguration(classes=ServiceRegistryContext.class),
	@ContextConfiguration(classes=ServiceTestContext.class)
})
public class ServiceRegistryTest {
	@Autowired
	@Qualifier("ServiceRegistryClient")
	private ServiceRegistry serviceRegistry;

	@Before
	public void init() throws InterruptedException {
		//register might not yet have happened just yet, it's asynch and takes time,
		//hence - we need to wait for some reasonable time, but not too much
		synchronized(this) {wait(100l);}
	}
	
	@Test
	public void testRegistry() throws URISyntaxException {
		URI serviceUri = new URI(TestService.class.getAnnotation(Service.class).uri());
		Object o = serviceRegistry.getSpec(serviceUri);
		
		JsonObject json = Json.createReader(new StringReader(o.toString())).readObject();
		
		System.out.println(json.toString());
	}
}
