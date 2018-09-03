package com.roscap.mw.registry;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;

import com.roscap.cdm.api.CdmUtils;
import com.roscap.mw.registry.management.InstanceManagementPolicy;
import com.roscap.mw.registry.management.PolicyEnum;
import com.roscap.mw.registry.management.StorageEnum;
import com.roscap.mw.registry.management.StorageManager;
import com.roscap.mw.registry.management.impl.ExclusiveElement;
import com.roscap.mw.registry.management.impl.HashMapStorage;
import com.roscap.mw.transport.TransportAdapter;
import com.roscap.mw.transport.TransportFactory;
import com.roscap.mw.transport.correlation.TransportHeader;
import com.roscap.mw.transport.header.WithHeaders;
import com.roscap.util.Tuple;

/**
 * Service registry impl. Is itself a CDM service for convenience
 * 
 * @author is.zharinov
 *
 */
public class ServiceRegistryImpl implements ServiceRegistry, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryImpl.class);

	public static final long CLIENT_STALE = 30*60l;
	
	private long clientStaleTimeout = CLIENT_STALE;
	
	private boolean cacheClients = true;
	private boolean enableInstanceFailover = false;

	//should be noted that adapter is initialized elsewhere
	private TransportAdapter transportAdapter = TransportFactory.getTransport();
	
	private StorageManager storage; 
	
	private PolicyEnum policy;
	
	//registry of serviceUrl -> service instance
	private InstanceManagementPolicy<URI, UUID> instanceManager;

	//registry of service instance -> spec
	private /*final*/ Map<UUID, Object> registry/* = new HashMap<>()*/;
	
	//registry of clientId -> accessed service instances
	private /*final*/ Map<UUID, Map<URI, Tuple<UUID, ZonedDateTime>>> clients/* =
			new HashMap<>()*/;
	
	//registry of service instance -> alive status
	private /*final*/ Map<UUID, Boolean> status/* = new HashMap<>()*/;
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (storage == null) {
			storage = new HashMapStorage();
		}
		
		registry = storage.createStorage(StorageEnum.SPEC);
		clients = storage.createStorage(StorageEnum.CLIENT);
		status = storage.createStorage(StorageEnum.STATUS);
		
		if (instanceManager == null) {
			if (policy != null) {
				try {
					Constructor<? extends InstanceManagementPolicy> c =
							policy.policyClass.getConstructor(StorageManager.class);
					
					this.instanceManager = c.newInstance(storage);
				}
				catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException e) {
					throw new IllegalArgumentException("invalid policy specified");
				}
			}
			else {
				instanceManager = new ExclusiveElement<>(storage);
			}
		}
		
		logger.info("refreshing registry");
		transportAdapter.send(INFRASTRUCTURE_CHANNEL, ControlImperative.REFRESH);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.registry.ServiceRegistry#register(java.util.UUID, java.lang.Object)
	 */
	@Override
	public synchronized void register(UUID id, Object descriptor) {
		if (!registry.containsKey(id)) {
			//new instance
			URI serviceUri = CdmUtils.searchForUri(descriptor);
			
			if (instanceManager.offer(serviceUri, id)) {
				//policy accepted our offering
				registry.put(id, descriptor);
				keepAlive(id);

				logger.info("registered service: " + descriptor.toString());
			}
			else {
				logger.warn("registration attempt violates policy, ignored: " + id);
			}
		}
		else {
			//we don't allow this
			logger.warn("attempted to re-register a service, ignored: " + id);
		}
	}

	/**
	 * unregister a service from this registry
	 * 
	 * @param serviceId
	 */
	public synchronized void unregister(UUID serviceId) {
		if (registry.containsKey(serviceId)) {
			//remove instance
			instanceManager.withdraw(serviceId);

			if (cacheClients) {
				//remove service from client cache
				Map<UUID, Set<UUID>> purged = purgeClientCache(v -> serviceId.equals(v.left));
	
				//TODO: need not invalidate if no instances are available
				if (enableInstanceFailover) {
					//invalidate affected clients
					invalidate(purged);
				}
			}
			
			//remove from registry
			registry.remove(serviceId);

			logger.info("unregistered service: " + serviceId);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.registry.ServiceRegistry#getSpec(java.util.UUID)
	 */
	@Override
	@WithHeaders(headers=TransportHeader.CLIENT, method="withClient")
	public synchronized Object getSpec(URI cdmUri) {
		try {
			return registry.get(instanceManager.choose(cdmUri));
		}
		catch (NoSuchElementException nsee) {
			throw new NoSuchElementException("service not registered " + cdmUri);
		}
	}
	
	/**
	 * Responsible for enforcing sticky clients policy
	 * 
	 * @param clientId
	 * @param spec
	 * @return
	 */
	public synchronized Object withClient(UUID clientId, Object spec) {
		if (!cacheClients || spec == null) {
			//don't need to cache
			return spec;
		}
		else if (clients.containsKey(clientId)) {
			//existing client
			URI serviceUri = CdmUtils.searchForUri(spec);
			UUID serviceId = CdmUtils.searchForId(spec);
			Object result = spec;
			
			Map<URI, Tuple<UUID, ZonedDateTime>> clientCache =
					clients.get(clientId);
			
			if (clientCache.containsKey(serviceUri)) {
				//we need to return cached service and ignore whatever we
				//initially found
				serviceId = clientCache.get(serviceUri).left;
				result = registry.get(serviceId);

				logger.info("client: " + clientId + ", returning cached service: " + serviceId);
			}
			else {
				//we need to cache the service 
				logger.info("client: " + clientId + ", caching service: " + serviceId);
			}
			
			//update cache, potentially replacing existing entry
			clientCache.put(serviceUri, new Tuple<>(serviceId, ZonedDateTime.now()));
			clients.put(clientId, clientCache);
			
			return result;
		}
		else {
			//this is a new client
			clients.put(clientId, new HashMap<>());
			return withClient(clientId, spec);
		}
	}
	
	/**
	 * purge service from cache based on a determinant function
	 * 
	 * @param determinant
	 */
	private synchronized Map<UUID, Set<UUID>> purgeClientCache(Function<Tuple<UUID, ZonedDateTime>, Boolean> determinant) {
		//map of clientId -> set of instanceId
		Map<UUID, Set<UUID>> purged = new HashMap<>();
				
		//streaming replaceAll might not work in certain cases
		//so we don't use it here
		for (UUID key : clients.keySet()) {
			Map<URI, Tuple<UUID, ZonedDateTime>> clientCache = clients.get(key);
			
			//purge services from cache
			if (clientCache != null) {
				purged.put(key, new HashSet<>());
				
				for (URI key2 : clientCache.keySet()) {
					if (determinant.apply(clientCache.get(key2))) {
						purged.get(key).add(clientCache.get(key2).left);
						clientCache.remove(key2);
					}
				}
				
				if (!purged.get(key).isEmpty()) {
					clients.put(key, clientCache);
					logger.debug(String.format("cleaned stale client %s", key));
				}
			}

			/*
			if (clientCache != null &&
					clientCache.entrySet().removeIf(e -> determinant.apply(e.getValue()))) {
				clients.put(key, clientCache);
				logger.debug(String.format("removed stale client %s", key));
			}
			*/		
		}
		
/*		
		clients.replaceAll((p, q) -> {
			//purge services from cache
			if (q != null && q.entrySet().removeIf(e -> determinant.apply(e.getValue()))) {
				logger.debug(String.format("removed stale client %s", p));
			}
			
			return q;
		});
*/		
		return purged;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.registry.ServiceRegistry#getSpec()
	 */
	@Override
	public synchronized Object getSpec() {
		return CdmUtils.assembleDescriptors(registry.values());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.registry.ServiceRegistry#keepAlive(java.util.UUID)
	 */
	@Override
	public synchronized void keepAlive(UUID id) {
		if (registry.containsKey(id)) {
			status.put(id, true);
		
			logger.info("service alive: " + id);
		}
		else {
			//this means that a service gracefully woke from the dead after it was deemed as such
			//we do nothing in this case, service must be restarted (not good probably?)
			logger.warn("service rose from the dead, but was already removed: " + id);
		}
	}
	
	/**
	 * notifies clients to refresh service instances
	 * 
	 * @param purged
	 */
	public void invalidate(Map<UUID, Set<UUID>> purged) {
		purged.entrySet().forEach(e -> {
			e.getValue().forEach(v -> {
				ClientControlImperative ri =
						new ClientControlImperative(ControlImperative.REFRESH);
				ri.addHeader(TransportHeader.CLIENT,
						transportAdapter.clientId());
				ri.addHeader(TransportHeader.CORRELATION, v);

				transportAdapter.send(e.getKey().toString(), ri);				
			});
		});
	}
	
	/**
	 * periodically pings services to see if they are alive
	 */
	@Scheduled(initialDelay=10000l, fixedDelay=60000l)
	public synchronized void keepAlive() {
		logger.info("invalidating live services");
		
		status.replaceAll((k, v) -> false);
		
		transportAdapter.send(INFRASTRUCTURE_CHANNEL, ControlImperative.KEEP_ALIVE);
	}
	
	/**
	 * periodically checks the registry to see
	 * if some services died and must be removed
	 */
	@Scheduled(initialDelay=40000l, fixedDelay=60000l)
	public synchronized void aliveChecker() {
		status.forEach((k, v) -> {
				if (!v) {
					unregister(k);
					logger.warn("removed dead service: " + k);
				}
			}
		);
		
		status.entrySet().removeIf(p -> !p.getValue());
	}
	
	/**
	 * periodically checks (every half-an-hour) if client cache must be purged
	 */
	@Scheduled(initialDelay=40000l, fixedDelay=30*60000l)
	public synchronized void clientChecker() {
		if (cacheClients) {
			logger.info("purging cache for stale clients");		
			ZonedDateTime t = ZonedDateTime.now();
			
			//remove cached services that weren't accessed recently
			purgeClientCache(e -> e.right.until(t, ChronoUnit.SECONDS) >= clientStaleTimeout);
			
			//remove empty cache entries
			clients.entrySet().removeIf(p -> p.getValue() == null || p.getValue().isEmpty());
		}
	}
	
	/**
	 * transport to use for sending control imperatives
	 * @param kt
	 */
	public void setTransportAdapter(TransportAdapter kt) {
		this.transportAdapter = kt;
	}

	/**
	 * Storage manager to use. HashMapStorage by default.
	 * 
	 * @param arg0
	 */
	public void setStorageManager(StorageManager arg0) {
		this.storage = arg0;
	}
	
	/**
	 * policy to use for instance management
	 * Exclusive by default.
	 * 
	 * @param arg0
	 */
	public void setPolicy(InstanceManagementPolicy<URI, UUID> arg0) {
		this.instanceManager = arg0;
	}

	/**
	 * policy to use for instance management.
	 * Exclusive by default.
	 * 
	 * @param policy
	 */
	public void setPolicy(PolicyEnum arg0) {
		this.policy = arg0;
	}
	
	/**
	 * timeout to use to clear client cache, in seconds.
	 * 30 minutes by default.
	 *  
	 * @param arg0
	 */
	public void setClientStaleTimeout(long arg0) {
		this.clientStaleTimeout = arg0;
	}
	
	/**
	 * indicates whether to use client caching strategy.
	 * True by default
	 * 
	 * @param arg0
	 */
	public void setCacheClients(boolean arg0) {
		this.cacheClients = arg0;
	}
	
	/**
	 * indicates whether to enable service instance rebalance feature
	 * (as in - when an instance fails, clients that used this instance
	 * will be notified to invalidate and receive a new instance reference).
	 * 
	 * Experimental feature.
	 * 
	 * False by default
	 * 
	 * @param arg0
	 */
	public void setEnableInstanceFailover(boolean arg0) {
		this.enableInstanceFailover = arg0;
	}
}
