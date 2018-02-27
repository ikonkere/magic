package com.roscap.mw.transport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Constructs transport adapters based on Java's SPI mechanism.
 * Implementations should use following locator file:
 * <code>META-INF/services/com.roscap.mw.transport.TransportAdapter</code>
 * 
 * Note that only a single transport adapter if a given type
 * is allowed per JVM, so {@code TransportFactory#getTransport}
 * will always return the same object.
 * 
 * @author is.zharinov
 *
 */
public final class TransportFactory {
	private static final Map<TransportType, TransportAdapter> loadedAdapters =
			new HashMap<TransportType, TransportAdapter>();
	
	static {
		loadTransport();
	}
	
	private static void loadTransport() {
		ServiceLoader<TransportAdapter> sl = ServiceLoader.load(TransportAdapter.class);
		sl.forEach(p -> {
			if (p.type() == null) {
				Arrays.stream(TransportType.values()).forEach(q -> loadedAdapters.put(q, p));
			}
			else {
				loadedAdapters.put(p.type(), p);
			}
		});
	}
	
	/**
	 * returns an instance of an IMT adapter, not initialized. If multiple
	 * SPIs are available, one will be chosen according to their order
	 * on the classpath.
	 * 
	 * @return
	 */
	public static TransportAdapter getTransport() {
		return getTransport(TransportType.IMT);
	}

	/**
	 * returns an instance of transport adapter for a given type, not initialized.
	 * If multiple SPIs are available, one will be chosen according to their order
	 * on the classpath.
	 * 
	 * @return
	 */
	public static TransportAdapter getTransport(TransportType t) {
		return loadedAdapters.get(t);
	}
}
