package com.roscap.mw.reflection;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.remoting.RemoteInvocationFailureException;
import org.springframework.remoting.support.RemoteAccessor;
import org.springframework.remoting.support.RemoteInvocationResult;

import com.roscap.mw.transport.correlation.TransportHeader;
import com.roscap.mw.transport.header.HeaderContainer;
import com.roscap.util.Tuple;

/**
 * A remote accessor that uses headers and reflections to perform an invocation
 * over remote service's proxy
 * 
 * @author is.zharinov
 *
 */
public abstract class ReflectiveServiceInvoker extends RemoteAccessor implements MethodInterceptor {
	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		if (AopUtils.isToStringMethod(methodInvocation.getMethod())) {
			return this.toString();
		}
		
		return invoke(createRemoteInvocation(methodInvocation));
	}

	/**
	 * synchronous invocation
	 * 
	 * @param targetMethod
	 * @param returnType
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	public Object invoke(String targetMethod, Class<?> returnType, Argument<?>... args) throws Throwable {
		Tuple<Class<?>[], Object[]> t = Argument.unwrap(args);
		return invoke(createRemoteInvocation(targetMethod, returnType, t.left, t.right));
	}

	/**
	 * asynchronous invocation
	 * 
	 * @param targetMethod
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	public Object invoke(String targetMethod, Argument<?>... args) throws Throwable {
		invoke(targetMethod, void.class, args);
		return null;
	}
	
	/**
	 * Programmatic remote invocation as opposed to {@link #invoke(MethodInvocation)}
	 * 
	 * @param ri remote invocation to perform
	 * @return remote invocation result (null in case of void methods) 
	 * @throws Throwable
	 */
	protected Object invoke(RemoteInvocationExt ri) throws Throwable {
		logger.info(String.format("invoking %s:%s", toString(), ri));

		RemoteInvocationResult result = isSynch(ri) ? invokeSynch(ri) : invokeAsynch(ri);
		
		try {
			return recreateRemoteInvocationResult(result);
		}
		catch (Throwable ex) {
			if (result.hasInvocationTargetException()) {
				throw ex;
			}
			else {
				throw new RemoteInvocationFailureException("Invocation of method [" + ri.getMethodName() +
						"] failed in remote service invoker", ex);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.remoting.support.RemoteInvocationBasedAccessor#createRemoteInvocation(org.aopalliance.intercept.MethodInvocation)
	 */
	protected RemoteInvocationExt createRemoteInvocation(MethodInvocation methodInvocation) {
		RemoteInvocationExt ri = new RemoteInvocationExt(methodInvocation);
		ri.addHeader(TransportHeader.RETURN_TYPE, (Class)methodInvocation.getMethod().getReturnType());
		return ri;
	}
	
	/**
	 * 
	 * @param methodName
	 * @param returnType
	 * @param parameterTypes
	 * @param arguments
	 * @return
	 */
	protected RemoteInvocationExt createRemoteInvocation(String methodName, Class<?> returnType, Class<?>[] parameterTypes, Object[] arguments) {
		RemoteInvocationExt ri = new RemoteInvocationExt(methodName, parameterTypes, arguments);
		ri.addHeader(TransportHeader.RETURN_TYPE, returnType);
		return ri;
	}


	/**
	 * Recreate the invocation result contained in the given RemoteInvocationResult object.
	 * <p>The default implementation calls the default {@code recreate()} method.
	 * This can be overridden in subclass to provide custom recreation, potentially
	 * processing the returned result object.
	 * @param result the RemoteInvocationResult to recreate
	 * @return a return value if the invocation result is a successful return
	 * @throws Throwable if the invocation result is an exception
	 * @see RemoteInvocationResult#recreate()
	 */
	protected Object recreateRemoteInvocationResult(RemoteInvocationResult result) throws Throwable {
		return result.recreate();
	}

	/**
	 * 
	 * @return true if the target method is void
	 */
	protected static boolean isSynch(HeaderContainer ri) {
		Class<?> returnType = void.class;
		
		if (ri.containsHeader(TransportHeader.RETURN_TYPE)) {
			returnType = (Class)ri.getHeader(TransportHeader.RETURN_TYPE);
		}
		
		return !void.class.equals(returnType);
	}
	
	/**
	 * perform synchronous invocation
	 *  
	 * @param ri
	 * @return
	 */
	protected abstract RemoteInvocationResult invokeSynch(RemoteInvocationExt ri);
	
	/**
	 * perform asynchronous invocation
	 * 
	 * @param ri
	 * @return
	 */
	protected abstract RemoteInvocationResult invokeAsynch(RemoteInvocationExt ri);
}
