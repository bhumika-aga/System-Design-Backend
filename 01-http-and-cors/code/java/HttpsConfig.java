// System Design - Backend
// Chapter 01, HTTP & CORS -> What the handshake actually achieves
// Java 21 / Spring Boot 3.3

package com.example.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@RestController
class HttpsConfig {

    // Serve HTTPS directly from a keystore.
    // src/main/resources/application.yml:
    //
    // server:
    // port: 443
    // ssl:
    // key-store: classpath:keystore.p12
    // key-store-type: PKCS12
    // key-store-password: ${KEYSTORE_PASSWORD} # from the environment
    // key-alias: server

    // And refuse to answer over plain HTTP at all:
    @Bean
    SecurityFilterChain https(HttpSecurity http) throws Exception {
        http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        return http.build();
    }
    // Production note: you usually terminate TLS at a proxy or load balancer
    // and run plain HTTP behind it, with
    // server.forward-headers-strategy=framework so that X-Forwarded-Proto
    // and X-Forwarded-For are honoured.
}
