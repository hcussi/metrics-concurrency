package com.hernancussi.poc.metrics.concurrency.aop;

import com.hernancussi.poc.metrics.concurrency.business.CacheServiceProvider;
import org.aspectj.lang.ProceedingJoinPoint;

import java.math.BigInteger;
import java.util.AbstractMap;

public abstract class HandleCacheMathAspect extends CacheServiceProvider<BigInteger> {

  protected Object aroundMath(ProceedingJoinPoint joinPoint) throws Throwable {
    Integer number = (Integer) joinPoint.getArgs()[0];
    var log = getLogger(joinPoint);

    if (get(number.toString()) != null) {
      return new AbstractMap.SimpleEntry<>(number, get(number.toString()));
    }

    AbstractMap.SimpleEntry<Integer, BigInteger> entry = (AbstractMap.SimpleEntry<Integer, BigInteger>) joinPoint.proceed();

    if (set(entry.getKey().toString(), entry.getValue()) && log.isInfoEnabled()) {
      log.info(STR."[\{cacheService.getName()}:\{cacheService.getNamespace()}:\{entry.getKey()}] Store \"\{entry.getValue()}\" in cache");
    }

    return entry;
  }

}
