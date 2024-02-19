package com.hernancussi.poc.metrics.concurrency.business;

import com.hernancussi.poc.metrics.concurrency.aop.LoggerAspect;
import com.hernancussi.poc.metrics.concurrency.cache.ICacheService;
import lombok.NonNull;

public abstract class CacheServiceProvider<V> extends LoggerAspect {

  protected ICacheService<V> cacheService;

  protected V get(@NonNull String key) {
    return cacheService.get(key);
  }

  protected boolean set(@NonNull String key, @NonNull V value) {
    return cacheService.set(key, value);
  }

}
