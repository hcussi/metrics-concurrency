package com.hernancussi.poc.metrics.concurrency.controllers;

import com.hernancussi.poc.metrics.concurrency.auth.AuthRequest;
import com.hernancussi.poc.metrics.concurrency.auth.AuthResponse;
import com.hernancussi.poc.metrics.concurrency.auth.JwtUtil;
import com.hernancussi.poc.metrics.concurrency.auth.SecretResponse;
import com.hernancussi.poc.metrics.concurrency.config.ApplicationConfig;
import com.hernancussi.poc.metrics.concurrency.exception.InvalidUserCredentialsException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;

@RestController
@RequestMapping("api/v1")
@Tag(name = "Authentication", description = "Authentication endpoint to obtain access to the API")
@SecurityRequirement(name = "basicAuth")
public class AuthenticationController {

  private final JwtUtil jwtUtil;
  private final ApplicationConfig applicationConfig;

  @Autowired
  public AuthenticationController(JwtUtil jwtUtil, ApplicationConfig applicationConfig) {
    this.jwtUtil = jwtUtil;
    this.applicationConfig = applicationConfig;
  }

  @GetMapping(value = "/generateSecret", produces = "application/json")
  @Operation(
    summary = "Generate Secret",
    description = "Generate random secret string using hash algorithm HMACSHA512. Will be used to sign JWT token")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "successful operation")
  })
  public ResponseEntity<SecretResponse> generateToken() {
    SecretKey key = Jwts.SIG.HS512.key().build();
    var secretResponse = SecretResponse.builder().secretKey(Encoders.BASE64.encode(key.getEncoded())).build();
    return new ResponseEntity<>(secretResponse, HttpStatus.ACCEPTED);
  }

  @PostMapping("/authentication")
  @Operation(
    summary = "Generate JWT token",
    description = "Generate JWT token using API user credentials")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "successful operation")
  })
  public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest authRequest) {
    try {
      authRequest.validate();
      if (!applicationConfig.getApiUserPass().equals(authRequest.getPassword())) {
        throw new InvalidUserCredentialsException("Invalid username or password");
      }
      var token = jwtUtil.generateToken(authRequest.getUsername());
      jwtUtil.validateToken(token, applicationConfig.getApiUserName());
      AuthResponse response = new AuthResponse(token);

      return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
    catch (JwtException ex) {
      throw new InvalidUserCredentialsException("Invalid username or password");
    }

  }

}
