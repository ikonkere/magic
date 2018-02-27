package com.roscap.mw.registry.management.impl.infinispan;

import java.util.Map;
import java.util.Properties;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;

import com.roscap.mw.registry.management.StorageEnum;
import com.roscap.mw.registry.management.StorageManager;

/**
 * Infinispan-based remote storage that creates
 * Map-based caches
 * 
 * @author is.zharinov
 *
 */
public class InfinispanStorage implements StorageManager {
	final RemoteCacheManager cacheManager; 
	
	public InfinispanStorage(String arg0) {
		Properties hotrodProps = new Properties();
		hotrodProps.setProperty(ConfigurationProperties.SERVER_LIST, arg0);
		
		cacheManager = new RemoteCacheManager(new ConfigurationBuilder().withProperties(hotrodProps).build());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.registry.management.StorageManager#createStorage(com.roscap.mw.registry.management.StorageEnum)
	 */
	@Override
	public <T, U> Map<T, U> createStorage(StorageEnum arg0) {
		return createStorage(arg0.name().toLowerCase());
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.registry.management.StorageManager#createStorage(java.lang.String)
	 */
	@Override
	public <T, U> Map<T, U> createStorage(String arg0) {
		try {
			cacheManager.administration().createCache(arg0, null);
		}
		catch (HotRodClientException hrce) {
			hrce.printStackTrace(System.err);
		}

		return cacheManager.getCache(arg0);
	}
}
