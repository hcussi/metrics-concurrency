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
public class FactorialBusinessService extends MathBusinessService {

  private final ApplicationConfig applicationConfig;

  private final MathematicalOperation factorialProvider;

  @Autowired
  public FactorialBusinessService(ApplicationConfig applicationConfig, FactorialProvider factorialProvider) {
    this.applicationConfig = applicationConfig;
    this.factorialProvider = factorialProvider;
  }


  @HandleRestRequestMetric(
    counterName = "factorial_hit_counter", counterDescription = "Counter of Factorial Hits",
    timerName = "factorial_timer", timerDescription = "Timer of Factorial Hits"
  )
  public Map<Integer, BigInteger> calculateFactorial(List<Integer> numbers) {
    return calculate(numbers, factorialProvider);
  }

  @HandleRestRequestMetric(
    counterName = "factorial_hit_counter_threads", counterDescription = "Counter of Factorial Hits with Platform Threads",
    timerName = "factorial_timer_threads", timerDescription = "Timer of Factorial Hits with Platform Threads"
  )
  public Map<Integer, BigInteger> calculateFactorialThreads(List<Integer> numbers) throws InterruptedException {
    if (log.isInfoEnabled()) {
      log.info(STR."Using pool size of \{applicationConfig.getFixedPoolSize()}");
    }

    return calculateThreads(numbers, applicationConfig.getFixedPoolSize(), factorialProvider);
  }

  @HandleRestRequestMetric(
    counterName = "factorial_hit_counter_virtual", counterDescription = "Counter of Factorial Hits with Virtual Threads",
    timerName = "factorial_timer_virtual", timerDescription = "Timer of Factorial Hits with Virtual Threads"
  )
  public Map<Integer, BigInteger> calculateFactorialVirtual(List<Integer> numbers) throws InterruptedException, ExecutionException {
    return calculateVirtual(numbers, factorialProvider);
  }

}
