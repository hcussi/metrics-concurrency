package com.hernancussi.poc.metrics.concurrency;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableScheduling
@OpenAPIDefinition(
	info = @Info(
		title = "Mathematical Functions API",
		version = "1.0",
		description = "Mathematical Functions API calculation with different strategies for concurrency"
	)
)
public class MetricsConcurrencyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetricsConcurrencyApplication.class, args);
	}

}
