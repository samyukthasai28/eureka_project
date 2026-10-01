package com.ecommerce.gateway;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ApiGatewaySecurityTest {

    @Autowired
    private WebTestClient webTestClient;

    @Value("${jwt.secret:ecommerce-microservices-super-secret-key-for-jwt-verification-minimum-256-bits!}")
    private String jwtSecret;

    private String generateToken(long ttlMillis) throws Exception {
        JWSSigner signer = new MACSigner(jwtSecret.getBytes(StandardCharsets.UTF_8));
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("test-user")
                .issuer("ecommerce-auth")
                .expirationTime(new Date(System.currentTimeMillis() + ttlMillis))
                .claim("roles", new String[]{"ROLE_USER"})
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    @Test
    void healthEndpointIsPermittedWithoutAuth() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void protectedEndpointWithoutAuthReturns401() {
        webTestClient.get()
                .uri("/api/orders/1")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedEndpointWithInvalidTokenReturns401() {
        webTestClient.get()
                .uri("/api/orders/1")
                .header("Authorization", "Bearer invalid-token-string")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedEndpointWithValidTokenPassesAuth() throws Exception {
        String token = generateToken(60000);
        webTestClient.get()
                .uri("/api/orders/1")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().value(status -> Assertions.assertNotEquals(401, status));
    }
}