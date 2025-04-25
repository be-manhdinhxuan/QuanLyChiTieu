package com.example.quanlychitieu.domain.enums;

public enum SpendingCategory {
    ESSENTIAL("Essential", 1),
    ENTERTAINMENT("Entertainment", 2),
    INVESTMENT("Investment", 3),
    INCOME("Income", 4),
    TRANSFER("Transfer", 5),
    OTHER("Other", 6);

    private final String displayName;
    private final int value;

    SpendingCategory(String displayName, int value) {
        this.displayName = displayName;
        this.value = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getValue() {
        return value;
    }

    public static SpendingCategory fromValue(int value) {
        for (SpendingCategory category : SpendingCategory.values()) {
            if (category.getValue() == value) {
                return category;
            }
        }
        return OTHER;
    }

    public boolean isExpenseCategory() {
        return this == ESSENTIAL || this == ENTERTAINMENT || this == OTHER;
    }

    public boolean isIncomeCategory() {
        return this == INCOME || this == INVESTMENT;
    }
}