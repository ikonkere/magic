package com.roscap.mw.registry.management.impl;

import java.util.HashMap;
import java.util.Map;

import com.roscap.mw.registry.management.StorageEnum;
import com.roscap.mw.registry.management.StorageManager;

/**
 * Simple in-memory storage
 * 
 * @author is.zharinov
 *
 */
public class HashMapStorage implements StorageManager {
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.registry.management.StorageManager#createStorage(com.roscap.mw.registry.management.StorageEnum)
	 */
	@Override
	public <T, U> Map<T, U> createStorage(StorageEnum arg0) {
		return createStorage(arg0.name());
	}

	@Override
	public <T, U> Map<T, U> createStorage(String storageName) {
		//we care not of the storage kind in this case
		//as it's non-persistent
		return new HashMap<>();
	}
}
