package com.roscap.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.roscap.mw.executor.ServiceExecutor;
import com.roscap.mw.executor.context.ServiceExecutorContext;
import com.roscap.mw.registry.ServiceRegistry;
import com.roscap.mw.registry.context.ServiceRegistryContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
	@ContextConfiguration(classes=ServiceRegistryContext.class),
	@ContextConfiguration(classes=TestServiceContext.class),
	@ContextConfiguration(classes=ServiceExecutorContext.class)
})
public class ServiceExecutorTest {
	@Autowired
	private ServiceExecutor executor;
	
	@Autowired
	@Qualifier("TestServiceClient")
	private TestService testService;

	@Autowired
	@Qualifier("ServiceRegistryClient")
	private ServiceRegistry serviceRegistry;
	
	@Before
	public void before() {
		System.out.println();
	}

	@Test
	public void testExecuteWithLocalProxy() {
		Object o = testService.whatever(6, Arrays.<String>asList("one", "two"));
		assertThat("HERE BE DRAGONS", equalTo(o));
	}
	
	@Test
	public void testExecuteProgrammatic() {
		Object o = executor.execute(URI.create("cdm://Services/Test/TestService3?whatever"), 6, Arrays.<String>asList("three", "four"));
		assertThat("HERE BE DRAGONS", equalTo(o));
	}
}
