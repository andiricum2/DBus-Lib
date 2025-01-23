package com.andiri.libs.dbus.exceptions;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message, int statusCode, String responseBody) {
        super(message, statusCode, responseBody);
    }
}