package com.roscap.mw.registry.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import com.roscap.cdm.api.CdmUtils;
import com.roscap.mw.registry.ServiceRegistryImpl;
import com.roscap.mw.registry.management.PolicyEnum;
import com.roscap.mw.registry.management.impl.HashMapStorage;
import com.roscap.mw.transport.TransportAdapter;
import com.roscap.util.Tuple;

@RunWith(BlockJUnit4ClassRunner.class)
public class ServiceRegistryTest {
	private ServiceRegistryImpl serviceRegistry;
	private UUID clientId = UUID.randomUUID();

	Object spec1 = "{'CdmUri':'cdm://Services/Test/TestService','Id':'43f3365f-7eb0-4a9a-a3ce-7c8060599382','QualifiedType':'com.roscap.test.TestService'}";
	Object spec2 = "{'CdmUri':'cdm://Services/Test/TestService','Id':'43f3365f-7eb0-4a9a-a3ce-7c8060599383','QualifiedType':'com.roscap.test.TestService'}";
	Object spec3 = "{'CdmUri':'cdm://Services/Test/TestService','Id':'43f3365f-7eb0-4a9a-a3ce-7c8060599384','QualifiedType':'com.roscap.test.TestService'}";
	
	UUID id1 = UUID.fromString("43f3365f-7eb0-4a9a-a3ce-7c8060599382");
	UUID id2 = UUID.fromString("43f3365f-7eb0-4a9a-a3ce-7c8060599383");
	UUID id3 = UUID.fromString("43f3365f-7eb0-4a9a-a3ce-7c8060599384");

	@Before
	public void init() {
		serviceRegistry = new ServiceRegistryImpl();
		serviceRegistry.setStorageManager(new HashMapStorage());
		serviceRegistry.setTransportAdapter(mock(TransportAdapter.class));
	}
	
	private Map<UUID, Map<URI, Tuple<UUID, ZonedDateTime>>> getClients() {
		try {
			Field f = ServiceRegistryImpl.class.getDeclaredField("clients");
			f.setAccessible(true);
			return (Map)f.get(serviceRegistry);
		}
		catch (Throwable t) {
			fail();
			return null;
		}
	}
	
	private Map<UUID, Object> getRegistry() {
		try {
			Field f = ServiceRegistryImpl.class.getDeclaredField("registry");
			f.setAccessible(true);
			return (Map)f.get(serviceRegistry);
		}
		catch (Throwable t) {
			fail();
			return null;
		}
	}
	
	@Test
	public void testExclusive() throws Exception {
		serviceRegistry.setPolicy(PolicyEnum.EXCLUSIVE);
		serviceRegistry.afterPropertiesSet();

		serviceRegistry.register(id1, spec1);
		serviceRegistry.register(id2, spec2);
		
		assertTrue(getRegistry().containsKey(id1));
		assertTrue(!getRegistry().containsKey(id2));
	}
	
	@Test
	public void testRobin() throws Exception {
		serviceRegistry.setPolicy(PolicyEnum.ROUND_ROBIN);
		serviceRegistry.afterPropertiesSet();
		
		serviceRegistry.register(UUID.fromString("43f3365f-7eb0-4a9a-a3ce-7c8060599382"), spec1);
		serviceRegistry.register(UUID.fromString("43f3365f-7eb0-4a9a-a3ce-7c8060599383"), spec2);
		serviceRegistry.register(UUID.fromString("43f3365f-7eb0-4a9a-a3ce-7c8060599384"), spec3);

		assertTrue(getRegistry().keySet().containsAll(Arrays.asList(id1, id2, id3)));

		URI serviceUri = new URI("cdm://Services/Test/TestService");
		Object o1 = serviceRegistry.getSpec(serviceUri);
		Object o2 = serviceRegistry.getSpec(serviceUri);
		Object o3 = serviceRegistry.getSpec(serviceUri);
		Object o4 = serviceRegistry.getSpec(serviceUri);
		Object o5 = serviceRegistry.getSpec(serviceUri);
		Object o6 = serviceRegistry.getSpec(serviceUri);

		UUID realId1 = CdmUtils.searchForId(o1);
		UUID realId2 = CdmUtils.searchForId(o2);
		UUID realId3 = CdmUtils.searchForId(o3);
		UUID realId4 = CdmUtils.searchForId(o4);
		UUID realId5 = CdmUtils.searchForId(o5);
		UUID realId6 = CdmUtils.searchForId(o6);
		List<UUID> ids = Arrays.asList(realId1,realId2,realId3,realId4,realId5,realId6);

		assertThat(2, equalTo(Collections.frequency(ids, id1)));
		assertThat(2, equalTo(Collections.frequency(ids, id2)));
		assertThat(2, equalTo(Collections.frequency(ids, id3)));
	}
	
	@Test
	public void testCache() throws Exception {
		serviceRegistry.setPolicy(PolicyEnum.RANDOM);
		serviceRegistry.setClientStaleTimeout(1l);
		serviceRegistry.afterPropertiesSet();
		
		serviceRegistry.register(UUID.fromString("43f3365f-7eb0-4a9a-a3ce-7c8060599382"), spec1);
		serviceRegistry.register(UUID.fromString("43f3365f-7eb0-4a9a-a3ce-7c8060599383"), spec2);
		serviceRegistry.register(UUID.fromString("43f3365f-7eb0-4a9a-a3ce-7c8060599384"), spec3);

		URI serviceUri = new URI("cdm://Services/Test/TestService");
		Object o = serviceRegistry.getSpec(serviceUri);
		Object realO = serviceRegistry.withClient(clientId, o);

		UUID cachedId = CdmUtils.searchForId(realO);

		Map<UUID, Map<URI, Tuple<UUID, ZonedDateTime>>> clients = getClients();

		assertTrue(clients.containsKey(clientId));
		assertTrue(clients.get(clientId).containsKey(serviceUri));
		assertThat(clients.get(clientId).get(serviceUri).left, equalTo(cachedId));

		assertThat(cachedId,
				equalTo(CdmUtils.searchForId(realO)));

		synchronized(this) {
			wait(1100l);
		}
		
		serviceRegistry.clientChecker();
		
		assertTrue(!clients.containsKey(clientId));
	}
	
	@Test
	public void testUnregister() throws Exception {
		serviceRegistry.setPolicy(PolicyEnum.RANDOM);
		serviceRegistry.afterPropertiesSet();
		
		serviceRegistry.register(UUID.fromString("43f3365f-7eb0-4a9a-a3ce-7c8060599382"), spec1);
		serviceRegistry.register(UUID.fromString("43f3365f-7eb0-4a9a-a3ce-7c8060599383"), spec2);
		serviceRegistry.register(UUID.fromString("43f3365f-7eb0-4a9a-a3ce-7c8060599384"), spec3);

		URI serviceUri = new URI("cdm://Services/Test/TestService");
		Object o = serviceRegistry.getSpec(serviceUri);
		Object realO = serviceRegistry.withClient(clientId, o);

		UUID cachedId = CdmUtils.searchForId(realO);

		Map<UUID, Map<URI, Tuple<UUID, ZonedDateTime>>> clients = getClients();

		assertTrue(clients.get(clientId).containsKey(serviceUri));

		serviceRegistry.unregister(cachedId);
		
		assertTrue(!clients.get(clientId).containsKey(serviceUri));
	}
	
	@Test
	public void testUnregister2() throws Exception {
		serviceRegistry.setPolicy(PolicyEnum.EXCLUSIVE);
		serviceRegistry.afterPropertiesSet();
		
		serviceRegistry.register(UUID.fromString("43f3365f-7eb0-4a9a-a3ce-7c8060599382"), spec1);

		URI serviceUri = new URI("cdm://Services/Test/TestService");
		Object o = serviceRegistry.getSpec(serviceUri);
		Object realO = serviceRegistry.withClient(clientId, o);

		UUID cachedId = CdmUtils.searchForId(realO);

		Map<UUID, Map<URI, Tuple<UUID, ZonedDateTime>>> clients = getClients();

		assertTrue(clients.get(clientId).containsKey(serviceUri));

		serviceRegistry.unregister(cachedId);
		
		assertTrue(!clients.get(clientId).containsKey(serviceUri));
	}
}
