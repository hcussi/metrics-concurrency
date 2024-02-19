package com.hernancussi.poc.metrics.concurrency.cache;

import jakarta.annotation.PreDestroy;
import lombok.NonNull;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.core.internal.statistics.DefaultStatisticsService;
import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.core.statistics.CacheStatistics;

import java.util.HashSet;
import java.util.Set;

public class EhCacheService<V> implements ICacheService<V> {

  private volatile Cache<String, V> cache;

  private final CacheManager cacheManager;

  private final StatisticsService statisticsService;

  private final String name;

  private final Set<String> keys = new HashSet<>();

  public EhCacheService(@NonNull String name, Integer size) {
    this.name = name;
    statisticsService = new DefaultStatisticsService();
    cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
      .using(statisticsService)
      .build();
    cacheManager.init();
    // JSR-107 with ehcache provider
    // CachingProvider provider = Caching.getCachingProvider();
    // CacheManager cacheManager = provider.getCacheManager();

    /*MutableConfiguration<String, V> configuration =
      new MutableConfiguration<String, V>()
        .setStoreByValue(false)
        .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ETERNAL));*/

    // Using ehCache classes in order to use cache statistics
    CacheConfiguration<String, V> configuration = (CacheConfiguration<String, V>) CacheConfigurationBuilder.newCacheConfigurationBuilder(
      String.class, Object.class, ResourcePoolsBuilder.newResourcePoolsBuilder().heap(size, EntryUnit.ENTRIES).build()
    ).build();
    cache = cacheManager.createCache(name, configuration);
  }

  @PreDestroy
  public void destroy() {
    cacheManager.close();
  }

  @Override
  public V get(@NonNull final String key) {
    return cache.get(key);
  }

  @Override
  public boolean set(@NonNull final String key, @NonNull final V value) {
    cache.put(key, value);
    keys.add(key);
    return true;
  }

  @Override
  public String getName() {
    return "EHCACHE";
  }

  @Override
  public String getNamespace() {
    return this.name;
  }

  @Override
  public void removeAll() {
    keys.forEach(cache::remove);
    keys.clear();
  }

  @Override
  public Long getKeyCount() {
    CacheStatistics ehCacheStat = statisticsService.getCacheStatistics(name);
    return ehCacheStat.getCachePuts() - ehCacheStat.getCacheRemovals();
  }
}
