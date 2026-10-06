package com.ecommerce.gateway.controller;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "JWT Token Generation & Authentication APIs")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Value("${jwt.secret:ecommerce-microservices-super-secret-key-for-jwt-verification-minimum-256-bits!}")
    private String jwtSecret;

    @Operation(summary = "Generate Demo JWT Token", description = "Provides a quick signed JWT token for accessing secured microservice APIs")
    @GetMapping("/token")
    public Mono<ResponseEntity<Map<String, Object>>> generateToken(
            @RequestParam(name = "username", defaultValue = "customer@ecommerce.com") String username,
            @RequestParam(name = "role", defaultValue = "ROLE_USER") String role) {

        return Mono.fromCallable(() -> createJwtResponse(username, role))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Failed to generate JWT token: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.internalServerError().body(Map.of(
                            "error", "Failed to generate token",
                            "message", e.getMessage()
                    )));
                });
    }

    @Operation(summary = "Login / Token Exchange", description = "Accepts login credentials or role request and returns a signed JWT")
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody(required = false) Map<String, String> payload) {
        String username = (payload != null && payload.containsKey("username") && !payload.get("username").isBlank())
                ? payload.get("username")
                : "customer@ecommerce.com";
        String role = (payload != null && payload.containsKey("role") && !payload.get("role").isBlank())
                ? payload.get("role")
                : "ROLE_USER";

        return Mono.fromCallable(() -> createJwtResponse(username, role))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Failed to generate JWT login token: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.internalServerError().body(Map.of(
                            "error", "Login failed",
                            "message", e.getMessage()
                    )));
                });
    }

    private Map<String, Object> createJwtResponse(String username, String role) throws Exception {
        JWSSigner signer = new MACSigner(jwtSecret.getBytes(StandardCharsets.UTF_8));
        long ttlMillis = 24 * 60 * 60 * 1000L; // 24 hours
        Date expiryDate = new Date(System.currentTimeMillis() + ttlMillis);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .issuer("ecommerce-auth")
                .expirationTime(expiryDate)
                .claim("roles", new String[]{role})
                .claim("username", username)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);
        String token = signedJWT.serialize();

        log.info("Issued JWT for user: {} with role: {}", username, role);

        return Map.of(
                "token", token,
                "type", "Bearer",
                "username", username,
                "role", role,
                "expiresInSeconds", 86400,
                "expiresAt", expiryDate.toInstant().toString()
        );
    }
}
