package com.example.quanlychitieu.domain.util;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    static {
        numberFormat.setMaximumFractionDigits(0);
        numberFormat.setGroupingUsed(true);
    }

    public static String formatCurrency(long amount) {
        return currencyFormat.format(amount);
    }

    public static String formatNumber(long number) {
        return numberFormat.format(number);
    }

    public static String formatAmount(double amount) {
        if (amount >= 0) {
            return formatCurrency((long) amount);
        } else {
            return "-" + formatCurrency((long) Math.abs(amount));
        }
    }

    public static long parseCurrencyString(String currencyString) {
        try {
            String cleanString = currencyString.replaceAll("[^\\d]", "");
            return Long.parseLong(cleanString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static boolean isValidAmount(long amount) {
        return amount >= 0 && amount <= 1000000000000L; // Max 1 trillion
    }

    public static String getFormattedBalance(long balance) {
        if (balance >= 0) {
            return formatCurrency(balance);
        } else {
            return "-" + formatCurrency(Math.abs(balance));
        }
    }
}