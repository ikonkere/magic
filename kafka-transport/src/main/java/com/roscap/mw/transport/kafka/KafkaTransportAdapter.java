package com.roscap.mw.transport.kafka;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.MapPropertySource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.event.KafkaEvent;

import com.roscap.mw.context.SharedKafkaContext;
import com.roscap.mw.kafka.spring.DelegatingKafkaListener;
import com.roscap.mw.kafka.spring.event.PartitionsAssignedEvent;
import com.roscap.mw.kafka.spring.event.PartitionsRevokedEvent;
import com.roscap.mw.transport.TransportAdapter;
import com.roscap.mw.transport.endpoint.RoutedEndpoint;
import com.roscap.mw.transport.endpoint.TransportEndpoint;

/**
 * Kafka transport adapter incapsulating everything needed to do remoting
 * via kafka protocol.
 * 
 * Is initialized with a single property - kafka broker address {@code "kafka.bootstrap.address"}.
 * 
 * @author is.zharinov
 *
 */
public class KafkaTransportAdapter implements TransportAdapter,
		ApplicationListener<ApplicationContextEvent> {
	private static final Logger logger = LoggerFactory.getLogger(KafkaTransportAdapter.class); 
	
	/**
	 * this is a static id for internal use to minimize
	 * transport costs
	 * 
	 */
	private final UUID clientId = UUID.randomUUID();
	
	private final ExecutorService executor = Executors.newFixedThreadPool(5);
	
	private boolean initialized = false;
	private boolean propertiesSet = false;
	private boolean available = true;
	
	protected final Object monitor = new Object();
	
	protected ConfigurableApplicationContext kafkaContext;
	private KafkaTemplate<?, Object> kt;
	private KafkaListenerFactory klf;
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportAdapter#clientId()
	 */
	@Override
	public UUID clientId() {
		return this.clientId;
	}
	
	/**
	 * 
	 * @return if this adapter is initialized
	 */
	protected synchronized boolean isInitialized() {
		return this.initialized;
	}
	
	/**
	 * 
	 * internal init-time flag for proper
	 * initialization
	 * 
	 * @param flag
	 */
	synchronized void setInitialized(boolean flag) {
		this.initialized = flag;
	}

	synchronized void setAvailable(boolean flag) {
		available = flag;
	}
	
	synchronized boolean isAvailable() {
		return available;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportAdapter#initialize(java.lang.String[])
	 */
	@Override
	public synchronized void initialize(String... properties) {
		if (!propertiesSet) {
			propertiesSet = true;
			
			Map<String, Object> p = new HashMap<String, Object>();
			MapPropertySource ps = new MapPropertySource("transport.properties", p);
			
			//this was fixed in spring-kafka 1.3 with @KafkaListener#isIdGroup,
			//so we don't care about grop names anymore
			p.put("kafka.group.name", /*inferGroupName(properties)*/ GroupingPolicy.STATIC.groupName());

			p.put("kafka.bootstrap.address", properties[0]);
			p.put("clientId", clientId);
			
			AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
			ctx.getEnvironment().getPropertySources().addFirst(ps);
			ctx.register(SharedKafkaContext.class);
			ctx.addApplicationListener((KafkaEvent e) -> onKafkaEvent(e));
			
			kafkaContext = ctx;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportAdapter#close()
	 */
	@Override
	public synchronized void close() {
		kafkaContext.close();
		executor.shutdown();
		try {
			executor.awaitTermination(5l, TimeUnit.SECONDS);
		}
		catch (InterruptedException ie) {
			logger.warn("graceful shutdown unsuccessfull");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	public synchronized void onApplicationEvent(ApplicationContextEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			if (!isInitialized()) {
				setInitialized(true);

				kafkaContext.refresh();
				
				kt = kafkaContext.getBean(KafkaTemplate.class);
				klf = kafkaContext.getBean(KafkaListenerFactory.class);
				
				logger.info("transport " + clientId + " initialized");
				
				synchronized(monitor) {
					monitor.notifyAll();
				}
			}
		}
		else if (event instanceof ContextClosedEvent) {
			this.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	public void onKafkaEvent(KafkaEvent event) {
		if (event instanceof PartitionsAssignedEvent) {
			if (!isAvailable()) {
				setAvailable(true);

				logger.info("transport " + clientId + " available");

				synchronized(monitor) {
					monitor.notifyAll();
				}
			}
		}
		else if (event instanceof PartitionsRevokedEvent) {
			setAvailable(false);
			logger.info("transport " + clientId + " unavailable");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportAdapter#registerEndpoint(com.roscap.mw.transport.TransportEndpoint)
	 */
	@Override
	public void registerEndpoint(TransportEndpoint<Serializable> endpoint) {
		executor.execute(() -> {
			if (!isInitialized() || !isAvailable()) {
				try {
					synchronized(monitor) {
						monitor.wait();
					}
				}
				catch (InterruptedException ie) {
					logger.error("unexpected exception", ie);
				}
			}
			
			if (endpoint.isRouted()) {
				kafkaContext.getBean(AggregatingRoutingEndpoint.class).add((RoutedEndpoint)endpoint);
			}
			else {
				klf.registerInvokerListener(new DelegatingKafkaListener(endpoint));
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportAdapter#unregisterEndpoint(com.roscap.mw.transport.TransportEndpoint)
	 */
	@Override
	public void unregisterEndpoint(TransportEndpoint<Serializable> endpoint) {
		if (endpoint.isRouted()) {
			kafkaContext.getBean(AggregatingRoutingEndpoint.class).remove((RoutedEndpoint)endpoint);
		}
		else if (klf != null) {
			//it doesn't matter, underlying method only uses endpoint id, which is consistent
			klf.unregisterInvokerListener(new DelegatingKafkaListener(endpoint));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportAdapter#send(java.lang.String, java.lang.Object)
	 */
	@Override
	public void send(String toDestination, Object payload) {
		executor.execute(() -> {
			if (!isInitialized() || !isAvailable()) {
				try {
					synchronized(monitor) {
						monitor.wait();
					}
				}
				catch (InterruptedException ie) {
					logger.error("unexpected exception", ie);
				}
			}
			
			kt.send(toDestination, payload);
		});
	}
	
	/**
	 * 
	 * @param properties
	 * @return
	 */
	@Deprecated
	private static String inferGroupName(String... properties) {
		String g = null;
		String groupName = null;
		
		switch (properties.length) {
			case 3: groupName = properties[2];
			case 2: g = properties[1];
			case 1: break;
			case 0: throw new IllegalArgumentException("kafka bootstrap address must be specified at position 0");
			default: throw new IllegalArgumentException("unsupported number of parameters");
		}
		
		GroupingPolicy gp = GroupingPolicy.resolve(g);
		
		switch (gp) {
			case EXTERNAL: assert properties.length == 3 : "consumer group name must be specified at position 2"; break;
			default: groupName = gp.groupName();
		}
		
		return groupName;
	}

	@Deprecated
	public static enum GroupingPolicy {
		TIMESTAMP(() -> String.valueOf(System.currentTimeMillis())),
		UNIQUE(() -> UUID.randomUUID().toString()),
		EXTERNAL(() -> null),
		STATIC(() -> "magic");
		
		private interface Generator {
			String generate();
		}
		
		private final Generator generator;
		
		private GroupingPolicy(Generator arg0) {
			generator = arg0;
		}
		
		public String groupName() {
			return generator.generate();
		}
		
		public static GroupingPolicy resolve(String value) {
			if (value != null) {
				try {
					return valueOf(value.toUpperCase());
				}
				catch (IllegalArgumentException iae) {
					return STATIC;
				}
			}
			else {
				return STATIC;
			}
		}
	}
}
