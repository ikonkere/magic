package com.roscap.mw.kafka;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.converter.ConversionException;

/**
 * Simple serializer that uses standard Java serialization mechanism
 * and thus inherits all it's pros and cons.

 * @author is.zharinov
 *
 */
public class JavaSerializationSerializer implements Serializer<Object> {
	/*
	 * (non-Javadoc)
	 * @see org.apache.kafka.common.serialization.Serializer#configure(java.util.Map, boolean)
	 */
	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		//nothing to configure
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.kafka.common.serialization.Serializer#serialize(java.lang.String, java.lang.Object)
	 */
	@Override
	public byte[] serialize(String topic, Object data) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);) {
			oos.writeObject(data);
			return baos.toByteArray();
		}
		catch (IOException e) {
			throw new ConversionException("Failed to convert to", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.kafka.common.serialization.Serializer#close()
	 */
	@Override
	public void close() {
		//it's stateless
	}
}
