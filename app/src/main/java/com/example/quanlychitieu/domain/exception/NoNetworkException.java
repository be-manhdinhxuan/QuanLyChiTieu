package com.example.quanlychitieu.domain.exception;

public class NoNetworkException extends RuntimeException {
    public NoNetworkException() {
        super("No internet connection available");
    }

    public NoNetworkException(String message) {
        super(message);
    }

    public NoNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}