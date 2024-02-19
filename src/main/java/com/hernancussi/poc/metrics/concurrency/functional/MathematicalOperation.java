package com.hernancussi.poc.metrics.concurrency.functional;

import java.math.BigInteger;
import java.util.AbstractMap;

@FunctionalInterface
public interface MathematicalOperation {

  AbstractMap.SimpleEntry<Integer, BigInteger> apply(final Integer number);
}
