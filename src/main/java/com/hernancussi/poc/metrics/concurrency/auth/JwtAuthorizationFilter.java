package com.hernancussi.poc.metrics.concurrency.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hernancussi.poc.metrics.concurrency.config.ApplicationConfig;
import com.hernancussi.poc.metrics.concurrency.exception.InvalidJwtTokenException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper mapper;
    private final ApplicationConfig applicationConfig;

    private static final List<AntPathRequestMatcher> excludedMatchers = List.of(
      AntPathRequestMatcher.antMatcher("/api/v1/authentication"),
      AntPathRequestMatcher.antMatcher("/api/v1/generateSecret"),
      AntPathRequestMatcher.antMatcher("/actuator/**")
    );

    private static final List<AntPathRequestMatcher> includeMatchers = List.of(
      AntPathRequestMatcher.antMatcher("/api/v1/**")
    );

    public JwtAuthorizationFilter(JwtUtil jwtUtil, ObjectMapper mapper, ApplicationConfig applicationConfig) {
      this.jwtUtil = jwtUtil;
        this.mapper = mapper;
        this.applicationConfig = applicationConfig;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Map<String, Object> errorDetails = new HashMap<>();

        try {
            String accessToken = jwtUtil.resolveToken(request);
            if (accessToken == null ) {
                throw new InvalidJwtTokenException("JWT Token has not been provided");
            }

            jwtUtil.validateToken(accessToken, applicationConfig.getApiUserName());

            Authentication authentication = new UsernamePasswordAuthenticationToken(applicationConfig.getApiUserName(),"", new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException e) {
            errorDetails.put("message", "Authentication Error");
            errorDetails.put("details",e.getMessage());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            mapper.writeValue(response.getWriter(), errorDetails);
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return (
          excludedMatchers.stream().anyMatch(matcher -> matcher.matches(request))
          || includeMatchers.stream().noneMatch(matcher -> matcher.matches(request))
        );
    }
}
