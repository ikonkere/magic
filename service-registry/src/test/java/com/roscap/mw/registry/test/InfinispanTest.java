package com.roscap.mw.registry.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class InfinispanTest {
	RemoteCacheManager cacheManager; 
	
	@Before
	public void before() {
		Properties hotrodProps = new Properties();
		hotrodProps.setProperty(ConfigurationProperties.SERVER_LIST, "localhost:11222");
		
		cacheManager = new RemoteCacheManager(new ConfigurationBuilder().withProperties(hotrodProps).build());
	}
	
	@Test
	public void testRemoveAll() {
		Map<String, Collection<String>> localMap = new HashMap<>();
		Map<String, Collection<String>> remoteMap = createStorage("test");
		remoteMap.clear();
		
		populate(localMap);
		populate(remoteMap);
		
		localMap.replaceAll((k, v) -> {
			v.remove("v3");
			System.out.println(k);
			return v;
		});

		remoteMap.replaceAll((k, v) -> {
			v.remove("v3");
			System.out.println(k);
			return v;
		});
		
		System.out.println(localMap);
		System.out.println(remoteMap);
	}

	private <T, U> Map<T, U> createStorage(String arg0) {
		try {
			cacheManager.administration().createCache(arg0, null);
		}
		catch (HotRodClientException hrce) {
			hrce.printStackTrace(System.err);
		}

		return cacheManager.getCache(arg0);
	}
	
	private static void populate(Map<String, Collection<String>> arg0) {
		Collection<String> s = new HashSet<String>();
		s.add("v1");
		s.add("v2");
		s.add("v3");
		s.add("v4");
		s.add("v5");
		s.add("v6");
		arg0.put("k1", s);
		arg0.put("k2", s);
		arg0.put("k3", s);
		arg0.put("k4", s);
		arg0.put("k5", s);
		arg0.put("k6", s);
	}
}
