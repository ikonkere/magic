package com.roscap.test.context;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.roscap.cdm.api.CdmUtils;
import com.roscap.mw.registry.ServiceRegistry;
import com.roscap.mw.remoting.config.EnableMagic;
import com.roscap.mw.transport.TransportAdapter;
import com.roscap.mw.transport.TransportFactory;

@Configuration
@EnableMagic
public class ServiceRegistryContext {
	@Bean
	public ServiceRegistry serviceRegistry() {
		ServiceRegistry sri = new ServiceRegistry() {
			private final Map<UUID, Object> mock = new HashMap<>();
			
			@Override
			public Object getSpec(URI cdmUri) {
				return mock.values().stream().filter((v) -> cdmUri.equals(CdmUtils.searchForUri(v))).findFirst().get();
			}

			@Override
			public void register(UUID id, Object descriptor) {
				mock.put(id, descriptor);
			}

			@Override
			public void keepAlive(UUID id) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Object getSpec() {
				throw new UnsupportedOperationException();
			}
		};
		
		return sri;
	}
	
	@Bean
	public TransportAdapter transportAdapter() {
		TransportAdapter ta = TransportFactory.getTransport();
		ta.initialize("ngwee:9092");
		return ta;
	}	
}
