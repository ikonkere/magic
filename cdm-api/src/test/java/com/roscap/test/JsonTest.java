package com.roscap.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import com.roscap.cdm.api.CdmUtils;
import com.roscap.cdm.api.annotation.Service;

@RunWith(BlockJUnit4ClassRunner.class)
public class JsonTest {
	Object spec;

	private static Object extractDescriptor(UUID id, Object service) {
		return CdmUtils.extractDescriptor(id, CdmUtils.findTargetServiceInterface(service.getClass()));
	}
	
	@Before
	public void beforeTest() {
		spec = extractDescriptor(UUID.randomUUID(),
				new TestService() {
					@Override
					public long testMethod(Object arg1, Object[] arg2, List<Object> arg3, int[] arg4) {
						return -1l;
					}

					@Override
					public void testMethod2() {
					}
				});
	}
	
	@Test
	public void testUUID() {
		System.out.println(UUID.randomUUID().toString());
	}
	
	@Test
	public void testUri() throws URISyntaxException {
		URI testParameterUri = new URI(TestService.class.getAnnotation(Service.class).uri() + "?testMethod" + "#1");
		
		System.out.println(testParameterUri.getFragment());

		URI testMethodUri = new URI(TestService.class.getAnnotation(Service.class).uri() + "?testMethod");
		
		System.out.println(testMethodUri.getQuery());
		
		String methodName = testMethodUri.getQuery();
		URI cdmServiceUri = URI.create(testMethodUri.toString().replaceFirst("\\?"+methodName, ""));

		System.out.println(cdmServiceUri);
		
		URI t = testMethodUri.resolve("#1");
		
		System.out.println(t);
		
		
		String name = "Services.Test.TestSeervice";
		
		System.out.println(CdmUtils.parseName(name));
	}
	
	@Test
	public void testPath() {
		URI cdmUri = CdmUtils.searchForUri(spec);
		assertThat(TestService.class.getAnnotation(Service.class).uri(), equalTo(cdmUri.toString()));
	}
	
	@Test
	public void testDescriptor() {
		System.out.println(spec);
	}
	
	@Test
	public void testResolveArguments() {
		Class<?>[] args = CdmUtils.resolveArguments("testMethod", spec);
		System.out.println();
		assertThat(new Class<?>[] {Object.class, Object[].class, List.class, int[].class}, equalTo(args));
	}
	
	@Test
	public void testResolveType() {
		Class<?> returnType = CdmUtils.resolveType("testMethod2", spec);
		System.out.println();
		assertThat(void.class, equalTo(returnType));
	}
	
	@Test
	public void testAssemble() {
		System.out.println(CdmUtils.assembleDescriptors(Arrays.asList(spec, spec, spec)));
	}
}
