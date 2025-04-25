package com.example.quanlychitieu.core.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtils {
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidMoney(String money) {
        if (TextUtils.isEmpty(money)) return false;
        try {
            int amount = MoneyUtils.parseMoneyString(money);
            return amount > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidNote(String note) {
        return note != null && note.length() <= 500; // Maximum 500 characters
    }
}