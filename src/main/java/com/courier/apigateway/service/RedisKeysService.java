package com.courier.apigateway.service;

import com.courier.apigateway.objects.dto.AuthInfoDto;

public interface RedisKeysService {

  void saveKeys(AuthInfoDto authInfoDto);

  String getPublicKey();

  String getAuthServiceSecret();

  boolean hasValidPublicKey();
}
