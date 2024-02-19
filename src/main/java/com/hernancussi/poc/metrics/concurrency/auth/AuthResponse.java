package com.hernancussi.poc.metrics.concurrency.auth;

import lombok.Data;

@Data
public class AuthResponse {

  private final String token;

  public AuthResponse(final String token) {
    this.token = token;
  }
}
