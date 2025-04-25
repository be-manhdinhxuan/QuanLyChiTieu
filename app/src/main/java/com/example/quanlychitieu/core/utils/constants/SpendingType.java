package com.example.quanlychitieu.core.utils.constants;

public class SpendingType {
    // Monthly spending types
    public static final int MONTHLY = 0;
    public static final int EATING = 1;
    public static final int TRANSPORTATION = 2;
    public static final int RENT = 3;
    public static final int WATER = 4;
    public static final int PHONE = 5;
    public static final int ELECTRICITY = 6;
    public static final int GAS = 7;
    public static final int TV = 8;
    public static final int INTERNET = 9;

    // Other spending types
    public static final int SHOPPING = 10;
    public static final int ENTERTAINMENT = 11;
    public static final int HEALTHCARE = 12;
    public static final int EDUCATION = 13;
    public static final int INVESTMENT = 14;
    public static final int SAVINGS = 15;
    public static final int OTHER = 16;

    // Income types
    public static final int SALARY = 41;
    public static final int BONUS = 42;
    public static final int INVESTMENT_RETURN = 43;
    public static final int OTHER_INCOME = 44;

    public static boolean isIncome(int type) {
        return type >= 41 && type <= 44;
    }

    public static boolean isExpense(int type) {
        return type >= 0 && type <= 16;
    }
}