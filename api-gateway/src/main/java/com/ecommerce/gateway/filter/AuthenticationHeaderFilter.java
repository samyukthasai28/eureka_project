package com.ecommerce.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationHeaderFilter extends AbstractGatewayFilterFactory<AuthenticationHeaderFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationHeaderFilter.class);

    public AuthenticationHeaderFilter() {
        super(Config.class);
    }

    public static class Config {
        private boolean requireAuth = false;

        public boolean isRequireAuth() {
            return requireAuth;
        }

        public void setRequireAuth(boolean requireAuth) {
            this.requireAuth = requireAuth;
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst("Authorization");

            if (config.isRequireAuth() && (authHeader == null || !authHeader.startsWith("Bearer "))) {
                log.warn("[AUTH-FILTER] Unauthorized request to protected path: {}", request.getURI().getPath());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-Forwarded-Auth", "true")
                        .build();
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }

            return chain.filter(exchange);
        };
    }
}