package com.roscap.cdm.api;

import static org.springframework.util.ClassUtils.forName;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import com.jayway.jsonpath.JsonPath;
import com.roscap.cdm.api.annotation.Service;

//FIXME: methods with same name are not supported
/**
 * Helper class that incapsulates CDM-related operations
 * with service specifications
 * 
 * @author is.zharinov
 *
 */
public class CdmUtils {
	static final String PROTOCOL_CDM = "cdm";
	static final String ATTRIBUTE_URI = "CdmUri";
	static final String ATTRIBUTE_ID = "Id";
	static final String ATTRIBUTE_TYPE = "QualifiedType";
	static final String ATTRIBUTE_METHOD = "Method";
	static final String ATTRIBUTE_PARAMETER = "Parameter";
	
	private static final String URI_PATTERN = PROTOCOL_CDM + "://%s";
	
	private static final String SEARCH_METHOD_ARGUMENTS_PATTERN = "$.%s[?(@.%s == '%s')].%s[*].%s";
	private static final String SEARCH_METHOD_TYPE_PATTERN = "$.%s[?(@.%s == '%s')].%s";

	
	/**
	 * parse URL-friendly service name (equivalent to <b>QualifiedType</b> spec attribute) to CDM URI
	 * 
	 * @param name
	 * @return
	 */
	public static URI parseName(String name) {
		return URI.create(String.format(URI_PATTERN, name.replaceAll("\\.", "\\/")));
	}
	
	/**
	 * find service URI by spec
	 * 
	 * @param spec
	 * @return
	 */
	public static URI searchForUri(Object spec) {
		try {
			return new URI(searchFor(ATTRIBUTE_URI, spec));
		}
		catch (URISyntaxException se) {
			return null;
		}
	}
	
	/**
	 * find service instance id by spec
	 * 
	 * @param spec
	 * @return
	 */
	public static UUID searchForId(Object spec) {
		try {
			return UUID.fromString(searchFor(ATTRIBUTE_ID, spec));
		}
		catch (IllegalArgumentException iae) {
			return null;
		}
	}

	/**
	 * find service type by spec 
	 * @param spec
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Class<?> searchForClass(Object spec) throws ClassNotFoundException {
		return forName(searchFor(ATTRIBUTE_TYPE, spec), null);
	}

	private static String searchFor(String fieldName, Object spec) {
		return JsonPath.read(spec.toString(), String.format("$." + /*ATTRIBUTE_SPEC +*/ "%s", fieldName));
	}

	/**
	 * resolve argument types by method name and spec
	 * 
	 * @param methodName
	 * @param spec
	 * @return
	 */
	public static Class<?>[] resolveArguments(String methodName, Object spec) {
		URI cdmMethodUri = URI.create(searchForUri(spec).toString() + "?" + methodName); 
		
		List<String> m = JsonPath.read(spec.toString(),
				String.format(SEARCH_METHOD_ARGUMENTS_PATTERN, /*ATTRIBUTE_SPEC,*/ ATTRIBUTE_METHOD, ATTRIBUTE_URI, cdmMethodUri, ATTRIBUTE_PARAMETER, ATTRIBUTE_TYPE));
		
		Class<?>[] result = new Class<?>[m.size()];
		
		IntStream.range(0, m.size()).forEach(i -> {
				try {
					result[i] = forName(m.get(i), null);
				}
				catch (ClassNotFoundException cnfe) {
					//not much we can do
					result[i] = Object.class;
				}
			}
		);
		
		return result;
	}

	/**
	 * resolve method return type by its name and spec
	 * 
	 * @param methodName
	 * @param spec
	 * @return
	 */
	public static Class<?> resolveType(String methodName, Object spec) {
		URI cdmMethodUri = URI.create(searchForUri(spec).toString() + "?" + methodName); 
		
		List<String> m = JsonPath.read(spec.toString(),
				String.format(SEARCH_METHOD_TYPE_PATTERN, ATTRIBUTE_METHOD, ATTRIBUTE_URI, cdmMethodUri, ATTRIBUTE_TYPE));
		
		try {
			return forName(m.get(0).toString(), null);
		}
		catch (ClassNotFoundException cnfe) {
			//not much we can do
			return Object.class;
		}
	}	
	
	/**
	 * extract service descriptor for a given instance id and @Service annotated interface
	 * 
	 * @param id
	 * @param service
	 * @return JSON string that contains service spec
	 */
	public static Object extractDescriptor(UUID id, Class<?> targetInterface) {
		try {
			URI uri =  new URI(targetInterface.getAnnotation(Service.class).uri());

			JsonArrayBuilder methodsBuilder = Json.createArrayBuilder();
			
			Arrays.asList(targetInterface.getMethods()).forEach(m -> {
				URI methodUri = URI.create(uri.toString() + "?" + m.getName());
				List<Class<?>> parameters = Arrays.asList(m.getParameterTypes());
				
				JsonArrayBuilder parametersBuilder = Json.createArrayBuilder();
				
				IntStream.range(0, parameters.size()).forEach(i -> {
					parametersBuilder.add(Json.createObjectBuilder().
							add(ATTRIBUTE_TYPE, parameters.get(i).getCanonicalName()).
							add(ATTRIBUTE_URI, methodUri.resolve("#" + i).toString()));
				});
				
				methodsBuilder.add(Json.createObjectBuilder().
						add(ATTRIBUTE_URI, methodUri.toString()).
						add(ATTRIBUTE_TYPE, m.getReturnType().getCanonicalName()).
						add(ATTRIBUTE_PARAMETER, parametersBuilder));
				}
			);
			
			JsonObject spec = Json.createObjectBuilder().
					add(ATTRIBUTE_URI, uri.toString()).
					add(ATTRIBUTE_ID, id.toString()).
					add(ATTRIBUTE_TYPE, targetInterface.getCanonicalName()).
					add(ATTRIBUTE_METHOD, methodsBuilder).
					build();
			
			return /*Json.createObjectBuilder().add(ATTRIBUTE_SPEC,*/ spec/*).build()*/.toString();
		}
		catch (URISyntaxException e) {
			return null;
		}
	}
	
	/**
	 * build a joined descriptor for given number of descriptors
	 * 
	 * @param descriptors
	 * @return
	 */
	public static Object assembleDescriptors(Collection<Object> descriptors) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		
		descriptors.forEach(d -> {
				builder.add(Json.createReader(new StringReader(d.toString())).readObject());
			}
		);
		
		return builder.build().toString();
	}
	
	/**
	 * find a @Service annotated interface on a given class (possibly an interface itself)
	 * 
	 * @param targetClass
	 * @return
	 */
	public static Class<?> findTargetServiceInterface(Class<?> targetClass) {
		if (targetClass.isInterface()) {
			if (targetClass.isAnnotationPresent(Service.class)) {
				return targetClass;
			}
		}
		
		for (Class<?> i : targetClass.getInterfaces()) {
			if (i.isAnnotationPresent(Service.class)) {
				return i;
			}
			else {
				Class<?> i1 = findTargetServiceInterface(i);
				if (i1 != null) {
					return i1;
				}
			}
		}
		
		return null;
	}	
}
