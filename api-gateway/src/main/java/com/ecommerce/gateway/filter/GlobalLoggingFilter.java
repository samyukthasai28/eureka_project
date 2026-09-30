package com.ecommerce.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GlobalLoggingFilter.class);
    private static final String START_TIME = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = UUID.randomUUID().toString();
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-Request-Id", requestId)
                .build();

        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(START_TIME, startTime);

        log.info("[GATEWAY-REQ] ID={} | Method={} | Path={} | ClientIP={}",
                requestId,
                request.getMethod(),
                request.getURI().getPath(),
                request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "UNKNOWN");

        return chain.filter(exchange.mutate().request(request).build()).then(Mono.fromRunnable(() -> {
            Long start = exchange.getAttribute(START_TIME);
            long duration = start != null ? System.currentTimeMillis() - start : 0;
            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().add("X-Request-Id", requestId);
            response.getHeaders().add("X-Response-Time", duration + "ms");

            log.info("[GATEWAY-RESP] ID={} | Status={} | Latency={}ms",
                    requestId,
                    response.getStatusCode(),
                    duration);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}