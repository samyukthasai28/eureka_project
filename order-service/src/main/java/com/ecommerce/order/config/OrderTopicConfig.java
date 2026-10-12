package com.ecommerce.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class OrderTopicConfig {

    @Value("${app.kafka.topics.order-placed:order-placed-topic}")
    private String orderPlacedTopic;

    @Bean
    public NewTopic orderPlacedTopic() {
        return TopicBuilder.name(orderPlacedTopic)
                .partitions(3)
                .replicas(1)
                .config("min.insync.replicas", "1")
                .config("cleanup.policy", "delete")
                .config("retention.ms", "604800000") // 7 days
                .build();
    }
}