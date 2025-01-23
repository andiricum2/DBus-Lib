package com.andiri.libs.dbus.exceptions;

public class ApiConnectionException extends ApiException {
    public ApiConnectionException(String message, Throwable cause) {
        super(message + ": " + cause.getMessage(), -1, null); // Status code -1 for connection errors
    }
}