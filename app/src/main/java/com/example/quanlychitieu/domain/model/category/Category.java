package com.example.quanlychitieu.domain.model.category;


/**
 * Domain model representing a spending category
 */
public class Category {
    private int id;
    private String name;
    private String icon;
    private String color;
    private int type;
    private boolean isDefault;

    /**
     * Default constructor
     */
    public Category() {
        this.name = "";
        this.icon = "";
        this.color = "#000000";
        this.type = 0;
        this.isDefault = false;
    }

    /**
     * Full constructor
     *
     * @param id Category ID
     * @param name Category name
     * @param icon Icon resource name or URL
     * @param color Color in hex format (e.g., "#FF5733")
     * @param type Category type (0 for expense, 1 for income)
     * @param isDefault Whether this is a default category
     */
    public Category(int id, String name, String icon, String color, int type, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.type = type;
        this.isDefault = isDefault;
    }

    /**
     * Constructor without ID for creating new categories
     *
     * @param name Category name
     * @param icon Icon resource name or URL
     * @param color Color in hex format
     * @param type Category type
     * @param isDefault Whether this is a default category
     */
    public Category(String name, String icon, String color, int type, boolean isDefault) {
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.type = type;
        this.isDefault = isDefault;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    public int getType() {
        return type;
    }

    public boolean isDefault() {
        return isDefault;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * Create a copy of this category
     * @return A new Category instance with the same values
     */
    public Category copy() {
        return new Category(id, name, icon, color, type, isDefault);
    }

    /**
     * Create a copy with modified values
     *
     * @param name New name or null to keep current
     * @param icon New icon or null to keep current
     * @param color New color or null to keep current
     * @param type New type or null to keep current
     * @param isDefault New isDefault value or null to keep current
     * @return A new Category with updated values
     */
    public Category copyWith(String name, String icon, String color, Integer type, Boolean isDefault) {
        return new Category(
                this.id,
                name != null ? name : this.name,
                icon != null ? icon : this.icon,
                color != null ? color : this.color,
                type != null ? type : this.type,
                isDefault != null ? isDefault : this.isDefault
        );
    }

    /**
     * Common expense category types
     */
    public static class ExpenseTypes {
        public static final int FOOD = 1;
        public static final int TRANSPORT = 2;
        public static final int SHOPPING = 3;
        public static final int ENTERTAINMENT = 4;
        public static final int BILLS = 5;
        public static final int HEALTH = 6;
        public static final int EDUCATION = 7;
        public static final int OTHERS = 8;
    }

    /**
     * Common income category types
     */
    public static class IncomeTypes {
        public static final int SALARY = 101;
        public static final int BONUS = 102;
        public static final int GIFT = 103;
        public static final int INVESTMENT = 104;
        public static final int OTHERS = 105;
    }

    /**
     * Get a string representation of the category
     */
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}