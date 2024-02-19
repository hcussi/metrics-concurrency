package com.hernancussi.poc.metrics.concurrency.business;

import com.hernancussi.poc.metrics.concurrency.aop.HandleCacheSquare;
import com.hernancussi.poc.metrics.concurrency.functional.MathematicalOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.AbstractMap;

@Component
@Slf4j
public class SquareProvider implements MathematicalOperation {

  @Override
  @HandleCacheSquare
  public AbstractMap.SimpleEntry<Integer, BigInteger> apply(final Integer number) {
    var total = BigInteger.valueOf((long) number * number);

    return new AbstractMap.SimpleEntry<>(number, total);
  }
}
