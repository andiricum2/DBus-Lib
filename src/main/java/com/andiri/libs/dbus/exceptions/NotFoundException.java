package com.andiri.libs.dbus.exceptions;

public class NotFoundException extends ApiException {
    public NotFoundException(String message, int statusCode, String responseBody) {
        super(message, statusCode, responseBody);
    }
}