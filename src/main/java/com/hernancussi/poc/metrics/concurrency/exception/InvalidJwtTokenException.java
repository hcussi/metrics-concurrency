package com.hernancussi.poc.metrics.concurrency.exception;

import io.jsonwebtoken.JwtException;

public class InvalidJwtTokenException extends JwtException {
    public InvalidJwtTokenException(String message) {
        super(message);
    }
}
