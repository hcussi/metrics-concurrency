package com.hernancussi.poc.metrics.concurrency.business;

import com.hernancussi.poc.metrics.concurrency.aop.HandleCacheFactorial;
import com.hernancussi.poc.metrics.concurrency.functional.MathematicalOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.AbstractMap;

@Component
@Slf4j
public class FactorialProvider implements MathematicalOperation {

  @Override
  @HandleCacheFactorial
  public AbstractMap.SimpleEntry<Integer, BigInteger> apply(final Integer number) {
    var total = BigInteger.ONE;

    for (int n = number; n > 0; n--) {
      total = total.multiply(BigInteger.valueOf(n));
    }

    return new AbstractMap.SimpleEntry<>(number, total);
  }

}
