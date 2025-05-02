package com.example.quanlychitieu.domain.model.item;

// Bên trong file LegendItem.java
public class LegendItem {
    private int color;
    private int iconResId;
    private String name;
    private String valueText;    // Dùng cho legend chính (hiển thị %)
    private double actualAmount; // Dùng cho dialog chi tiết "Khác" và lưu trữ

    // *** Constructor nhận 5 tham số ***
    public LegendItem(int color, int iconResId, String name, String valueText, double actualAmount) {
        this.color = color;
        this.iconResId = iconResId;
        this.name = name;
        this.valueText = valueText;    // Có thể là % hoặc rỗng
        this.actualAmount = actualAmount; // Số tiền thực tế
    }

    // Getters...
    public int getColor() { return color; }
    public int getIconResId() { return iconResId; }
    public String getName() { return name; }
    public String getValueText() { return valueText; }
    public double getActualAmount() { return actualAmount; }
}