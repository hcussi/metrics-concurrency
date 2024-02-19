package com.hernancussi.poc.metrics.concurrency.cache;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Optional;

public class RedisCacheService<V> implements ICacheService<V> {

  private final RedisTemplate<String, V> redisTemplate;

  @Getter
  private final String namespace;

  public RedisCacheService(String host, Integer port, String password, String namespace) {
    var redisConfiguration = new RedisStandaloneConfiguration(host, port);
    redisConfiguration.setPassword(RedisPassword.of(password));
    var connectionFactory = new JedisConnectionFactory(redisConfiguration);
    connectionFactory.afterPropertiesSet();
    redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(connectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.afterPropertiesSet();
    this.namespace = namespace;
  }

  @Override
  public V get(@NonNull final String key) {
    return redisTemplate.opsForValue().get(STR."\{this.namespace}:\{key}");
  }

  @Override
  public boolean set(@NonNull final String key, final V value) {
    redisTemplate.opsForValue().set(STR."\{this.namespace}:\{key}", value);
    return true;
  }

  @Override
  public String getName() {
    return "REDIS";
  }

  @Override
  public void removeAll() {
    var keySet = Optional.ofNullable(redisTemplate.keys(STR."\{this.namespace}:*"));
    keySet.ifPresent(redisTemplate::delete);
  }

  @Override
  public Long getKeyCount() {
    var keySet = redisTemplate.keys(STR."\{this.namespace}:*");
    return keySet == null ? 0 : (long) keySet.size();
  }
}
