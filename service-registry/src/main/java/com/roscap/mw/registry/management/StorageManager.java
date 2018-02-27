package com.roscap.mw.registry.management;

import java.util.Map;

/**
 * Storage manager abstraction for Service Registry
 * 
 * @author is.zharinov
 *
 */
public interface StorageManager {
	/**
	 * create a storage for given kind
	 * 
	 * @param storageType
	 * @return
	 */
	public <T, U> Map<T, U> createStorage(StorageEnum storageType);

	/**
	 * create a storage for given name
	 * 
	 * @param storageName
	 * @return
	 */
	public <T, U> Map<T, U> createStorage(String storageName);
}
