package com.ecommerce.inventory.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class InventoryTopicConfig {

    @Value("${app.kafka.topics.inventory-events:inventory-events-topic}")
    private String inventoryEventsTopic;

    @Bean
    public NewTopic inventoryEventsTopic() {
        return TopicBuilder.name(inventoryEventsTopic)
                .partitions(3)
                .replicas(1)
                .config("min.insync.replicas", "1")
                .config("cleanup.policy", "delete")
                .config("retention.ms", "604800000") // 7 days
                .build();
    }
}