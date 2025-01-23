package com.andiri.libs.dbus.exceptions;

public class ServerErrorException extends ApiException {
    public ServerErrorException(String message, int statusCode, String responseBody) {
        super(message, statusCode, responseBody);
    }
}