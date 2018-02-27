package com.roscap.mw.registry.management.impl.jcache;

import java.util.Map;
import java.util.Properties;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;

import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.jcache.remote.JCachingProvider;

import com.roscap.mw.registry.management.StorageEnum;
import com.roscap.mw.registry.management.StorageManager;

public class JCacheStorage implements StorageManager {
	final CacheManager cacheManager;
	
	public JCacheStorage(String arg0) {
		Properties hotrodProps = new Properties();
		hotrodProps.setProperty(ConfigurationProperties.SERVER_LIST, arg0);
		
		cacheManager = Caching.getCachingProvider(JCachingProvider.class.getCanonicalName()).
				getCacheManager(null, null, hotrodProps);
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
		Cache<T, U> c;
		
		try {
			c = cacheManager.createCache(arg0,
					new MutableConfiguration<T, U>());
		}
		catch (CacheException ce) {
			c = cacheManager.getCache(arg0);
		}
		
		if (c instanceof Map) {
			return (Map)c;
		}
		else {
			throw new IllegalStateException("cache not castabe to Map");
		}
	}
}
