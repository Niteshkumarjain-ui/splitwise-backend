package com.learn.splitwise.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class JwtExceptionHandler extends RuntimeException{

    private final HttpStatus status;

    public JwtExceptionHandler(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
