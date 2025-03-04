package com.courier.apigateway.objects.dto;

import java.time.LocalDateTime;

import com.courier.apigateway.objects.enums.ErrorSeverity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorLogDto {

  private LocalDateTime timestamp;
  private int status;
  private String error;
  private String message;
  private String path;
  private String exception;

  private ErrorSeverity severity;
}
