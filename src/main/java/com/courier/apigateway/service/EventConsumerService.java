package com.courier.apigateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.courier.apigateway.objects.dto.AuthInfoDto;

@Component
public class EventConsumerService {

  @Autowired private RedisKeysService redisKeysService;

  @KafkaListener(topics = "public-key", groupId = "courier-api-gateway-group")
  public void listenKeys(AuthInfoDto authInfoDto) {
    redisKeysService.saveKeys(authInfoDto);
  }
}
