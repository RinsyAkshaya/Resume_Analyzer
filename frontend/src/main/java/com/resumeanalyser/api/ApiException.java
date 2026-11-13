package com.resumeanalyser.api;

/** Error returned by (or while calling) the backend. The message is safe to show to the user. */
public class ApiException extends Exception {

    private final int statusCode;

    public ApiException(String message) {
        this(message, -1);
    }

    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
