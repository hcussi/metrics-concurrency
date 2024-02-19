package com.hernancussi.poc.metrics.concurrency.controllers;

import com.hernancussi.poc.metrics.concurrency.aop.HandleRestRequest;
import com.hernancussi.poc.metrics.concurrency.cache.CacheStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/cache")
@Tag(name = "Cache", description = "Cache operations for configured cache strategies")
@SecurityRequirement(name = "basicAuth")
public class CacheController {

  private final CacheStrategy<?> cacheStrategy;

  @Autowired
  public CacheController(CacheStrategy<?> cacheStrategy) {
    this.cacheStrategy = cacheStrategy;
  }

  @PostMapping(
    path = "/clear"
  )
  @HandleRestRequest
  @Operation(
    summary = "Clear Cache",
    description = "Evict all keys from all configured caches")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "successful operation")
  })
  public void clear() {
    cacheStrategy.clear();
  }

}
