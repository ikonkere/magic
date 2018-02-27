package com.roscap.mw.reflection;

import java.util.stream.IntStream;

import com.roscap.util.Tuple;

/**
 * Convenience class that makes operating with\
 * reflections a bit easier
 * 
 * @author is.zharinov
 *
 * @param <T>
 */
public class Argument<T> extends Tuple<Class<T>, T> {
	Argument(Class<T> arg0, T arg1) {
		super(arg0, arg1);
	}

	/**
	 * Creates an argument with a given type and value.
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public static <T> Argument<T> create(Class<T> arg0, T arg1) {
		return new Argument<T>(arg0, arg1);
	}

	/**
	 * converts an array of arguments into a tuple of arrays,
	 * useful for method invocation
	 * @param args
	 * @return
	 */
	public static Tuple<Class<?>[], Object[]> unwrap(Argument<?>[] args) {
		Tuple<Class<?>[], Object[]> result =
				new Tuple<Class<?>[], Object[]>(new Class<?>[args.length], new Object[args.length]);
		
		IntStream.range(0, args.length).forEach(i -> {
				result.left[i] = args[i].left;
				result.right[i] = args[i].right;
			}
		);
		
		return result;
	}

	/**
	 * converts a tuple of arrays into an array of arguments, 
	 * useful for method invocation
	 *
	 * @param argTypes
	 * @param args
	 * @return
	 */
	public static Argument<?>[] wrap(Class<?>[] argTypes, Object[] args) {
		Argument<?>[] result =
				new Argument<?>[argTypes.length];
		
		IntStream.range(0, argTypes.length).forEach(i -> {
				result[i] = Argument.create((Class)argTypes[i], args[i]);
			}
		);
		
		return result;
	}
}
