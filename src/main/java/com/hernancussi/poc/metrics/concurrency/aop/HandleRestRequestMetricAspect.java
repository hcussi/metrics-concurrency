package com.hernancussi.poc.metrics.concurrency.aop;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class HandleRestRequestMetricAspect extends LoggerAspect {

  private final MeterRegistry meterRegistry;

  private static final Map<String, Counter> COUNTER_MAP = new ConcurrentHashMap<>();

  private static final Map<String, Timer> TIMER_MAP = new ConcurrentHashMap<>();

  public HandleRestRequestMetricAspect(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Around(value = "@annotation(handleRestRequestMetric)")
  public Object processAround(ProceedingJoinPoint joinPoint, HandleRestRequestMetric handleRestRequestMetric) throws Throwable {
    var log = getLogger(joinPoint);

    var counter = COUNTER_MAP.computeIfAbsent(handleRestRequestMetric.counterName(), (name) -> {
        if(log.isInfoEnabled()) {
          log.info(STR."Creating counter for \{name}");
        }
        return Counter.builder(name)
          .description(handleRestRequestMetric.counterDescription())
          .register(meterRegistry);
      }
    );
    var timer = TIMER_MAP.computeIfAbsent(handleRestRequestMetric.timerName(), (name) -> {
        if(log.isInfoEnabled()) {
          log.info(STR."Creating timer for \{name}");
        }
        return Timer.builder(name)
          .description(handleRestRequestMetric.timerDescription())
          .publishPercentiles(0.90, 0.95, 0.99)
          .publishPercentileHistogram()
          .register(meterRegistry);
      }
    );

    return timer.record(() -> {
      counter.increment();

      try {
        return joinPoint.proceed();
      } catch (Throwable e) {
        if(log.isErrorEnabled()) {
          log.error("Failed to proceed", e);
        }
        throw new RuntimeException("Failed to proceed");
      }
    });
  }

}
