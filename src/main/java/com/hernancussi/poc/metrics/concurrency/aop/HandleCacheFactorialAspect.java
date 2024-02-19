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
public class HandleCacheFactorialAspect extends HandleCacheMathAspect {

  public HandleCacheFactorialAspect(ICacheService<BigInteger> cacheFactorialService) {
    this.cacheService = cacheFactorialService;
  }

  // Define the pointcut for HandleCacheFactorial annotation
  @Pointcut("@annotation(com.hernancussi.poc.metrics.concurrency.aop.HandleCacheFactorial)")
  public void handleAnnotationPointcut() {
  }

  @Around(value = "handleAnnotationPointcut()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    return super.aroundMath(joinPoint);
  }

}
