package com.roscap.mw.kafka.spring;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.util.ReflectionUtils;

/**
 * A necessary extension to KafkaAdmin that provides
 * more dynamic approach to managing topics
 * 
 * @author is.zharinov
 *
 */
public class KafkaTopicFactory extends KafkaAdmin {
	private static final Logger logger = LoggerFactory.getLogger(KafkaTopicFactory.class);
	
	private final Map<String, Object> config;

	public KafkaTopicFactory(Map<String, Object> config) {
		super(config);
		this.config = config;
		super.setAutoCreate(false);
	}

	AdminClient adminClient() {
		try {
			return AdminClient.create(this.config);
		}
		catch (Exception e) {
			throw new IllegalStateException("Could not create admin", e);
		}
	}

	/**
	 * create a topic with arbitrary number of partitions
	 * 
	 * @param destination
	 * @param partitions
	 * @return
	 */
	public boolean createTopic(String destination, int partitions) {
		AdminClient adminClient = null;

		try {
			adminClient = adminClient();
		}
		catch (IllegalStateException ise) {
			return false;
		}
		
		if (adminClient != null) {
			try {
				addTopicsIfNeeded(adminClient, Arrays.asList(new NewTopic(destination, partitions, (short)1)));
				logger.debug("created topic " + destination);
				return true;
			}
			catch (Throwable e) {
				throw new IllegalStateException("Could not configure topics", e);
			}
			finally {
				adminClient.close(1, TimeUnit.SECONDS);
			}
		}
		else {
			return false;
		}
		
	}
	
	
	/**
	 * Creates a topic with 1 partition
	 * 
	 * @param destination
	 * @return
	 */
	public boolean createTopic(String destination) {
		return createTopic(destination, 1);
	}
	
	/**
	 * Removes a topic.
	 * 
	 * This is not instantaneous.
	 * 
	 * @param destination
	 * @return
	 */
	public boolean removeTopic(String destination) {
		AdminClient adminClient = null;

		try {
			adminClient = adminClient();
		}
		catch (IllegalStateException ise) {
			return false;
		}
		
		if (adminClient != null) {
			try {
				adminClient.deleteTopics(Arrays.asList(destination)).all()/*.get()*/;
				logger.debug("removed topic " + destination);
				return true;
			}
			catch (Throwable e) {
				throw new IllegalStateException("Could not configure topics", e);
			}
			finally {
				adminClient.close(1, TimeUnit.SECONDS);
			}
		}
		else {
			return false;
		}
	}
	
	/**
	 * no idea why this particular method was made private in the superclass,
	 * so in order to avoid copy-paste we have to improvise
	 * 
	 * @see KafkaAdmin#addTopicsIfNeeded
	 */
	protected void addTopicsIfNeeded(AdminClient adminClient, Collection<NewTopic> topics) throws Throwable {
		try {
			Method addTopicsIfNeeded = KafkaAdmin.class.getDeclaredMethod("addTopicsIfNeeded",
					AdminClient.class, Collection.class);
			ReflectionUtils.makeAccessible(addTopicsIfNeeded);
			addTopicsIfNeeded.invoke(this, adminClient, topics);
		}
		catch (Exception e) {
			//this is literally not possible
		}
	}
}