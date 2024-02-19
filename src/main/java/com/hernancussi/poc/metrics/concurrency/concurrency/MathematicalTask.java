package com.hernancussi.poc.metrics.concurrency.concurrency;

import com.hernancussi.poc.metrics.concurrency.functional.MathematicalOperation;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.Callable;

@Setter
public class MathematicalTask implements Callable<Map.Entry<Integer, BigInteger>> {

    private Integer number;

    private MathematicalOperation mathematicalOperation;

    public MathematicalTask(Integer number, MathematicalOperation mathematicalOperation) {
        this.number = number;
        this.mathematicalOperation = mathematicalOperation;
    }

    @Override
    public Map.Entry<Integer, BigInteger> call() {
        return mathematicalOperation.apply(number);
    }
}
