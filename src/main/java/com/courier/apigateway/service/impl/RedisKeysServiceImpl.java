package com.courier.apigateway.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courier.apigateway.objects.dto.AuthInfoDto;
import com.courier.apigateway.service.RedisKeysService;

@Service
public class RedisKeysServiceImpl implements RedisKeysService {

  @Autowired private StringRedisTemplate redisTemplate;

  private static final String PUBLIC_KEY = "publicKey";
  private static final String AUTH_SERVICE = "authServiceSecret";

  @Override
  @Transactional
  public void saveKeys(AuthInfoDto authInfoDto) {
    redisTemplate.opsForValue().set(PUBLIC_KEY, authInfoDto.getPublicKey());
    redisTemplate.opsForValue().set(AUTH_SERVICE, authInfoDto.getAuthServiceSecret());
  }

  @Override
  public String getPublicKey() {
    return redisTemplate.opsForValue().get(PUBLIC_KEY);
  }

  @Override
  public String getAuthServiceSecret() {
    return redisTemplate.opsForValue().get(AUTH_SERVICE);
  }

  @Override
  public boolean hasValidPublicKey() {
    return redisTemplate.hasKey(PUBLIC_KEY);
  }
}
