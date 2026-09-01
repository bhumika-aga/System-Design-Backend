// System Design - Backend
// Chapter 18, Scaling & Performance -> 11 Connection pooling
// Java 21 / Spring Boot 3.3

package com.example.scaling.pooling;

// Spring Boot already pools connections with HikariCP. Most of the
// time you tune it in application.yaml rather than in Java:
//
//   spring.datasource.hikari:
//     maximum-pool-size: 25    # never open more than this
//     minimum-idle: 10         # keep this many ready to go
//     max-lifetime: 300000     # recycle a connection after 5 min
//     idle-timeout: 60000      # drop an idle one after 1 min
//
// Build it yourself only when the values are not static:
@Configuration
class PoolConfig {

    @Bean
    DataSource dataSource(DataSourceProperties props) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(props.getUrl());
        cfg.setUsername(props.getUsername());
        cfg.setPassword(props.getPassword());

        cfg.setMaximumPoolSize(25);
        cfg.setMinimumIdle(10);
        cfg.setMaxLifetime(Duration.ofMinutes(5).toMillis());
        cfg.setIdleTimeout(Duration.ofMinutes(1).toMillis());

        return new HikariDataSource(cfg);
    }
}
