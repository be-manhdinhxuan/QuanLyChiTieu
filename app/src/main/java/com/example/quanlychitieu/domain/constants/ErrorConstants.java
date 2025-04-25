package com.example.quanlychitieu.domain.constants;

public class ErrorConstants {
    // Network Errors
    public static final String ERROR_NO_NETWORK = "ERROR_NO_NETWORK";
    public static final String ERROR_TIMEOUT = "ERROR_TIMEOUT";
    public static final String ERROR_SERVER = "ERROR_SERVER";

    // Authentication Errors
    public static final String ERROR_INVALID_CREDENTIALS = "ERROR_INVALID_CREDENTIALS";
    public static final String ERROR_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND";
    public static final String ERROR_EMAIL_ALREADY_EXISTS = "ERROR_EMAIL_ALREADY_EXISTS";
    public static final String ERROR_WEAK_PASSWORD = "ERROR_WEAK_PASSWORD";
    public static final String ERROR_INVALID_EMAIL = "ERROR_INVALID_EMAIL";

    // Data Errors
    public static final String ERROR_INVALID_DATA = "ERROR_INVALID_DATA";
    public static final String ERROR_DATA_NOT_FOUND = "ERROR_DATA_NOT_FOUND";
    public static final String ERROR_PERMISSION_DENIED = "ERROR_PERMISSION_DENIED";

    // Storage Errors
    public static final String ERROR_UPLOAD_FAILED = "ERROR_UPLOAD_FAILED";
    public static final String ERROR_DOWNLOAD_FAILED = "ERROR_DOWNLOAD_FAILED";
    public static final String ERROR_FILE_NOT_FOUND = "ERROR_FILE_NOT_FOUND";

    public static String getErrorMessage(String errorCode) {
        switch (errorCode) {
            case ERROR_NO_NETWORK:
                return "No internet connection";
            case ERROR_TIMEOUT:
                return "Connection timeout";
            case ERROR_SERVER:
                return "Server error";
            case ERROR_INVALID_CREDENTIALS:
                return "Invalid email or password";
            case ERROR_USER_NOT_FOUND:
                return "User not found";
            case ERROR_EMAIL_ALREADY_EXISTS:
                return "Email already exists";
            default:
                return "An unexpected error occurred";
        }
    }
}