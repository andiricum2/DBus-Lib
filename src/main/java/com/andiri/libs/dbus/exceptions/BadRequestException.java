package com.andiri.libs.dbus.exceptions;

public class BadRequestException extends ApiException {
    public BadRequestException(String message, int statusCode, String responseBody) {
        super(message, statusCode, responseBody);
    }
}