package com.roscap.test;

import java.util.UUID;

import com.roscap.cdm.api.annotation.Service;

@Service(/*id="e79dc103-136c-4295-8c9f-1060129308ac", */uri="cdm://Services/Test/TestService")
public interface TestService {
	public static final UUID SID = UUID.fromString("e79dc103-136c-4295-8c9f-1060129308ac");
	
	public UUID doStuff(UUID arg0);
	
	public Object doMoreStuff(String arg1, String arg2);
	
	public Object doMoreStuff2(String arg1, String arg2);

	public void whatever();
	
	public Object fail();
}
