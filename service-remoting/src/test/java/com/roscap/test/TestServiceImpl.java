package com.roscap.test;

import java.util.UUID;

import com.roscap.mw.transport.correlation.TransportHeader;
import com.roscap.mw.transport.header.WithHeaders;

public class TestServiceImpl implements TestService {
	@Override
	public UUID doStuff(UUID arg) {
		System.out.println("I DID STUFF! " + arg);
		
		return arg;
	}

	@Override	
	public Object doMoreStuff(String arg1, String arg2) {
		System.out.println(arg1);
		System.out.println(arg2);
		return "ohforfsake";
	}
	
	@Override	
	@WithHeaders(headers=TransportHeader.CLIENT, method="intcpt")
	public Object doMoreStuff2(String arg1, String arg2) {
		System.out.println(arg1);
		System.out.println(arg2);
		return "ohforfsake";
	}
	
	public void whatever() {
		System.out.println("ASYNCHRONOUS STUFF");
	}
	
	public Object fail() {
		throw new RuntimeException("ass");
	}
	
	public Object intcpt(UUID clientId, Object res) {
		return clientId.toString();
	}
}
