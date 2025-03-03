package com.courier.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

  @Bean
  public RouteLocator routes(RouteLocatorBuilder builder) {
    return builder
        .routes()
        .route(
            "auth-service",
            r -> r.path("/api/auth/**", "/api/credential/**").uri("lb://courier-auth-service"))
        .route("user-service", r -> r.path("/api/user/**").uri("lb://courier-user-service"))
        .route(
            "resource-service",
            r -> r.path("/api/resource/**").uri("lb://courier-resource-service"))
        .build();
  }
}
