package com.example.quanlychitieu.domain.constants;

import com.example.quanlychitieu.R;

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

    // Essential spending types
    public static final int HOME_REPAIR = 20;
    public static final int VEHICLE = 21;
    public static final int HEALTHCARE = 22;
    public static final int INSURANCE = 23;
    public static final int EDUCATION = 24;
    public static final int HOUSEWARES = 25;
    public static final int PERSONAL = 26;
    public static final int PET = 27;
    public static final int FAMILY = 28;
    public static final int HOUSING = 29;
    public static final int UTILITIES = 30;

    // Entertainment and lifestyle types
    public static final int SPORTS = 31;
    public static final int BEAUTY = 32;
    public static final int GIFTS = 33;
    public static final int ENTERTAINMENT = 34;
    public static final int SHOPPING = 35;

    // Income types
    public static final int SALARY = 41;
    public static final int BONUS = 42;
    public static final int INVESTMENT_RETURN = 43;
    public static final int OTHER_INCOME = 44;

    // Other types
    public static final int OTHER = 50;

    public static boolean isIncome(int type) {
        return type >= 41 && type <= 44;
    }

    public static boolean isExpense(int type) {
        return type >= 0 && type <= 35 || type == 50;
    }

    public static int getDefaultIconResource(int type) {
        switch (type) {
            // Monthly spending types
            case EATING:
                return R.drawable.ic_eat;
            case TRANSPORTATION:
                return R.drawable.ic_taxi;
            case RENT:
                return R.drawable.ic_house;
            case WATER:
                return R.drawable.ic_water;
            case PHONE:
                return R.drawable.ic_phone;
            case ELECTRICITY:
                return R.drawable.ic_electricity;
            case GAS:
                return R.drawable.ic_gas;
            case TV:
                return R.drawable.ic_tv;
            case INTERNET:
                return R.drawable.ic_internet;

            // Essential spending types
            case HOME_REPAIR:
                return R.drawable.ic_house_2;
            case VEHICLE:
                return R.drawable.ic_tools;
            case HEALTHCARE:
                return R.drawable.ic_doctor;
            case INSURANCE:
                return R.drawable.ic_health_insurance;
            case EDUCATION:
                return R.drawable.ic_education;
            case HOUSEWARES:
                return R.drawable.ic_armchair;
            case PERSONAL:
                return R.drawable.ic_toothbrush;
            case PET:
                return R.drawable.ic_pet;
            case FAMILY:
                return R.drawable.ic_family;
            case HOUSING:
                return R.drawable.ic_house;
            case UTILITIES:
                return R.drawable.ic_water;

            // Entertainment and lifestyle types
            case SPORTS:
                return R.drawable.ic_sports;
            case BEAUTY:
                return R.drawable.ic_diamond;
            case GIFTS:
                return R.drawable.ic_give_love;
            case ENTERTAINMENT:
                return R.drawable.ic_game_pad;
            case SHOPPING:
                return R.drawable.ic_shopping;

            // Income types
            case SALARY:
                return R.drawable.ic_money;
            case BONUS:
                return R.drawable.ic_money_bag;
            case INVESTMENT_RETURN:
                return R.drawable.ic_money;
            case OTHER_INCOME:
                return R.drawable.ic_money_bag;

            // Other types
            case OTHER:
                return R.drawable.ic_box;
            
            default:
                return R.drawable.ic_other;
        }
    }
}