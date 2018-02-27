package com.roscap.transport.kafka.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import com.roscap.mw.kafka.spring.KafkaTopicFactory;
import com.roscap.mw.transport.TransportAdapter;
import com.roscap.mw.transport.correlation.TransportHeader;
import com.roscap.mw.transport.endpoint.RoutedEndpoint;
import com.roscap.mw.transport.endpoint.TransportEndpoint;
import com.roscap.mw.transport.header.HeaderContainer;
import com.roscap.mw.transport.kafka.KafkaTransportAdapter;

@RunWith(BlockJUnit4ClassRunner.class)
public class KafkaTest {
	KafkaTransportAdapter transportAdapter;
	KafkaTopicFactory topicFactory;
	final Object monitor = new Object();
	
//	Sender s;
	Receiver r;
	
	@Before
	public void init() {
		transportAdapter = new KafkaTransportAdapter();
		transportAdapter.initialize("ngwee:9092");
		transportAdapter.onApplicationEvent(new ContextRefreshedEvent(new AnnotationConfigApplicationContext()));
	
		r = new Receiver(transportAdapter);
	}
	
	@After
	public void destroy() {
		transportAdapter.unregisterEndpoint((TransportEndpoint)r);
		transportAdapter.close();
	}
	
	@Test
	public void testLol() {
		for (int i = 0; i < 10; i++) {
			Sender s = new Sender(transportAdapter);
			s.send(r.id().toString(), UUID.randomUUID());
			
			synchronized(monitor) {
				try {
					monitor.wait();
				}
				catch (InterruptedException e) {
					fail();
				}
			}
			
			transportAdapter.unregisterEndpoint((TransportEndpoint)s);
		}
	}
	
	private static class Dummy<T> extends HashMap<TransportHeader, Serializable> implements HeaderContainer {
		final T value;
		
		Dummy(T arg0) {
			value = arg0;
		}
		
		@Override
		public Serializable getHeader(TransportHeader header) {
			return get(header);
		}

		@Override
		public void addHeader(TransportHeader header, Serializable value) {
			put(header, value);
		}

		@Override
		public boolean containsHeader(TransportHeader header) {
			return containsKey(header);
		}
	}
	
	private class Sender implements RoutedEndpoint<Dummy<UUID>> {
		final UUID id;
		TransportAdapter transportAdapter;
		ZonedDateTime measure;
		UUID specimen;

		Sender(TransportAdapter arg0) {
			id = UUID.randomUUID();
			transportAdapter = arg0;
			transportAdapter.registerEndpoint((TransportEndpoint)this);
		}
		
		void send(String toDestination, UUID arg1) {
			specimen = arg1;
			Dummy<UUID> payload = new Dummy(arg1);
			payload.addHeader(TransportHeader.CLIENT, transportAdapter.clientId());
			payload.addHeader(TransportHeader.CORRELATION, id);
			
			measure = ZonedDateTime.now();
			transportAdapter.send(toDestination, payload);
		}
		
		@Override
		public void receive(String fromDestination, Dummy<UUID> payload) {
			System.out.println("received response: " + payload);
			assertThat(payload.value, equalTo(this.specimen));
			System.out.println("waited: " + measure.until(ZonedDateTime.now(), ChronoUnit.MILLIS));
			synchronized(monitor) {
				monitor.notifyAll();
			}
		}

		@Override
		public UUID id() {
			return id;
		}
	}

	private class Receiver implements TransportEndpoint<Dummy<UUID>> {
		final UUID id;
		TransportAdapter transportAdapter;

		Receiver(TransportAdapter arg0) {
			id = UUID.randomUUID();
			transportAdapter = arg0;
			transportAdapter.registerEndpoint((TransportEndpoint)this);
		}
		
		@Override
		public void receive(String fromDestination, Dummy<UUID> payload) {
			System.out.println("received: " + payload);
			
			transportAdapter.send(payload.getHeader(TransportHeader.CLIENT).toString(), payload);
		}

		@Override
		public String destination() {
			return id().toString();
		}

		@Override
		public UUID id() {
			return id;
		}
	}
}
