package com.hernancussi.poc.metrics.concurrency.auth;

import com.hernancussi.poc.metrics.concurrency.config.ApplicationConfig;
import com.hernancussi.poc.metrics.concurrency.exception.InvalidJwtTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

  private final String TOKEN_HEADER = "Authorization";
  private final String TOKEN_PREFIX = "Bearer ";
  private final SecretKey secretKey;

  @Autowired
  public JwtUtil(ApplicationConfig applicationConfig) {
    secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(applicationConfig.getApiSecretKey()));
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }
  private Claims extractAllClaims(String token) throws JwtException {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token).getPayload();
  }

  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public String generateToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, username);
  }

  private String createToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
      .claims(claims)
      .subject(subject)
      .issuedAt(new Date(System.currentTimeMillis()))
      .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
      .signWith(secretKey)
      .compact();
  }

  public String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(TOKEN_HEADER);
    if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
      return bearerToken.substring(TOKEN_PREFIX.length());
    }
    return null;
  }

  public void validateToken(String token, String username) {
    final String tokenUsername = extractUsername(token);
    if (!tokenUsername.equals(username) || isTokenExpired(token)) {
      if(log.isWarnEnabled()) {
        log.warn(STR."Invalid token has been provided for \{username}");
      }
      throw new InvalidJwtTokenException(STR."Invalid token has been provided for \{username}");
    }
  }
}
