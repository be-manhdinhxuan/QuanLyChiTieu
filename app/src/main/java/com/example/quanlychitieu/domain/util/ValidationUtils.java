package com.example.quanlychitieu.domain.util;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtils {
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_NOTE_LENGTH = 500;

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.length() <= MAX_NAME_LENGTH;
    }

    public static boolean isValidNote(String note) {
        return note == null || note.length() <= MAX_NOTE_LENGTH;
    }

    public static boolean isValidPhoneNumber(String phone) {
        if (TextUtils.isEmpty(phone)) return true; // Optional field
        return Patterns.PHONE.matcher(phone).matches();
    }

    public static String getPasswordErrorMessage(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Password is required";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters";
        }
        return null;
    }

    public static String getEmailErrorMessage(String email) {
        if (TextUtils.isEmpty(email)) {
            return "Email is required";
        }
        if (!isValidEmail(email)) {
            return "Invalid email format";
        }
        return null;
    }

    public static String getNameErrorMessage(String name) {
        if (TextUtils.isEmpty(name)) {
            return "Name is required";
        }
        if (name.length() > MAX_NAME_LENGTH) {
            return "Name is too long (maximum " + MAX_NAME_LENGTH + " characters)";
        }
        return null;
    }
}