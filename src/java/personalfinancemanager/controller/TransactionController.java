package personalfinancemanager.controller;

import personalfinancemanager.dao.AccountDAO;
import personalfinancemanager.dao.TransactionDAO;
import personalfinancemanager.model.Account;
import personalfinancemanager.model.Transaction;
import personalfinancemanager.util.DateUtil;
import personalfinancemanager.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.util.List;

/**
 * Controller xử lý logic nghiệp vụ cho Giao dịch.
 */
public class TransactionController {
    private final TransactionDAO transactionDAO;
    private final AccountDAO accountDAO;

    public TransactionController() {
        this.transactionDAO = new TransactionDAO();
        this.accountDAO = new AccountDAO();
    }

    /**
     * Lấy danh sách giao dịch có bộ lọc.
     */
    public List<Transaction> getTransactionsByFilters(int userId, Integer accountId, Integer categoryId, String type, Date fromDate, Date toDate) {
        return transactionDAO.findByFilters(userId, accountId, categoryId, type, fromDate, toDate);
    }

    /**
     * Thêm mới một giao dịch.
     * 
     * @return null nếu thành công, thông báo lỗi tiếng Việt nếu thất bại.
     */
    public String addTransaction(int accountId, int categoryId, String type, String amountText, String dateText, String description) {
        if (accountId <= 0) {
            return "Vui lòng chọn tài khoản giao dịch.";
        }
        if (categoryId <= 0) {
            return "Vui lòng chọn danh mục thu chi.";
        }
        if (ValidationUtil.isNullOrEmpty(type)) {
            return "Vui lòng chọn loại giao dịch (Thu nhập/Chi tiêu).";
        }
        if (ValidationUtil.isNullOrEmpty(amountText)) {
            return "Vui lòng nhập số tiền giao dịch.";
        }
        if (ValidationUtil.isNullOrEmpty(dateText)) {
            return "Vui lòng nhập ngày giao dịch.";
        }

        BigDecimal amount;
        try {
            String cleanText = amountText.replace(".", "").replace(",", "").trim();
            amount = new BigDecimal(cleanText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return "Số tiền giao dịch phải lớn hơn 0.";
            }
        } catch (NumberFormatException e) {
            return "Số tiền không hợp lệ.";
        }

        Date transactionDate;
        try {
            transactionDate = DateUtil.parseDate(dateText);
        } catch (ParseException e) {
            return "Định dạng ngày không hợp lệ (yêu cầu dd/MM/yyyy).";
        }

        // Kiểm tra số dư tài khoản có đủ không (đối với giao dịch CHI TIÊU)
        if ("EXPENSE".equals(type)) {
            Account acc = accountDAO.findById(accountId);
            if (acc != null && acc.getBalance().compareTo(amount) < 0) {
                return "Số dư tài khoản không đủ để thực hiện chi tiêu này.";
            }
        }

        Transaction t = new Transaction();
        t.setAccountId(accountId);
        t.setCategoryId(categoryId);
        t.setType(type);
        t.setAmount(amount);
        t.setTransactionDate(transactionDate);
        t.setDescription(description != null ? description.trim() : null);

        boolean success = transactionDAO.insert(t);
        if (success) {
            return null; // Thành công
        }
        return "Lỗi khi lưu giao dịch vào cơ sở dữ liệu.";
    }

    /**
     * Cập nhật thông tin giao dịch.
     * 
     * @return null nếu thành công, thông báo lỗi tiếng Việt nếu thất bại.
     */
    public String updateTransaction(int transactionId, int accountId, int categoryId, String type, String amountText, String dateText, String description) {
        Transaction oldT = transactionDAO.findById(transactionId);
        if (oldT == null) {
            return "Giao dịch không tồn tại trên hệ thống.";
        }

        if (accountId <= 0) {
            return "Vui lòng chọn tài khoản giao dịch.";
        }
        if (categoryId <= 0) {
            return "Vui lòng chọn danh mục thu chi.";
        }
        if (ValidationUtil.isNullOrEmpty(type)) {
            return "Vui lòng chọn loại giao dịch (Thu nhập/Chi tiêu).";
        }
        if (ValidationUtil.isNullOrEmpty(amountText)) {
            return "Vui lòng nhập số tiền giao dịch.";
        }
        if (ValidationUtil.isNullOrEmpty(dateText)) {
            return "Vui lòng nhập ngày giao dịch.";
        }

        BigDecimal amount;
        try {
            String cleanText = amountText.replace(".", "").replace(",", "").trim();
            amount = new BigDecimal(cleanText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return "Số tiền giao dịch phải lớn hơn 0.";
            }
        } catch (NumberFormatException e) {
            return "Số tiền không hợp lệ.";
        }

        Date transactionDate;
        try {
            transactionDate = DateUtil.parseDate(dateText);
        } catch (ParseException e) {
            return "Định dạng ngày không hợp lệ (yêu cầu dd/MM/yyyy).";
        }

        // Kiểm tra số dư tài khoản có đủ không (đối với giao dịch CHI TIÊU)
        if ("EXPENSE".equals(type)) {
            Account acc = accountDAO.findById(accountId);
            if (acc != null) {
                BigDecimal balanceAfter = acc.getBalance();
                if (oldT.getAccountId() == accountId) {
                    // Nếu cùng tài khoản, hoàn tác số tiền cũ trước
                    if ("EXPENSE".equals(oldT.getType())) {
                        balanceAfter = balanceAfter.add(oldT.getAmount());
                    } else {
                        balanceAfter = balanceAfter.subtract(oldT.getAmount());
                    }
                }
                // Áp dụng số tiền chi tiêu mới
                balanceAfter = balanceAfter.subtract(amount);
                if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
                    return "Số dư tài khoản không đủ để thực hiện chi tiêu này.";
                }
            }
        }

        // Kiểm tra xem hoàn tác tài khoản cũ có làm số dư tài khoản cũ âm không (khi đổi tài khoản hoặc đổi loại)
        if (oldT.getAccountId() != accountId && "INCOME".equals(oldT.getType())) {
            Account oldAcc = accountDAO.findById(oldT.getAccountId());
            if (oldAcc != null && oldAcc.getBalance().compareTo(oldT.getAmount()) < 0) {
                return "Số dư của tài khoản cũ không đủ để hoàn tác giao dịch này.";
            }
        }

        Transaction newT = new Transaction();
        newT.setTransactionId(transactionId);
        newT.setAccountId(accountId);
        newT.setCategoryId(categoryId);
        newT.setType(type);
        newT.setAmount(amount);
        newT.setTransactionDate(transactionDate);
        newT.setDescription(description != null ? description.trim() : null);

        boolean success = transactionDAO.update(newT, oldT);
        if (success) {
            return null; // Thành công
        }
        return "Lỗi cơ sở dữ liệu. Không thể cập nhật giao dịch.";
    }

    /**
     * Xóa giao dịch.
     * 
     * @return null nếu thành công, thông báo lỗi tiếng Việt nếu thất bại.
     */
    public String deleteTransaction(int transactionId) {
        boolean success = transactionDAO.delete(transactionId);
        if (success) {
            return null;
        }
        return "Lỗi cơ sở dữ liệu. Không thể xóa giao dịch.";
    }
}
