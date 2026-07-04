package personalfinancemanager.model;

/**
 * Model đại diện cho danh mục thu/chi (bảng Categories).
 */
public class Category {
    private int categoryId;
    private int userId;
    private String categoryName;
    private String type; // INCOME, EXPENSE
    private String icon;
    private String color; // Hex color code
    private boolean isDefault;

    public Category() {
        this.isDefault = false;
    }

    public Category(int categoryId, int userId, String categoryName, String type, String icon, String color, boolean isDefault) {
        this.categoryId = categoryId;
        this.userId = userId;
        this.categoryName = categoryName;
        this.type = type;
        this.icon = icon;
        this.color = color;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public String toString() {
        // Trả về tên danh mục để dễ hiển thị trong JComboBox
        return categoryName;
    }
}
