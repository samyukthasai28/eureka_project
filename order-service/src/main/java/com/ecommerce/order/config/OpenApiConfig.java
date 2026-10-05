package com.ecommerce.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("REST API for Order creation, order tracking, and distributed Saga orchestration")
                        .version("v1.0.0")
                        .contact(new Contact().name("Pranav").email("pranavgandewar113@gmail.com"))
                        .license(new License().name("Apache 2.0").url("https://spring.io")));
    }
}
