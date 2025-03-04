package com.courier.apigateway.config;

import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

@Service
public class RouterValidator {

  private static final Logger logger = LoggerFactory.getLogger(RouterValidator.class);

  private static final List<String> OPEN_ENDPOINTS =
      List.of("/api/auth/login", "/api/auth/set-password");

  public Predicate<ServerHttpRequest> isSecured =
      request -> {
        String path = request.getURI().getPath();
        boolean isSecure = OPEN_ENDPOINTS.stream().noneMatch(path::startsWith);

        logger.info("Evaluating security for request path: {}, is secure: {}", path, isSecure);
        return isSecure;
      };
}
