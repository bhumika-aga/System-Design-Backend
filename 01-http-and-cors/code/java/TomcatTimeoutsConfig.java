// System Design - Backend
// Chapter 01, HTTP & CORS -> The mechanics and the knobs
// Java 21 / Spring Boot 3.3

package com.example.http;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;

@RestController
class TomcatTimeoutsConfig {
    
    // These are server knobs, so they belong in application.yml:
    //
    // server:
    // tomcat:
    // connection-timeout: 5s # time allowed to send the headers
    // keep-alive-timeout: 60s # how long an idle conn is held
    // max-keep-alive-requests: 100 # then the server sends close
    // threads:
    // max: 200
    //
    // connection-timeout is the Slowloris defense: it caps how long a client
    // may take to finish sending its headers.
    
    // When you need the programmatic hook instead:
    @Bean
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> timeouts() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            connector.setProperty("connectionTimeout", "5000");
            connector.setProperty("keepAliveTimeout", "60000");
            connector.setProperty("maxKeepAliveRequests", "100");
        });
    }
}
