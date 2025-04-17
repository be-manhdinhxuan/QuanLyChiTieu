package com.example.quanlychitieu.core.function;

import java.util.Calendar;
import java.util.Date;

public class Extensions {
    // String Extensions
    public static class StringExtensions {
        public static String capitalize(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        }

        public static String formatByTNT(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            
            String[] parts = str.split("\\.");
            if (parts.length != 2) {
                return str;
            }

            if (parts[1].length() > 10) {
                return parts[0] + "." + parts[1].substring(0, 10);
            }
            
            return str;
        }
    }

    // Date Extensions
    public static class DateExtensions {
        public static Date formatToDate(Date date) {
            if (date == null) {
                return null;
            }
            
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            return calendar.getTime();
        }
    }

    // Cách sử dụng:
    // String capitalized = StringExtensions.capitalize("hello");
    // String formatted = StringExtensions.formatByTNT("123.45678901234");
    // Date dateOnly = DateExtensions.formatToDate(new Date());
}