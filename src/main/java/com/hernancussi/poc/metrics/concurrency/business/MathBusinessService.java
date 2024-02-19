package com.hernancussi.poc.metrics.concurrency.business;

import com.hernancussi.poc.metrics.concurrency.concurrency.MathematicalTask;
import com.hernancussi.poc.metrics.concurrency.functional.MathematicalOperation;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Collectors;

public abstract class MathBusinessService {

  public Map<Integer, BigInteger> calculate(List<Integer> numbers, MathematicalOperation mathematicalOperation) {
    return numbers.stream().map(mathematicalOperation::apply).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public Map<Integer, BigInteger> calculateThreads(List<Integer> numbers, int fixedPoolSize, MathematicalOperation mathematicalOperation) throws InterruptedException {

    // On heavy load might produce java.lang.OutOfMemoryError: unable to create native thread: possibly out of memory or process/resource limits reached
    try(var executorService = Executors.newFixedThreadPool(fixedPoolSize)) {
      var tasks = numbers.stream().map(number -> new MathematicalTask(number, mathematicalOperation)).toList();

      var futures = executorService.invokeAll(tasks);

      return futures.stream()
        .map(entryFuture -> {
          try {
            return entryFuture.get();
          } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
  }

  protected Map<Integer, BigInteger> calculateVirtual(List<Integer> numbers, MathematicalOperation mathematicalOperation) throws InterruptedException, ExecutionException {

    try(var scope = new StructuredTaskScope.ShutdownOnFailure()) {

      var tasks = numbers.stream().map(number -> new MathematicalTask(number, mathematicalOperation)).toList();

      var subtasks = tasks.stream().map(scope::fork).toList();

      // Wait till all tasks succeed or first Child Task fails.
      // Send cancellation to all other Child Tasks if one fails
      scope.join();
      scope.throwIfFailed();

      return subtasks.stream()
        .map(StructuredTaskScope.Subtask::get)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
  }
}
