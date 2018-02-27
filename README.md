# Building service meshes with sorcery 

This is a brief howto on creating service meshes with Magic(tm) Middleware.

To discover exactly why you must configure or use something in a certain way, have a look at JavaDocs
which i've painstakingly and comprehensibly compiled. 

## Requirements
* Java 8+
* Kafka 0.11+ 
* Spring Framework [4.3.x] (i am not sure it works on 5.x and i couldn't care less ;P)
* Maven 3.3 (feel free to use your favourite build tool though)

## Service mesh

It is an ecosystem of (micro)services that communicate with each other for whatever reasons. The mesh must be available, scalable and performant,
and location-oblivious - that is the responsibility of Magic(tm). Everything else is out of scope (stateless vs stateful, partitioning, eventual consistency, etc).

Middleware is built on top of a "transport" abstraction. Main focus was of course Kafka, but other transports can be easily implemented.
Currently an extra one is "local" transport when you execute everything in a single JVM (useful for testing).

## Service mesh node

A service mesh node is a (micro)service written in Java that complies to CDM.

### Coding

Microservice itself (use whatever method signatures you like as long as it is CDM-compliant).

	package com.mystuff;
	
	import com.roscap.cdm.api.annotation.Service;
	
	@Service(uri="cdm://Services/MyDomain/MyService")
	public interface MyService {
		public String myMethod();
	}

SF config for the microservice (set kafka bootstrap address using your favourite value injection style).

	package com.mystuff;
	
	import org.springframework.context.annotation.Bean;
	import org.springframework.context.annotation.Configuration;
	
	import com.roscap.mw.service.annotation.ConfigureServices;
	
	@Configuration
	@ConfigureServices(bootstrap="${kafka.bootstrap.address}")
	public class ServiceWebContext {
		@Bean
		public MyService testService() {
			return new MyServiceImpl();
		}
	}

### Configuring

Use these property files to adjust your node behaviour (not much available though). 

> classpath:transport.properties

	#method invocation concurrency   
	kafka.concurrent.listeners=

for non-j2ee environments e.g. spring-boot
> classpath:META-INF/services/META-INF/services/javax.json.spi.JsonProvider

	com.github.pgelinas.jackson.javax.json.spi.JacksonProvider

### Building

I'm not sure webmvc is even required, because this dependency list is out-of-context.

	<dependencies>
		<dependency>
			<groupId>com.roscap.service</groupId>
			<artifactId>service-api</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
		<dependency>
			<groupId>com.roscap.mw.transport</groupId>
			<artifactId>kafka-transport</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-webmvc</artifactId>
			<version>${version.spring}</version>
		</dependency>
		<!-- for non-j2ee environments, e.g. spring-boot -->
		<dependency>
			<groupId>com.github.pgelinas</groupId>
			<artifactId>jackson-javax-json</artifactId>
			<version>0.9.0</version>
		</dependency>
	</dependencies>

### Running

Use your favourite microprofile runtime: wildfly, liberty, spring-boot, etc.

## Service registry

Is itself your service mesh node and a CDM service at it. Used for discovery and housekeeping (see JavaDoc for details), istio-light if you want.

Supports external in-memory storages, but for now only one of them - Infinispan - implements Java8 streams.

### Coding

SF config for the registry is rather simple.

	package com.mystuff;
	
	import org.springframework.context.annotation.Configuration;
	import org.springframework.context.annotation.Import;
	
	import com.roscap.mw.registry.context.ServiceRegistryContext;
	
	@Configuration
	@Import(ServiceRegistryContext.class)
	public class ServiceRegistryWebContext {
	
	}

### Configuring

Use these property files to adjust your registry behaviour. 

> classpath:service-registry.properties

	#Transport address (e.g. kafka broker)
	bootstrap.address=
	#managed services instance policy, enum
	management.policy=
	#to cache or not to cache clients, boolean
	client.cache=
	#service clients caching TTL, ms 
	client.timeout=

> classpath:transport.properties

	#registry invocation concurrency   
	kafka.concurrent.listeners=

### Building

I'm not sure webmvc is even required, because this dependency list is out-of-context.

	<dependencies>
		<dependency>
			<groupId>com.roscap.mw</groupId>
			<artifactId>service-registry</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
		<dependency>
			<groupId>com.roscap.mw.transport</groupId>
			<artifactId>kafka-transport</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-webmvc</artifactId>
			<version>${version.spring}</version>
		</dependency>
		<dependency>
			<groupId>javax.json</groupId>
			<artifactId>javax.json-api</artifactId>
			<version>1.0</version>
			<scope>provided</scope>
		</dependency>
		<!-- for non-j2ee environments, e.g. spring-boot -->
		<dependency>
			<groupId>com.github.pgelinas</groupId>
			<artifactId>jackson-javax-json</artifactId>
			<version>0.9.0</version>
		</dependency>
	</dependencies>
	
### Running

Again, use your favourite microprofile runtime: wildfly, liberty, spring-boot, etc.

## Service client

Now that you built your mesh, you need to access it and start using your nodes.

This part is less developer-friendly.

### Coding

First you must configure the mediator (service executor). Yeah, i know, configuring transport explicitly - not cool.

	package com.mystuff;
	
	import org.springframework.context.annotation.Bean;
	import org.springframework.context.annotation.Configuration;
	import org.springframework.context.annotation.Import;
	
	import com.roscap.mw.executor.context.ServiceExecutorContext;
	import com.roscap.mw.transport.TransportAdapter;
	import com.roscap.mw.transport.TransportFactory;
	
	@Configuration
	@Import(ServiceExecutorContext.class)
	public class ServiceClientContext {
		
		@Bean
		public TransportAdapter transportAdapter() {
			TransportAdapter ta = TransportFactory.getTransport();
			ta.initialize("${kafka.bootstrap.address}");
			return ta;
		}
	}

Now, here's the tricky part. You can use two different approaches for invoking CDM services from your mesh.

**1. Using local proxies.** Remember CDM interfaces declared earlier with `@Service`?
You can package it in a jar and drop to your client's classpath, Magic(tm) will do the rest and you can start using it
as if it was a local service "somewhere" in your SF context.

	package com.mystuff;
	
	import java.util.Arrays;
	
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.beans.factory.annotation.Qualifier;
	
	import org.junit.Test;
	import org.junit.runner.RunWith;
	import com.roscap.mw.executor.ServiceExecutor;
	import com.roscap.mw.executor.context.ServiceExecutorContext;
	import com.roscap.mw.registry.ServiceRegistry;
	import com.roscap.mw.registry.context.ServiceRegistryContext;
	import org.springframework.test.context.ContextConfiguration;
	import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
	
	@RunWith(SpringJUnit4ClassRunner.class)
	@ContextConfiguration(classes=ServiceClientContext.class)
	public class ServiceClient {
		@Autowired
		@Qualifier("MyServiceClient")
		private MyService myService;
	
		@Test
		public void testExecuteWithLocalProxy() {
			String o = myService.myMethod(/* your args here*/);
		}
	}

This is not unlike RMI, but without all the rubbish that it is.	

**2. With programmatic invocations,** using the mediator explicitly. In this case you address the CDM URI
of your service, specifying the method you want to invoke (see JavaDocs for deeper explanation).

	package com.mystuff;
	
	import java.net.URI;
	
	import org.junit.Test;
	import org.junit.runner.RunWith;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.beans.factory.annotation.Qualifier;
	import org.springframework.test.context.ContextConfiguration;
	import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
	
	import com.roscap.mw.executor.ServiceExecutor;
	import com.roscap.mw.executor.context.ServiceExecutorContext;
	import com.roscap.mw.registry.ServiceRegistry;
	import com.roscap.mw.registry.context.ServiceRegistryContext;
	
	@RunWith(SpringJUnit4ClassRunner.class)
	@ContextConfiguration(classes=ServiceClientContext.class)
	public class ServiceExecutorTest {
		@Autowired
		private ServiceExecutor executor;
		
		@Test
		public void testExecuteProgrammatic() {
			Object o = executor.execute(URI.create("cdm://Services/MyDomain/MyService?myMethod"),
				/* your args here*/);
		}
	}

### Building

	<dependencies>
		<dependency>
			<groupId>com.roscap.mw</groupId>
			<artifactId>service-executor</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
		<dependency>
			<groupId>com.roscap.mw.transport</groupId>
			<artifactId>kafka-transport</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-webmvc</artifactId>
			<version>${version.spring}</version>
		</dependency>
		<!-- for non-j2ee -->
		<dependency>
			<groupId>com.github.pgelinas</groupId>
			<artifactId>jackson-javax-json</artifactId>
			<version>0.9.0</version>
		</dependency>
	</dependencies>

### Running

Use your favourite runtime.