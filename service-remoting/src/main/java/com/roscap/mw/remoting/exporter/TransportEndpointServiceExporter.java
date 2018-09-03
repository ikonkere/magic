package com.roscap.mw.remoting.exporter;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteInvocationResult;

import com.roscap.mw.reflection.ReflectiveServiceExporter;
import com.roscap.mw.reflection.RemoteInvocationExt;
import com.roscap.mw.transport.TransportAdapter;
import com.roscap.mw.transport.endpoint.TransportEndpoint;

/**
 * Transport endpoint implementation that serves as a service exporter
 * 
 * @author is.zharinov
 *
 */
public class TransportEndpointServiceExporter extends ReflectiveServiceExporter
		implements InitializingBean, TransportEndpoint<RemoteInvocationExt> {
	private static final Logger logger = LoggerFactory.getLogger(TransportEndpointServiceExporter.class);
	private final UUID serviceId;
	private TransportAdapter transportAdapter;

	private final ExecutorService executor = Executors.newFixedThreadPool(10);
	
	public TransportEndpointServiceExporter(UUID arg0) {
		serviceId = arg0;
	}
	
	/**
	 * transport adapter to use for this exporter
	 * 
	 * @param arg0
	 */
	public void setTransportAdapter(TransportAdapter arg0) {
		this.transportAdapter = arg0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportEndpoint#destination()
	 */
	@Override
	public String destination() {
		return id().toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.TransportEndpoint#id()
	 */
	@Override
	public UUID id() {
		return serviceId;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return serviceId.hashCode();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		assert serviceId != null : "service id can't be null";
		createProxy();
	}

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.endpoint.TransportEndpoint#receive(java.lang.String, java.io.Serializable)
	 */
	@Override
	public void receive(String fromDestination, RemoteInvocationExt payload) {
		//FIXME: in certain recursive cases executor will run out of threads
		//and will block kafka listener threads in turn.
		//i can't seem to fix this for now, but must have this in mind
		executor.execute(() -> {
			logger.info(String.format("service %s received %s", serviceId, payload));
			this.receive(payload);
		});
	}
	
	private AtomicInteger i = new AtomicInteger(10);

	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.reflection.ReflectiveServiceExporter#respond(java.lang.String, org.springframework.remoting.support.RemoteInvocationResult)
	 */
	@Override
	protected void respond(String toDestination, RemoteInvocationResult payload) {
		transportAdapter.send(toDestination, payload);
	}

	/*
	 * services own their endpoints
	 * 
	 * (non-Javadoc)
	 * @see com.roscap.mw.transport.endpoint.TransportEndpoint#isDestinationOwner()
	 */
	@Override
	public boolean isDestinationOwner() {
		return true;
	}
}