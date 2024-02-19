package com.hernancussi.poc.metrics.concurrency.auth;

import com.hernancussi.poc.metrics.concurrency.input.InputValidation;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class AuthRequest implements InputValidation {

  private String username;

  private String password;

  @Override
  public void validate() {
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("Username is required");
    }
    if (StringUtils.isBlank(password)) {
      throw new IllegalArgumentException("Password is required");
    }
  }
}
