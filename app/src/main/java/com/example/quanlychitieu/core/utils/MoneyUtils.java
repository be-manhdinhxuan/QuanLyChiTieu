package com.example.quanlychitieu.core.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class MoneyUtils {
    private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public static String formatMoney(int amount) {
        return currencyFormatter.format(amount);
    }

    public static String formatMoneyWithoutCurrency(int amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }

    public static int parseMoneyString(String moneyStr) {
        if (moneyStr == null || moneyStr.isEmpty()) {
            return 0;
        }
        // Remove all non-digit characters
        String cleanStr = moneyStr.replaceAll("[^0-9]", "");
        try {
            return Integer.parseInt(cleanStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}