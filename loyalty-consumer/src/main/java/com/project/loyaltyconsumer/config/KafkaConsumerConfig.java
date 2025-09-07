package com.project.loyaltyconsumer.config;

import com.project.common.TransactionFinalizedDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, TransactionFinalizedDto> finalizedDtoConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "loyalty-group");

        JsonDeserializer<TransactionFinalizedDto> deserializer = new JsonDeserializer<>(TransactionFinalizedDto.class, false);
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionFinalizedDto> finalizedDtoContainerFactory() { // ❗️ Bean 이름 변경
        ConcurrentKafkaListenerContainerFactory<String, TransactionFinalizedDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(finalizedDtoConsumerFactory());
        return factory;
    }
}