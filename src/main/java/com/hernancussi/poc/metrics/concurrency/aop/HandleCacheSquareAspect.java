package com.hernancussi.poc.metrics.concurrency.aop;

import com.hernancussi.poc.metrics.concurrency.cache.ICacheService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Aspect
@Component
public class HandleCacheSquareAspect extends HandleCacheMathAspect {

  public HandleCacheSquareAspect(ICacheService<BigInteger> cacheSquareService) {
    this.cacheService = cacheSquareService;
  }

  // Define the pointcut for HandleCacheSquare annotation
  @Pointcut("@annotation(com.hernancussi.poc.metrics.concurrency.aop.HandleCacheSquare)")
  public void handleAnnotationPointcut() {
  }

  @Around(value = "handleAnnotationPointcut()")
  public Object processAround(ProceedingJoinPoint joinPoint) throws Throwable {
    return super.aroundMath(joinPoint);
  }

}
