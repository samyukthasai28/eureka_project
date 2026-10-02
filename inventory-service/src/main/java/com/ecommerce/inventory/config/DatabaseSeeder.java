package com.ecommerce.inventory.config;

import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DatabaseSeeder {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    @Bean
    public CommandLineRunner loadInitialData(InventoryRepository inventoryRepository) {
        return args -> {
            if (inventoryRepository.count() == 0) {
                log.info("[INVENTORY-DB] Seeding default inventory items...");
                List<Inventory> items = List.of(
                        Inventory.builder().skuCode("iphone_15").quantity(100).reservedQuantity(0).unitPrice(new BigDecimal("799.99")).build(),
                        Inventory.builder().skuCode("macbook_pro_16").quantity(50).reservedQuantity(0).unitPrice(new BigDecimal("2499.00")).build(),
                        Inventory.builder().skuCode("sony_wh1000xm5").quantity(75).reservedQuantity(0).unitPrice(new BigDecimal("399.99")).build(),
                        Inventory.builder().skuCode("airpods_pro").quantity(120).reservedQuantity(0).unitPrice(new BigDecimal("249.00")).build(),
                        Inventory.builder().skuCode("samsung_s24_ultra").quantity(0).reservedQuantity(0).unitPrice(new BigDecimal("1199.99")).build()
                );
                inventoryRepository.saveAll(items);
                log.info("[INVENTORY-DB] Successfully loaded {} initial inventory items.", items.size());
            }
        };
    }
}