package com.roscap.mw.transport.local;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.roscap.mw.transport.endpoint.TransportEndpoint;

public class DelegatingQueueListener<T extends Serializable> implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(DelegatingQueueListener.class);

	private final TransportEndpoint<T> endpoint;
	private final BlockingQueue<T> destination;
	private final AtomicBoolean active = new AtomicBoolean(false);
	
	public DelegatingQueueListener(TransportEndpoint<T> arg0, BlockingQueue<T> arg1) {
		endpoint = arg0;
		destination = arg1;
	}
	
	/**
	 * get delegate id
	 * 
	 * @return
	 */
	public UUID getEndpointId() {
		return endpoint.id();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		active.set(true);
		
		while (active.get()) {
			T p = null;
			
			try {
				p = destination.poll(10, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException ie) {
				logger.error("unexpected exception", ie);
				continue;
			}
			
			if (p == null) {
				continue;
			}
			
			endpoint.receive(endpoint.destination(), p);
		}
	}

	/**
	 * stop this listener
	 */
	public void deactivate() {
		active.set(false);
	}
}
