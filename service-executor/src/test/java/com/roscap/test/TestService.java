package com.roscap.test;

import java.util.List;

import com.roscap.cdm.api.annotation.Service;

@Service(/*id="42f8d3f4-44dd-40c6-820a-e086d9d0e23", */uri="cdm://Services/Test/TestService3")
public interface TestService {
	public String whatever(int a, List<String> b);
}
