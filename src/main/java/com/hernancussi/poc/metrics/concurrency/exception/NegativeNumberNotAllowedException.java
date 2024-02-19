package com.hernancussi.poc.metrics.concurrency.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED, reason = "Negative numbers are not allowed")
public class NegativeNumberNotAllowedException extends RuntimeException {
    public NegativeNumberNotAllowedException() {
        super();
    }
    public NegativeNumberNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
    public NegativeNumberNotAllowedException(String message) {
        super(message);
    }
    public NegativeNumberNotAllowedException(Throwable cause) {
        super(cause);
    }
}
