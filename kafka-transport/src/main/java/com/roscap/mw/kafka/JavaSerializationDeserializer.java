package com.roscap.mw.kafka;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.support.converter.ConversionException;

/**
 * Simple deserializer that uses standard Java serialization mechanism
 * and thus inherits all it's pros and cons.
 * 
 * @author is.zharinov
 *
 */
public class JavaSerializationDeserializer implements Deserializer<Object> {
	/*
	 * (non-Javadoc)
	 * @see org.apache.kafka.common.serialization.Deserializer#configure(java.util.Map, boolean)
	 */
	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		//nothing to configure
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.kafka.common.serialization.Deserializer#deserialize(java.lang.String, byte[])
	 */
	@Override
	public Object deserialize(String topic, byte[] data) {
		try (ObjectInputStream ois =
				new ObjectInputStream(new ByteArrayInputStream(data));) {
			return ois.readObject();
		}
		catch (IOException e) {
			throw new ConversionException("Failed to convert from", e);
		}
		catch (ClassNotFoundException e) {
			throw new ConversionException("No suitable class found", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.kafka.common.serialization.Deserializer#close()
	 */
	@Override
	public void close() {
		//it's stateless
	}
}
