package com.roscap.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.roscap.context.test.RemotingClientTestContext;
import com.roscap.context.test.RemotingTestContext;
import com.roscap.context.test.TransportTestContext;
import com.roscap.mw.reflection.Argument;
import com.roscap.mw.remoting.client.RemoteServiceInvoker;
import com.roscap.mw.transport.TransportAdapter;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
	@ContextConfiguration(classes=TransportTestContext.class),
	@ContextConfiguration(classes=RemotingTestContext.class),
	@ContextConfiguration(classes=RemotingClientTestContext.class)
})
public class RemotingTest {
	@Autowired
	@Qualifier("TestServiceClient")
	private TestService testServiceClient;
	
	@Autowired
	private TransportAdapter ta;

	@Autowired
	private RemoteServiceInvoker rsi;
	
	
	@Test
	public void testFail() throws Exception {
		try {
			testServiceClient.fail();
			fail();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
		
		try {
			Object res = rsi.invoke("fail");
			fail();
		}
		catch (Throwable t) {
			t.printStackTrace();;
		}
	}
	
	@Test
	public void testSimpleMethod() {
		for (int i = 0; i < 10; i++) {
			UUID t = UUID.randomUUID();
			assertThat(testServiceClient.doStuff(t), equalTo(t));
		}
	}
	
	@Test
	public void testComplexMethod() throws Throwable {
		Object res1 = testServiceClient.doMoreStuff("I DID", "MORE STUFF");
		assertThat(res1, equalTo("ohforfsake"));

		Object res2 = rsi.invoke("doMoreStuff", String.class, Argument.create(String.class, "AND THIS"), Argument.create(String.class, "AS WELL!"));
		assertThat(res2, equalTo("ohforfsake"));
	}
	
	@Test
	public void testVoidMethod() throws Throwable {
		testServiceClient.whatever();		

		Object res1 = rsi.invoke("whatever");
		assertThat(null, equalTo(res1));
	}
	
	@Test
	public void testIntercept() throws Throwable {
		Object res2 = rsi.invoke("doMoreStuff2", Object.class, Argument.create(String.class, "ONE"), Argument.create(String.class, "TWO"));
		assertThat(String.valueOf(ta.clientId()), equalTo(res2));
	}
}
