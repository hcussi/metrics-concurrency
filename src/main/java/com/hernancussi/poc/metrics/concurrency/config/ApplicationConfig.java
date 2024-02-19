package com.hernancussi.poc.metrics.concurrency.config;

import com.hernancussi.poc.metrics.concurrency.cache.CacheStrategyType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "concurrency")
@Data
@Slf4j
public class ApplicationConfig {

  @Value("${fixed-pool-size:5}")
  private int fixedPoolSize;

  @Value("${cache-strategy-type:NONE}")
  private CacheStrategyType cacheStrategyType;

  @Value("${redis-host:localhost}")
  private String redisHost;

  @Value("${redis-port:6379}")
  private Integer redisPort;

  @Value("${redis-password:}")
  private String redisPassword;

  @Value("${ehcache-size:20}")
  private Integer ehCacheSize;

  @Value("${api-user-name:}")
  private String apiUserName;

  @Value("${api-user-pass:}")
  private String apiUserPass;

  @Value("${api-secret-key:}")
  private String apiSecretKey;

}
