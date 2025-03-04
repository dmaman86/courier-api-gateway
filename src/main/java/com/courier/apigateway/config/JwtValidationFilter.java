package com.courier.apigateway.config;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.courier.apigateway.objects.dto.ErrorLogDto;
import com.courier.apigateway.objects.dto.SecurityEventDto;
import com.courier.apigateway.objects.enums.ErrorSeverity;
import com.courier.apigateway.service.BlackListService;
import com.courier.apigateway.service.EventProducerService;
import com.courier.apigateway.service.JwtService;
import com.courier.apigateway.service.RedisKeysService;

import reactor.core.publisher.Mono;

@Component
public class JwtValidationFilter implements GlobalFilter, Ordered {

  private static final Logger logger = LoggerFactory.getLogger(JwtValidationFilter.class);

  @Autowired private BlackListService blackListService;

  @Autowired private JwtService jwtService;

  @Autowired private RedisKeysService redisKeysService;

  @Autowired private EventProducerService eventProducerService;

  @Autowired private RouterValidator routerValidator;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

    if (redisKeysService.getPublicKey() == null) {
      return unauthorizedResponse(exchange, "Public key is not available");
    }

    ServerHttpRequest request = exchange.getRequest();
    String requestPath = request.getURI().getPath();

    if (!routerValidator.isSecured.test(request)) {
      return chain.filter(exchange);
    }

    String token = extractTokenFromCookies(request);
    if (token == null || token.isEmpty() || !jwtService.isTokenValid(token)) {
      return unauthorizedResponse(exchange, "Invalid token");
    }

    String clientIp = getClientIp(request);
    String clientUserAgent = request.getHeaders().getFirst(HttpHeaders.USER_AGENT);

    SecurityEventDto securityEventDto = jwtService.getSecurityEvent(token);

    if (blackListService.isUserBlackListed(securityEventDto.getUserId())) {
      sendSecurityAlert(securityEventDto, requestPath, "User in blacklisted");
      return unauthorizedResponse(exchange, "User is not authorized");
    }

    if (isSuspiciousRequest(securityEventDto, clientIp, clientUserAgent)) {
      logger.warn(
          "Detected malicious request from user: {} with ip: {} and user agent: {}",
          securityEventDto.getUserId(),
          clientIp,
          clientUserAgent);
      blackListService.handleUserIdEvent(securityEventDto.getUserId());
      eventProducerService.sendUserIdBlacklist(securityEventDto.getUserId());
      sendSecurityAlert(securityEventDto, requestPath, "User ip or user agent mismatch");
      return unauthorizedResponse(exchange, "Detected malicious request");
    }

    return chain.filter(exchange);
  }

  private void sendSecurityAlert(SecurityEventDto securityEventDto, String path, String reason) {
    ErrorLogDto errorLog =
        ErrorLogDto.builder()
            .timestamp(LocalDateTime.now())
            .status(401) // Unauthorized
            .error("Malicious Request Detected")
            .message(
                "User with id: "
                    + securityEventDto.getUserId()
                    + " tried to access: "
                    + path
                    + " with reason: "
                    + reason)
            .path(path)
            .exception("SecurityViolationException")
            .severity(ErrorSeverity.CRITICAL)
            .build();

    eventProducerService.sendErrorLog(errorLog);
    logger.warn("Security alert event send to error-service: {}", errorLog);
  }

  private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String reason) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }

  private String getClientIp(ServerHttpRequest request) {
    return Optional.ofNullable(request.getHeaders().getFirst("X-Forwarded-For"))
        .map(ip -> ip.split(",")[0])
        .orElseGet(() -> request.getRemoteAddress().getAddress().getHostAddress());
  }

  private String extractTokenFromCookies(ServerHttpRequest request) {
    return Optional.ofNullable(request.getCookies().getFirst("accessToken"))
        .map(HttpCookie::getValue)
        .orElse(null);
  }

  private boolean isSuspiciousRequest(
      SecurityEventDto securityEventDto, String clientIp, String clientUserAgent) {
    return !securityEventDto.getIp().equals(clientIp)
        || !securityEventDto.getUserAgent().equals(clientUserAgent);
  }

  @Override
  public int getOrder() {
    return -10;
  }
}
