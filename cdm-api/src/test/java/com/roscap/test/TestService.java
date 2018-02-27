package com.roscap.test;

import java.util.List;

import com.roscap.cdm.api.annotation.Service;

@Service(/*id="695b9fa0-e32c-4e13-bd5a-2da2cf3cb19a",*/
	uri="cdm://Services/Test/TestService")
public interface TestService {
	public long testMethod(Object arg1, Object[] arg2, List<Object> arg3, int[] arg4);
	public void testMethod2();
}
