package com.andiri.libs.dbus.exceptions;

public class ApiResponseParseException extends ApiException {
    public ApiResponseParseException(String message, String responseBody, Throwable cause) {
        super(message + ": " + cause.getMessage(), -2, responseBody); // Status code -2 for parse errors
    }
}