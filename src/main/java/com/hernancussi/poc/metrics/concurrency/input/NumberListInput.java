package com.hernancussi.poc.metrics.concurrency.input;

import com.hernancussi.poc.metrics.concurrency.exception.NegativeNumberNotAllowedException;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class NumberListInput implements InputValidation {

  @NotNull
  private List<Integer> numbers;

  @Override
  public void validate() {
    if(numbers.stream().anyMatch(number -> number <= 0)) {
      throw new NegativeNumberNotAllowedException("Negative Numbers are not allowed");
    };
  }
}
