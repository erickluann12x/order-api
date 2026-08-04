package com.erick.order_api.exception;


import org.springframework.security.core.AuthenticationException;

public class InvalidRefreshTokenException  extends AuthenticationException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
