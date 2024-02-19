package com.hernancussi.poc.metrics.concurrency.security;

import com.hernancussi.poc.metrics.concurrency.auth.JwtAuthorizationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig {

  private final JwtAuthorizationFilter jwtAuthorizationFilter;

  @Autowired
  public WebSecurityConfig(JwtAuthorizationFilter jwtAuthorizationFilter) {
    this.jwtAuthorizationFilter = jwtAuthorizationFilter;
  }

  @Bean
  public AuthenticationEntryPoint unauthorizedEntryPoint() {
    return (_, response, _) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
      .securityMatchers(
        matcher -> matcher
          .requestMatchers(HttpMethod.POST, "/actuator/**", "/api/v1/authentication/**")
          .requestMatchers(HttpMethod.GET, "/actuator/**", "/api/v1/generateSecret/**")
      )
      .authorizeHttpRequests(registry ->
        registry
          .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
          .requestMatchers("/api/v1/generateSecret", "/api/v1/authentication").permitAll()
          .requestMatchers("/actuator/**").hasRole("ADMIN")
      )
      .anonymous(AbstractHttpConfigurer::disable)
      .csrf(AbstractHttpConfigurer::disable)
      .httpBasic(Customizer.withDefaults())
      // this disables session creation on Spring Security
      .sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .build();
  }

  @Bean
  public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
    return http
      .securityMatchers(
        matcher -> matcher
          .requestMatchers(HttpMethod.OPTIONS, "/api/v1/**")
          .requestMatchers(HttpMethod.POST, "/api/v1/**")
          .requestMatchers(HttpMethod.GET, "/api/v1/**")
      )
      .authorizeHttpRequests(registry -> registry
        .requestMatchers("/api/v1/generateSecret", "/api/v1/authentication").permitAll()
        .anyRequest().authenticated()
      )
      .anonymous(AbstractHttpConfigurer::disable)
      .csrf(AbstractHttpConfigurer::disable)
      .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(unauthorizedEntryPoint()))
      .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
      // this disables session creation on Spring Security
      .sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .build();
  }

}
