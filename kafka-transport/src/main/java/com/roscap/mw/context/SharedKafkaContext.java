package com.roscap.mw.context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Role;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor;
import org.springframework.kafka.config.KafkaListenerConfigUtils;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer.AckMode;

import com.roscap.mw.kafka.JavaSerializationDeserializer;
import com.roscap.mw.kafka.JavaSerializationSerializer;
import com.roscap.mw.kafka.spring.AckConcurrentKafkaListenerContainerFactory;
import com.roscap.mw.kafka.spring.ConfigurableKafkaListenerEndpointRegistry;
import com.roscap.mw.kafka.spring.KafkaTopicFactory;
import com.roscap.mw.kafka.spring.RebalanceManager;
import com.roscap.mw.kafka.spring.ReflectiveKafkaListenerFactory;
import com.roscap.mw.transport.endpoint.TransportEndpoint;
import com.roscap.mw.transport.header.HeaderContainer;
import com.roscap.mw.transport.kafka.AggregatingRoutingEndpoint;

/**
 * 
 * Shared properties will clash if both a service and it's remote client
 * are instantiated within a single context (that is usually the case with tests.
 * That is also true in case of Service Registry's client b(that is part
 * of each service anyway) but the clash is avoided as its methods are void.
 * 
 * @author is.zharinov
 *
 */
@Configuration
@PropertySource(value="classpath:transport.properties", ignoreResourceNotFound=true)
public class SharedKafkaContext /*extends KafkaBootstrapConfiguration*/ {
	@Value("${kafka.bootstrap.address}")
	private String bootstrapAddress;
	
	@Value("${kafka.group.name}")
	@Deprecated
	private String groupName;
	
	@Value("${kafka.concurrent.listeners:1}")
	private int listenerConcurrency = 1;
	
	@Value("${clientId}")
	private UUID clientId;
	
	@Bean
	public static BeanFactoryPostProcessor propertySourcesPlaceholderConfigurer() {
		PropertySourcesPlaceholderConfigurer pspc =  new PropertySourcesPlaceholderConfigurer();
		pspc.setIgnoreUnresolvablePlaceholders(true);
		return pspc;
	}
	
	@SuppressWarnings({"rawtypes"})
	@Bean(name = KafkaListenerConfigUtils.KAFKA_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public KafkaListenerAnnotationBeanPostProcessor kafkaListenerAnnotationProcessor(KafkaTopicFactory _ka) {
		ReflectiveKafkaListenerFactory rklf = new ReflectiveKafkaListenerFactory();
		rklf.setTopicFactory(_ka);
		rklf.setPartitionFactor(listenerConcurrency);
		return rklf;
	}

	@Bean
	public KafkaTopicFactory _ka() {
		Map<String, Object> props = new HashMap<>();
		props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		return new KafkaTopicFactory(props);
	}
	
	@Bean(destroyMethod="close")
	public TransportEndpoint<HeaderContainer> inboundEndpoint() {
		return new AggregatingRoutingEndpoint(clientId);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public ConsumerFactory<?, ?> _kcf() {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupName);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
//		props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 6001);
//		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 6000);
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JavaSerializationDeserializer.class);
		
		return new DefaultKafkaConsumerFactory(props);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public ProducerFactory<?, ?> _kpf() {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
//		props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 6001);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JavaSerializationSerializer.class);
		
		return new DefaultKafkaProducerFactory(props);
	}
	
	@Bean
	public <K, V> KafkaTemplate<K, V> kt(ProducerFactory<K, V> _kpf) {
		return new KafkaTemplate<K, V>(_kpf);
	}
	
	@Bean
	public <K, V> KafkaListenerContainerFactory<?> kafkaListenerContainerFactory(ConsumerFactory<K, V> _kcf, RebalanceManager rm) {
		AckConcurrentKafkaListenerContainerFactory<K, V> klcf = new AckConcurrentKafkaListenerContainerFactory<K, V>();
		klcf.setConcurrency(this.listenerConcurrency);
		klcf.setConsumerFactory(_kcf);
//		klcf.setPollTimeout(1000l);
		klcf.setAckMode(AckMode.RECORD);
		//TODO: i don't think this is necessary anymore with single-listener-per-group policy
//		klcf.setRebalanceListener(rm);
		return klcf;
	}
	
	@Bean(name = KafkaListenerConfigUtils.KAFKA_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME)
	public KafkaListenerEndpointRegistry defaultKafkaListenerEndpointRegistry() {
		return new ConfigurableKafkaListenerEndpointRegistry();
	}
	
	@Bean
	public RebalanceManager rebalanceManager() {
		return new RebalanceManager();
	}
}
