package com.hernancussi.poc.metrics.concurrency.controllers;

import com.hernancussi.poc.metrics.concurrency.aop.HandleRestRequest;
import com.hernancussi.poc.metrics.concurrency.business.SquareBusinessService;
import com.hernancussi.poc.metrics.concurrency.input.NumberListInput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("api/v1")
@Tag(name = "Square", description = "Square endpoint for function calculation")
@SecurityRequirement(name = "basicAuth")
public class SquareController {

  private final SquareBusinessService squareBusinessService;

  @Autowired
  public SquareController(SquareBusinessService squareBusinessService) {
    this.squareBusinessService = squareBusinessService;
  }

  @PostMapping(
    path = "/calculateSquare",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @HandleRestRequest
  @Operation(
    summary = "Calculate Square",
    description = "Square function calculation for all input numbers given")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "successful operation")
  })
  public Map<Integer, BigInteger> calculateSquare(@RequestBody NumberListInput input, @RequestParam(required = false) CalculationStrategy strategy) throws ExecutionException, InterruptedException {
    switch (strategy) {
      case VIRTUAL -> {
        return squareBusinessService.calculateSquareVirtual(input.getNumbers());
      }
      case THREADS -> {
        return squareBusinessService.calculateSquareThreads(input.getNumbers());
      }
      case null, default -> {
        return squareBusinessService.calculateSquare(input.getNumbers());
      }
    }
  }

}
