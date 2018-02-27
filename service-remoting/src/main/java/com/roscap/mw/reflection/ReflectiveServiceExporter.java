package com.roscap.mw.reflection;

import java.lang.reflect.Method;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedExporter;
import org.springframework.remoting.support.RemoteInvocationResult;

import com.roscap.mw.transport.correlation.CorrelationType;
import com.roscap.mw.transport.correlation.TransportHeader;
import com.roscap.mw.transport.header.HeaderContainer;
import com.roscap.mw.transport.header.WithHeaders;

/**
 * 
 * a RIBE that uses transport headers and reflections to export services
 * 
 * @author is.zharinov
 *
 * @param <T>
 */
public abstract class ReflectiveServiceExporter extends RemoteInvocationBasedExporter {
	private static final Logger logger = LoggerFactory.getLogger(ReflectiveServiceExporter.class);

	private Object proxy;

	/**
	 * creates an infrastructure proxy this exporter
	 * will use to invoke all remote invocations
	 */
	protected void createProxy() {
		proxy = this.getProxyForService();
	}
	
	/**
	 * receive given remote invocation for execution and potential response
	 * 
	 * @param ri
	 */
	protected void receive(RemoteInvocation invocation) {
		if (invocation != null && invocation instanceof RemoteInvocationExt) {
			RemoteInvocationExt ri = (RemoteInvocationExt)invocation;
			RemoteInvocationResultExt result =
					new RemoteInvocationResultExt(invokeAndCreateResult(ri, this.proxy));
			
			try {
				result = new RemoteInvocationResultExt(invokeWithHeaders(ri, result.getValue()));
			}
			catch (Exception e) {
				logger.warn("Couldn't intercept with headers", e);
				//couldn't intercept, don't care
			}

			result.addHeader(TransportHeader.CORRELATION, ri.getHeader(TransportHeader.CORRELATION));
			
			if (isSynch(ri)) {
				String replyTo = replyTo(ri);
				
				if (replyTo != null) {
					respond(replyTo, result);
				}
				else {
					logger.warn("Can't formulate reply-to to invocation: " + ri);
				}
			}
		}
		//we can't do much if it's a normal RemoteInvocation
	}
	
	/**
	 * synchronously respond to given remote invocation
	 * 
	 * @param payload
	 * @param result
	 */
	protected abstract void respond(String toDestination, RemoteInvocationResult result);
	
	/**
	 * invokes @WithHeaders method after the main method returns (AfterReturning).
	 * 
	 * nb. this invocation bypasses the internal proxy
	 * 
	 * @param ri
	 * @param result
	 * @throws Exception
	 * @return
	 */
	protected Object invokeWithHeaders(RemoteInvocationExt ri, Object result) throws Exception {
		Class<?> targetClass = this.getService().getClass();
		Method targetMethod = targetClass.getMethod(ri.getMethodName(), ri.getParameterTypes());
		
		if (targetMethod.isAnnotationPresent(WithHeaders.class)) {
			WithHeaders md = targetMethod.getAnnotation(WithHeaders.class);
			Class<?>[] argTypes;
			Object[] args;
			
			if (isSynch(ri)) {
				argTypes = new Class<?>[md.headers().length + 1];
				args = new Object[md.headers().length + 1];
				
				argTypes[md.headers().length] = (Class)ri.getHeader(TransportHeader.RETURN_TYPE);
				args[md.headers().length] = result;
			}
			else {
				argTypes = new Class<?>[md.headers().length];
				args = new Object[md.headers().length];
			}
			
			for (int i = 0; i < md.headers().length; i++) {
				TransportHeader headerName = md.headers()[i];
				
				if (ri.containsHeader(headerName)) {
					args[i] = ri.getHeader(headerName);
					argTypes[i] = args[i].getClass();
				}
				else {
					throw new IllegalArgumentException("Invalid WithHeaders definition: " + headerName);
				}
			}
			
			try {
				Method withHeadersMethod = targetClass.getMethod(md.method(), argTypes);
				Object finalResult = withHeadersMethod.invoke(this.getService(), args);
				//interceptor doesn't return, don't care
				return (void.class.equals(withHeadersMethod.getReturnType())) ? result : finalResult;
			}
			catch (Exception e) {
				//couldn't intercept, don't care
				logger.warn("", e);
				return result;
			}
		}
		else {
			return result;
		}
	}
	
	/**
	 * determines if client expects the given remote invocation to be executed
	 * in a synchronous manner
	 * 
	 * @param ri
	 * @return
	 */
	protected static boolean isSynch(HeaderContainer ri) {
		Class<?> returnType = void.class;
		
		if (ri.containsHeader(TransportHeader.RETURN_TYPE)) {
			returnType = (Class)ri.getHeader(TransportHeader.RETURN_TYPE);
		}
		
		return !void.class.equals(returnType);
	}

	/**
	 * determines reply-to for a given invocation
	 * 
	 * @param ri
	 * @return null if can't be determined for any reason
	 */
	protected static String replyTo(HeaderContainer ri) {
		//we prefer to reply to clients and do correlation on their side
		if (ri.containsHeader(TransportHeader.CLIENT)) {
			return ((UUID)ri.getHeader(TransportHeader.CLIENT)).toString();
		}
		//but we support direct correlation as well
		else if (ri.containsHeader(TransportHeader.CORRELATION)
				&& ri.containsHeader(TransportHeader.CORRELATION_TYPE)) {
			return ((CorrelationType) ri.getHeader(TransportHeader.CORRELATION_TYPE))
					.replyTo((String) ri.getHeader(TransportHeader.CORRELATION));
		}
		else {
			return null;
		}
	}
}
