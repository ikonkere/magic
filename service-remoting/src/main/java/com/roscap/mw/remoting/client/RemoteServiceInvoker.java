package com.roscap.mw.remoting.client;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.RemoteInvocationResult;

import com.roscap.mw.reflection.ReflectiveServiceInvoker;
import com.roscap.mw.reflection.RemoteInvocationExt;
import com.roscap.mw.reflection.RemoteInvocationResultExt;
import com.roscap.mw.transport.TransportAdapter;
import com.roscap.mw.transport.correlation.TransportHeader;
import com.roscap.mw.transport.endpoint.TransportEndpoint;

/**
 * 
 * Method interceptor that is responsible for sending invocations
 * over specified transport and receiving corresponding invocation results;
 * 
 * @author is.zharinov
 *
 */
public class RemoteServiceInvoker extends ReflectiveServiceInvoker implements InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(RemoteServiceInvoker.class);
	
	/**
	 * an optimistic timeout for queue operations
	 */
	public static final long PROPAGATE_TIMEOUT = 5;

	private TransportAdapter transportAdapter;
	
	private UUID serviceId;

	private final ThreadLocal<BlockingQueue<RemoteInvocationResultExt>> responseQueue =
			ThreadLocal.withInitial(() -> new SynchronousQueue<RemoteInvocationResultExt>(true));

	public RemoteServiceInvoker() {
		
	}
	
	public RemoteServiceInvoker(UUID arg0) {
		setServiceId(arg0);
	}
	
	/**
	 * transport adapter to use
	 * @param arg0
	 */
	public void setTransportAdapter(TransportAdapter arg0) {
		this.transportAdapter = arg0;
	}
	
	/**
	 * 
	 * @return
	 */
	public UUID getServiceId() {
		return serviceId;
	}

	/**
	 * 
	 * @param serviceId
	 */
	public void setServiceId(UUID serviceId) {
		this.serviceId = serviceId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
//		assert serviceId != null : "Target servce is not set";
		assert transportAdapter != null : "Transport is not set";
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return getServiceId().toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.reflection.ReflectiveServiceInvoker#invokeSynch(org.springframework.remoting.support.RemoteInvocation)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected RemoteInvocationResult invokeSynch(RemoteInvocationExt ri) {
		RemoteInvocationResult result = null;
		AsynchTransportEndpoint responseEndpoint =
				new AsynchTransportEndpoint(responseQueue.get());
		ri.addHeader(TransportHeader.CLIENT,
				transportAdapter.clientId());
		ri.addHeader(TransportHeader.CORRELATION,
				responseEndpoint.policy().correlation());
		ri.addHeader(TransportHeader.CORRELATION_TYPE,
				responseEndpoint.policy().type());
		
		try {
			transportAdapter.registerEndpoint((TransportEndpoint)responseEndpoint);
			
			ZonedDateTime startTime = ZonedDateTime.now();
				transportAdapter.send(toString(), ri);
				result = responseQueue.get().poll(PROPAGATE_TIMEOUT, TimeUnit.SECONDS);
			long t = startTime.until(ZonedDateTime.now(), ChronoUnit.MILLIS);

			logger.debug("executed synchronously in " + t + "ms");
			
			if (result == null) {
				result = new RemoteInvocationResult(new RemoteAccessException("invocation timed out"));
			}
		}
		catch (Exception ex) {
			throw new RemoteAccessException("Remote invocation exception", ex);
		}
		finally {
			transportAdapter.unregisterEndpoint((TransportEndpoint)responseEndpoint);
			responseQueue.remove();
		}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.roscap.mw.reflection.ReflectiveServiceInvoker#invokeAsynch(org.springframework.remoting.support.RemoteInvocation)
	 */
	@Override
	protected RemoteInvocationResult invokeAsynch(RemoteInvocationExt ri) {
		//FIXME: we don't account for cases when void methods throw exceptions
		//that need to be propagated to clients
		transportAdapter.send(toString(), ri);
		return new RemoteInvocationResult(null);
	}
}