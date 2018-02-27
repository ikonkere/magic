package com.roscap.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.roscap.mw.registry.client.ServiceInstanceFactory;
import com.roscap.test.context.ServiceRegistryClientTestContext;
import com.roscap.test.context.ServiceRegistryContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
	@ContextConfiguration(classes=ServiceRegistryContext.class),
	@ContextConfiguration(classes=ServiceRegistryClientTestContext.class)
})
public class SeparatedServiceContextFactoryTest {
	@Autowired
	private ServiceInstanceFactory serviceFactory;
	
	@Test
	public void testServiceFactory() throws URISyntaxException {
		URI uri = new URI("cdm://Services/Test/TestService2");
		
		Object o = serviceFactory.findByUri(uri);
		
		assertTrue(TestService.class.isAssignableFrom(o.getClass()));
	}
	
	@Test
	public void testServiceFactory2() throws URISyntaxException {
		URI uri = new URI("cdm://Services/Test/TestService");
		
		try {
			serviceFactory.findByUri(uri);
			fail();
		}
		catch (NoSuchElementException nsee) {
			
		}
	}

}
