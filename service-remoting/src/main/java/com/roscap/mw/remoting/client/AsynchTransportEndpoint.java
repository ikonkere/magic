package com.roscap.mw.remoting.client;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.roscap.mw.reflection.RemoteInvocationResultExt;
import com.roscap.mw.transport.UniqueCorrelation;
import com.roscap.mw.transport.correlation.CorrelationPolicy;
import com.roscap.mw.transport.endpoint.RoutedEndpoint;

/**
 * A basic transport endpoint that uses shared blocking queues to
 * propagate received payload to it's appropriate thread of execution
 * (usually the thread of its creation)
 * 
 * @author is.zharinov
 *
 */
public class AsynchTransportEndpoint implements RoutedEndpoint<RemoteInvocationResultExt> {
	private final static Logger logger = LoggerFactory.getLogger(AsynchTransportEndpoint.class);
	
	private final BlockingQueue<RemoteInvocationResultExt> q;
	private final CorrelationPolicy<UUID> cPolicy;
	
	/**
	 * 
	 * @param arg2 shared queue to use for payload propagation
	 */
	public AsynchTransportEndpoint(BlockingQueue<RemoteInvocationResultExt> arg2) {
		this(new UniqueCorrelation(), arg2);
	}
	
	/**
	 * 
	 * @param arg0
	 * @param arg1
	 */
	private AsynchTransportEndpoint(CorrelationPolicy arg0, BlockingQueue<RemoteInvocationResultExt> arg1) {
		cPolicy = arg0;
		q = arg1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportEndpoint#receive(java.lang.String, java.lang.Object)
	 */
	@Override
	public void receive(String fromDestination, RemoteInvocationResultExt payload) {
		logger.info(String.format("received response %s %s", fromDestination, payload));
		if (!q.offer(payload)) {
			logger.error("nobody listens");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportEndpoint#id()
	 */
	@Override
	public UUID id() {
		return cPolicy.correlation();
	}

	/**
	 * 
	 * @return
	 */
	CorrelationPolicy<UUID> policy() {
		return cPolicy;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id().hashCode();
	}
}
