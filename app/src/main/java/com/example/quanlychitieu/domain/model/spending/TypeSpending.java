package com.example.quanlychitieu.domain.model.spending;

import java.util.ArrayList;
import java.util.List;

public class TypeSpending {
    private String typeName;
    private List<Spending> spendings;
    private double totalAmount;
    private int typeIcon; // Resource ID của icon

    public TypeSpending(String typeName, int typeIcon) {
        this.typeName = typeName;
        this.typeIcon = typeIcon;
        this.spendings = new ArrayList<>();
        this.totalAmount = 0;
    }

    public void addSpending(Spending spending) {
        spendings.add(spending);
        totalAmount += spending.getMoney();
    }

    public String getTypeName() {
        return typeName;
    }

    public List<Spending> getSpendings() {
        return spendings;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public int getTypeIcon() {
        return typeIcon;
    }
}