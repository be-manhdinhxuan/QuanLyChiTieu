package com.example.quanlychitieu.domain.exception;

public class AuthenticationException extends RuntimeException {
    private final String errorCode;

    public AuthenticationException(String errorCode) {
        super(getMessageForErrorCode(errorCode));
        this.errorCode = errorCode;
    }

    public AuthenticationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AuthenticationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    private static String getMessageForErrorCode(String errorCode) {
        switch (errorCode) {
            case "ERROR_INVALID_CREDENTIALS":
                return "Invalid email or password";
            case "ERROR_USER_NOT_FOUND":
                return "User not found";
            case "ERROR_EMAIL_ALREADY_EXISTS":
                return "Email already exists";
            case "ERROR_WEAK_PASSWORD":
                return "Password is too weak";
            case "ERROR_INVALID_EMAIL":
                return "Invalid email format";
            default:
                return "Authentication failed";
        }
    }
}