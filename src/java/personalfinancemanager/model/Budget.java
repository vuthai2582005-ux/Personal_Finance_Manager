package personalfinancemanager.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model đại diện cho ngân sách (bảng Budgets).
 */
public class Budget {
    private int budgetId;
    private int userId;
    private int categoryId;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private int month;
    private int year;
    private Timestamp createdAt;

    // Các thuộc tính phụ trợ để hiển thị thông tin từ view vw_BudgetStatus
    private String categoryName;
    private String categoryColor;
    private String categoryIcon;
    private BigDecimal remainingAmount;
    private double usagePercent;

    public Budget() {
        this.spentAmount = BigDecimal.ZERO;
        this.remainingAmount = BigDecimal.ZERO;
        this.usagePercent = 0.0;
    }

    public Budget(int budgetId, int userId, int categoryId, BigDecimal budgetAmount, BigDecimal spentAmount, int month, int year, Timestamp createdAt) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount;
        this.month = month;
        this.year = year;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public double getUsagePercent() {
        return usagePercent;
    }

    public void setUsagePercent(double usagePercent) {
        this.usagePercent = usagePercent;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "budgetId=" + budgetId +
                ", categoryId=" + categoryId +
                ", budgetAmount=" + budgetAmount +
                ", spentAmount=" + spentAmount +
                ", period=" + month + "/" + year +
                '}';
    }
}
