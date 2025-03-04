package com.courier.apigateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.courier.apigateway.objects.dto.ErrorLogDto;

@Service
public class EventProducerService {

  @Autowired private KafkaTemplate<String, ErrorLogDto> errorLogDtoTemplate;

  @Autowired private KafkaTemplate<String, Long> longKafkaTemplate;

  public void sendErrorLog(ErrorLogDto errorLogDto) {
    errorLogDtoTemplate.send("error-topic", errorLogDto);
  }

  public void sendUserIdBlacklist(Long userId) {
    longKafkaTemplate.send("user-disabled", userId);
  }
}
