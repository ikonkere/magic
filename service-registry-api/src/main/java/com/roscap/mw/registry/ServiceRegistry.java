package com.roscap.mw.registry;

import java.net.URI;
import java.util.UUID;

import com.roscap.cdm.api.IdGenerator;
import com.roscap.cdm.api.annotation.Service;

/**
 * Centralized service registry that keeps track
 * of all currently running CDM services and
 * provides service specs
 * 
 * @author is.zharinov
 *
 */
@Service(idGeneratorClass=ServiceRegistry.StaticSidGenerator.class,
	id="0a398127-f403-4cce-80e0-1c754901736c",
	uri="cdm://Services/Infrastructure/ServiceRegistry")
public interface ServiceRegistry {
	static final UUID SID = UUID.fromString("0a398127-f403-4cce-80e0-1c754901736c");
	static String INFRASTRUCTURE_CHANNEL = "service.governance";

	/**
	 * default id generator for service registry
	 * returns a static id.
	 * 
	 * @author is.zharinov
	 *
	 */
	static final class StaticSidGenerator implements IdGenerator {
		/*
		 * (non-Javadoc)
		 * @see com.roscap.cdm.api.IdGenerator#generateId()
		 */
		@Override
		public final UUID generateId() {
			return SID;
		}
	}
	
	/**
	 * register a service (intended to be remotely called)
	 * @internal
	 * @param id service id
	 * @param descriptor service spec
	 */
	public void register(UUID id, Object descriptor);
	
	/**
	 * gracefully unregister a service
	 * 
	 * @param id service id
	 */
	public void unregister(UUID id);
	
	/**
	 * mark the service as alive (intended to be remotely called)
	 * @internal
	 * @param id service id
	 */
	public void keepAlive(UUID id);

	/**
	 * get a service's spec
	 * @param cdmUri CDM URI identifying the spec
	 * @return null if no service with {@code id} is currently alive, otherwise it's JSON spec as string
	 */
	public Object getSpec(URI cdmUri);
	
	/**
	 * get all registered specs
	 * 
	 * @return
	 */
	public Object getSpec();
}
