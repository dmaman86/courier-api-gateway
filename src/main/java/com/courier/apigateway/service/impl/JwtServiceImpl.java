package com.courier.apigateway.service.impl;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.courier.apigateway.objects.dto.SecurityEventDto;
import com.courier.apigateway.service.JwtService;
import com.courier.apigateway.service.RedisKeysService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Service
public class JwtServiceImpl implements JwtService {

  @Autowired private RedisKeysService redisKeysService;

  @Override
  public boolean isTokenValid(String token) {
    return extractExpiration(token).after(new Date());
  }

  @Override
  public SecurityEventDto getSecurityEvent(String token) {
    Claims claims = parseTokenClaims(token);
    return SecurityEventDto.builder()
        .userId(claims.get("id", Long.class))
        .ip(claims.get("ip", String.class))
        .userAgent(claims.get("userAgent", String.class))
        .build();
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = parseTokenClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims parseTokenClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getPublicKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private PublicKey getPublicKey() {
    String publicKeyStr = redisKeysService.getPublicKey();

    try {
      byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return keyFactory.generatePublic(keySpec);

    } catch (Exception e) {
      throw new RuntimeException("Error loading public key from Redis", e);
    }
  }
}
