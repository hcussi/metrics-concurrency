package com.hernancussi.poc.metrics.concurrency.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

@Configuration
class RedisConfiguration {

  private final ApplicationConfig applicationConfig;

  RedisConfiguration(ApplicationConfig applicationConfig) {
    this.applicationConfig = applicationConfig;
  }

  @Bean
  public JedisConnectionFactory redisConnectionFactory() {
    var redisConfiguration = new RedisStandaloneConfiguration(applicationConfig.getRedisHost(), applicationConfig.getRedisPort());
    redisConfiguration.setPassword(RedisPassword.of(applicationConfig.getRedisPassword()));
    var connectionFactory = new JedisConnectionFactory(redisConfiguration);
    connectionFactory.afterPropertiesSet();

    return connectionFactory;
  }
}
