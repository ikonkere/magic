package com.roscap.mw.transport.header;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * copied from RemoteInvoocation
 * 
 * @author is.zharinov
 *
 */
public class Attributes implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6349022854356971322L;
	
	private Map<String, Serializable> attributes = new HashMap<>();

	/**
	 * Add an additional invocation attribute. Useful to add additional
	 * invocation context without having to subclass RemoteInvocation.
	 * <p>Attribute keys have to be unique, and no overriding of existing
	 * attributes is allowed.
	 * <p>The implementation avoids to unnecessarily create the attributes
	 * Map, to minimize serialization size.
	 * @param key the attribute key
	 * @param value the attribute value
	 * @throws IllegalStateException if the key is already bound
	 */
	public void addAttribute(String key, Serializable value) throws IllegalStateException {
		if (this.attributes == null) {
			this.attributes = new HashMap<String, Serializable>();
		}
		if (this.attributes.containsKey(key)) {
			throw new IllegalStateException("There is already an attribute with key '" + key + "' bound");
		}
		this.attributes.put(key, value);
	}

	/**
	 * Retrieve the attribute for the given key, if any.
	 * <p>The implementation avoids to unnecessarily create the attributes
	 * Map, to minimize serialization size.
	 * @param key the attribute key
	 * @return the attribute value, or {@code null} if not defined
	 */
	public Serializable getAttribute(String key) {
		if (this.attributes == null) {
			return null;
		}
		return this.attributes.get(key);
	}

	/**
	 * Set the attributes Map. Only here for special purposes:
	 * Preferably, use {@link #addAttribute} and {@link #getAttribute}.
	 * @param attributes the attributes Map
	 * @see #addAttribute
	 * @see #getAttribute
	 */
	public void setAttributes(Map<String, Serializable> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Return the attributes Map. Mainly here for debugging purposes:
	 * Preferably, use {@link #addAttribute} and {@link #getAttribute}.
	 * @return the attributes Map, or {@code null} if none created
	 * @see #addAttribute
	 * @see #getAttribute
	 */
	public Map<String, Serializable> getAttributes() {
		return this.attributes;
	}
}
