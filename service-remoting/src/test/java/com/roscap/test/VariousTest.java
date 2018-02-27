package com.roscap.test;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class VariousTest {
	@Test
	public void lol() throws NoSuchMethodException, SecurityException {
		Method m = VariousTest.class.getMethod("lol");
		
		assertTrue(void.class.equals(m.getReturnType()));
	}
}
