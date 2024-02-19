package com.hernancussi.poc.metrics.concurrency.business;

import com.hernancussi.poc.metrics.concurrency.aop.HandleRestRequestMetric;
import com.hernancussi.poc.metrics.concurrency.config.ApplicationConfig;
import com.hernancussi.poc.metrics.concurrency.functional.MathematicalOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class SquareBusinessService extends MathBusinessService {

  private final ApplicationConfig applicationConfig;

  private final MathematicalOperation squareProvider;

  @Autowired
  public SquareBusinessService(ApplicationConfig applicationConfig, SquareProvider squareProvider) {
    this.applicationConfig = applicationConfig;
    this.squareProvider = squareProvider;
  }

  @HandleRestRequestMetric(
    counterName = "square_hit_counter", counterDescription = "Counter of Square Hits",
    timerName = "square_timer", timerDescription = "Timer of Square Hits"
  )
  public Map<Integer, BigInteger> calculateSquare(List<Integer> numbers) {
    return calculate(numbers, squareProvider);
  }

  @HandleRestRequestMetric(
    counterName = "square_hit_counter_threads", counterDescription = "Counter of Square Hits with Platform Threads",
    timerName = "square_timer_threads", timerDescription = "Timer of Square Hits with Platform Threads"
  )
  public Map<Integer, BigInteger> calculateSquareThreads(List<Integer> numbers) throws InterruptedException {
    if (log.isInfoEnabled()) {
      log.info(STR."Using pool size of \{applicationConfig.getFixedPoolSize()}");
    }

    return calculateThreads(numbers, applicationConfig.getFixedPoolSize(), squareProvider);
  }

  @HandleRestRequestMetric(
    counterName = "square_hit_counter_virtual", counterDescription = "Counter of Square Hits with Virtual Threads",
    timerName = "square_timer_virtual", timerDescription = "Timer of Square Hits with Virtual Threads"
  )
  public Map<Integer, BigInteger> calculateSquareVirtual(List<Integer> numbers) throws InterruptedException, ExecutionException {
    return calculateVirtual(numbers, squareProvider);
  }

}
