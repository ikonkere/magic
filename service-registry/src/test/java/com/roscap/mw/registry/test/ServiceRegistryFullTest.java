package com.roscap.mw.registry.test;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.roscap.mw.reflection.Argument;
import com.roscap.mw.registry.ControlImperative;
import com.roscap.mw.registry.ServiceRegistry;
import com.roscap.mw.remoting.client.RemoteServiceInvoker;
import com.roscap.mw.transport.TransportAdapter;
import com.roscap.mw.transport.endpoint.TransportEndpoint;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=ServiceRegistryTestContext.class)
public class ServiceRegistryFullTest {
	static String spec1 = "{'CdmUri':'cdm://Services/Test/TestService5','Id':'43f3365f-7eb0-4a9a-a3ce-7c8060599385','QualifiedType':'com.roscap.test.TestService'}";
	static UUID id1 = UUID.fromString("43f3365f-7eb0-4a9a-a3ce-7c8060599385");
	static URI uri1 = URI.create("cdm://Services/Test/TestService5");

	private static class TestEndpoint implements TransportEndpoint<ControlImperative> {
		private RemoteServiceInvoker i;
		private final UUID id = UUID.randomUUID();

		TestEndpoint(RemoteServiceInvoker arg0) {
			i = arg0;
		}
		
		@Override
		public void receive(String fromDestination, ControlImperative payload) {
			try {
				i.invoke("keepAlive", Argument.<UUID>create(UUID.class, id1));
			}
			catch (Throwable t) {
				System.err.println("fail");
			}
		}
		
		@Override
		public UUID id() {
			return id;
		}
		
		@Override
		public String destination() {
			return ServiceRegistry.INFRASTRUCTURE_CHANNEL;
		}
	}	
	
	@Autowired
	private TransportAdapter ta;

	private RemoteServiceInvoker i;
	
	private TransportEndpoint e;
	
	@SuppressWarnings("unchecked")
	@Before
	public void beforeTest() {
		i = new RemoteServiceInvoker(ServiceRegistry.SID);
		i.setTransportAdapter(ta);
		i.afterPropertiesSet();
		
		e = new TestEndpoint(i);
		
		ta.registerEndpoint(e);
	}
	
	@SuppressWarnings("unchecked")
	@After
	public void afterTest() {
		ta.unregisterEndpoint(e);
	}
	
	@Test
	public void testRegisterGetSpec() throws Throwable {
		i.invoke("register", Argument.<UUID>create(UUID.class, id1), Argument.<Object>create(Object.class, spec1));
		
		//register isn't supposed to be called at runtime, it's asynch and takes time,
		//hence - we need to wait for some reasonable time, but not too much
		synchronized(this) {wait(100l);}
		
		Object spec = i.invoke("getSpec", Object.class, Argument.<URI>create(URI.class, uri1));
		
		assertEquals(spec1, spec);
	}
}
