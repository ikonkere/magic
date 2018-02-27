package com.roscap.test;

import java.net.URI;
import java.util.NoSuchElementException;

import com.roscap.mw.executor.ServiceExecutor;

public class TestServiceExecutor extends ServiceExecutor {
	public Object findByUri(URI uri) {
		throw new NoSuchElementException("imitating");
	}
}
