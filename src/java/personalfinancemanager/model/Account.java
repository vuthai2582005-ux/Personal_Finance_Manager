package personalfinancemanager.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model đại diện cho tài khoản tài chính (bảng Accounts).
 */
public class Account {
    private int accountId;
    private int userId;
    private String accountName;
    private String accountType; // CASH, BANK, E_WALLET, CREDIT_CARD, SAVINGS
    private BigDecimal balance;
    private String currency;
    private boolean isActive;
    private Timestamp createdAt;

    public Account() {
        this.balance = BigDecimal.ZERO;
        this.currency = "VND";
        this.isActive = true;
    }

    public Account(int accountId, int userId, String accountName, String accountType, BigDecimal balance, String currency, boolean isActive, Timestamp createdAt) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountName = accountName;
        this.accountType = accountType;
        this.balance = balance;
        this.currency = currency;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        // Trả về tên tài khoản để dễ hiển thị trong JComboBox
        return accountName;
    }
}
