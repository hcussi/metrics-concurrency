package com.hernancussi.poc.metrics.concurrency.cache;

import com.hernancussi.poc.metrics.concurrency.config.ApplicationConfig;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class CacheStrategy<V> {

  private final ApplicationConfig applicationConfig;

  private final MeterRegistry meterRegistry;

  @Setter @Autowired(required = false)
  private JedisConnectionFactory redisConnectionFactory;

  private final Map<String, CacheCounterEntry<V>> cacheMap = new HashMap<>();

  @Autowired
  public CacheStrategy(ApplicationConfig applicationConfig, MeterRegistry meterRegistry) {
    this.applicationConfig = applicationConfig;
    this.meterRegistry = meterRegistry;
  }

  /**
   * Initialize the cache with the configuration set
   * @param name The name of the cache to identify it
   * @return The cache instance to use
   */
  public ICacheService<V> init(final Optional<String> name) {
    Optional<ICacheService<V>> cacheService = Optional.empty();

    switch (applicationConfig.getCacheStrategyType()) {
      case REDIS -> cacheService = Optional.of(new RedisCacheService<V>(
        redisConnectionFactory,
        name.orElse("default")
      ));
      case EHCACHE -> cacheService = Optional.of(new EhCacheService<>(name.orElse("default"), applicationConfig.getEhCacheSize()));
      case NONE -> {}
    }

    cacheService.ifPresent((cache) -> {
      var gauge = Gauge.builder(STR."\{cache.getNamespace()}_key_count", cache::getKeyCount)
        .description("Actual Key Count")
        .register(meterRegistry);
      var cacheEntry = new CacheCounterEntry<>(gauge, cache);
      cacheMap.putIfAbsent(name.orElse("default"), cacheEntry);
    });

    return cacheService.orElse(null);
  }

  /**
   * Returns <code>true</code> if any cache strategy has been configured
   * @return <code>true</code> if any cache strategy has been configured
   */
  public boolean mustUseCache() {
    return (
      applicationConfig.getCacheStrategyType() != null
      && !applicationConfig.getCacheStrategyType().equals(CacheStrategyType.NONE)
    );
  }


  /**
   * Evict all keys from any given cache strategy immediately
   */
  public void clear() {
    if(log.isInfoEnabled()) {
      log.info("Clearing Cache");
    }
    cacheMap.values().forEach(entry -> entry.cacheService().removeAll());
  }

  /**
   * Evict all keys from any given cache strategy after 30 minutes
   */
  @Scheduled(fixedRate = 1000 * 60 * 30)
  public void scheduleClear() {
    clear();
  }
}

record CacheCounterEntry<V>(Gauge counter, ICacheService<V> cacheService) {}
