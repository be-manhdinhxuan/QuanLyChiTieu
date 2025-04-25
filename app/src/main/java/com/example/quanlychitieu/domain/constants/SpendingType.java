package com.example.quanlychitieu.domain.constants;

import com.example.quanlychitieu.R;

public class SpendingType {
    // Expense Types
    public static final int FOOD = 1;
    public static final int TRANSPORT = 2;
    public static final int SHOPPING = 3;
    public static final int ENTERTAINMENT = 4;
    public static final int HEALTHCARE = 5;
    public static final int EDUCATION = 6;
    public static final int OTHER = 7;

    // Income Types
    public static final int SALARY = 101;
    public static final int BONUS = 102;
    public static final int INVESTMENT = 103;
    public static final int OTHER_INCOME = 104;

    // Transfer Types
    public static final int TRANSFER_IN = 201;
    public static final int TRANSFER_OUT = 202;

    public static boolean isExpense(int type) {
        return type > 0 && type < 100;
    }

    public static boolean isIncome(int type) {
        return type >= 100 && type < 200;
    }

    public static boolean isTransfer(int type) {
        return type >= 200 && type < 300;
    }

    public static int getDefaultIconResource(int type) {
        switch (type) {
            case FOOD:
                return R.drawable.ic_food;
            case TRANSPORT:
                return R.drawable.ic_transport;
            case SHOPPING:
                return R.drawable.ic_shopping;
            case ENTERTAINMENT:
                return R.drawable.ic_entertainment;
            case SALARY:
                return R.drawable.ic_salary;
            case BONUS:
                return R.drawable.ic_bonus;
            default:
                return R.drawable.ic_other;
        }
    }
}