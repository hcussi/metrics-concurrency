package com.hernancussi.poc.metrics.concurrency.config;

import com.hernancussi.poc.metrics.concurrency.cache.CacheStrategy;
import com.hernancussi.poc.metrics.concurrency.cache.ICacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;
import java.util.Optional;

@Configuration
@Slf4j
public class CacheConfig {

  @Bean
  public ICacheService<BigInteger> cacheFactorialService(@Autowired CacheStrategy<BigInteger> cacheStrategy) {
    if(cacheStrategy.mustUseCache()) {
      var cacheService = cacheStrategy.init(Optional.of("factorial"));
      if(log.isInfoEnabled()) {
        log.info(STR."Using cache strategy: \{cacheService.getName()} for factorial");
      }
      return cacheService;
    }
    return new ICacheService<>() {};
  }

  @Bean
  public ICacheService<BigInteger> cacheSquareService(@Autowired CacheStrategy<BigInteger> cacheStrategy) {
    if(cacheStrategy.mustUseCache()) {
      var cacheService = cacheStrategy.init(Optional.of("square"));
      if(log.isInfoEnabled()) {
        log.info(STR."Using cache strategy: \{cacheService.getName()} for square");
      }
      return cacheService;
    }
    return new ICacheService<>() {};
  }

}
