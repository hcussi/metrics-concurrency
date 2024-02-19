package com.hernancussi.poc.metrics.concurrency.simulations;

import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class MathRegistrationSimulation extends Simulation {

  private static final HttpProtocolBuilder HTTP_PROTOCOL_BUILDER = setupProtocolForSimulation();

  private static final Iterator<Map<String, Object>> FEED_DATA = setupTestFeedData();

  private static final ScenarioBuilder POST_SCENARIO_BUILDER = buildPostScenario();


  public MathRegistrationSimulation() {
    setUp(POST_SCENARIO_BUILDER.injectOpen(postEndpointInjectionProfile())
      .protocols(HTTP_PROTOCOL_BUILDER)).assertions(global().responseTime()
      .max()
      .lte(60000), global().successfulRequests()
      .percent()
      .gt(95d));
  }

  private OpenInjectionStep.RampRate.RampRateOpenInjectionStep postEndpointInjectionProfile() {
    int totalDesiredCount = 30;
    double userRampUpPerInterval = 10;
    double rampUpIntervalSeconds = 10;

    int totalRampUptimeSeconds = 30;
    int steadyStateDurationSeconds = 30;
    return rampUsersPerSec(userRampUpPerInterval / (rampUpIntervalSeconds / 60))
      .to(totalDesiredCount)
      .during(Duration.ofSeconds(totalRampUptimeSeconds + steadyStateDurationSeconds));
  }

  private static HttpProtocolBuilder setupProtocolForSimulation() {
    return http
      .baseUrl("http://localhost:8080")
      .acceptHeader("application/json")
      .maxConnectionsPerHost(20)
      .userAgentHeader("Gatling/Math");
  }

  private static Iterator<Map<String, Object>> setupTestFeedData() {
    var random = new Random();
    return Stream.generate(() -> {
        Map<String, Object> stringObjectMap = new HashMap<>();
        var numbers = random.ints(50, 1, 60);
        stringObjectMap.put("numbers", numbers.boxed().collect(Collectors.toSet()));
        return stringObjectMap;
      })
      .iterator();
  }

  private static ScenarioBuilder buildPostScenario() {
    var token = System.getProperty("GATLING_TOKEN");

    return CoreDsl.scenario("Load Testing")
      .feed(FEED_DATA)
      .exec(
        http("factorial-request")
          .post("/api/v1/calculateFactorial")
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + token)
          .body(StringBody("{ \"numbers\": ${numbers} }"))
          .check(status().is(200))
      ).exec(
        http("square-request")
          .post("/api/v1/calculateSquare")
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + token)
          .body(StringBody("{ \"numbers\": ${numbers} }"))
          .check(status().is(200))
      ).exec(
        http("factorial-virtual-request")
          .post("/api/v1/calculateFactorial?strategy=VIRTUAL")
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + token)
          .body(StringBody("{ \"numbers\": ${numbers} }"))
          .check(status().is(200))
      ).exec(
        http("square-virtual-request")
          .post("/api/v1/calculateSquare?strategy=VIRTUAL")
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + token)
          .body(StringBody("{ \"numbers\": ${numbers} }"))
          .check(status().is(200))
      ).exec(
        http("factorial-thread-request")
          .post("/api/v1/calculateFactorial?strategy=THREADS")
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + token)
          .body(StringBody("{ \"numbers\": ${numbers} }"))
          .check(status().is(200))
      ).exec(
        http("square-thread-request")
          .post("/api/v1/calculateSquare?strategy=THREADS")
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + token)
          .body(StringBody("{ \"numbers\": ${numbers} }"))
          .check(status().is(200))
      );
  }

}
