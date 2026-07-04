package personalfinancemanager.controller;

import personalfinancemanager.dao.AccountDAO;
import personalfinancemanager.model.Account;
import personalfinancemanager.util.ValidationUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller xử lý logic nghiệp vụ cho Tài khoản tài chính.
 */
public class AccountController {
    private final AccountDAO accountDAO;

    public AccountController() {
        this.accountDAO = new AccountDAO();
    }

    /**
     * Lấy danh sách tài khoản của người dùng.
     */
    public List<Account> getAccountsByUserId(int userId) {
        return accountDAO.findByUserId(userId);
    }

    /**
     * Thêm tài khoản mới.
     * 
     * @return null nếu thành công, thông báo lỗi tiếng Việt nếu có lỗi.
     */
    public String addAccount(String accountName, String accountType, String initialBalanceText, int userId) {
        if (ValidationUtil.isNullOrEmpty(accountName)) {
            return "Tên tài khoản không được để trống.";
        }
        if (ValidationUtil.isNullOrEmpty(accountType)) {
            return "Loại tài khoản không được để trống.";
        }
        if (ValidationUtil.isNullOrEmpty(initialBalanceText)) {
            return "Số dư ban đầu không được để trống.";
        }

        accountName = accountName.trim();

        // Kiểm tra xem tên tài khoản có bị trùng của chính user này không
        List<Account> existing = accountDAO.findByUserId(userId);
        for (Account acc : existing) {
            if (acc.getAccountName().equalsIgnoreCase(accountName)) {
                return "Tên tài khoản này đã tồn tại.";
            }
        }

        // Parse số tiền số dư ban đầu
        BigDecimal balance;
        try {
            // Loại bỏ dấu phân cách nếu có
            String cleanText = initialBalanceText.replace(".", "").replace(",", "").trim();
            balance = new BigDecimal(cleanText);
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                return "Số dư ban đầu không được âm.";
            }
        } catch (NumberFormatException e) {
            return "Số tiền số dư ban đầu không hợp lệ.";
        }

        Account newAcc = new Account();
        newAcc.setUserId(userId);
        newAcc.setAccountName(accountName);
        newAcc.setAccountType(accountType);
        newAcc.setBalance(balance);
        newAcc.setCurrency("VND");
        newAcc.setActive(true);

        boolean success = accountDAO.insert(newAcc);
        if (success) {
            return null; // Thành công
        }
        return "Lỗi cơ sở dữ liệu. Không thể tạo tài khoản mới.";
    }

    /**
     * Cập nhật tài khoản hiện tại.
     * 
     * @return null nếu thành công, thông báo lỗi tiếng Việt nếu có lỗi.
     */
    public String updateAccount(int accountId, String accountName, String accountType, String balanceText, boolean isActive, int userId) {
        if (ValidationUtil.isNullOrEmpty(accountName)) {
            return "Tên tài khoản không được để trống.";
        }
        if (ValidationUtil.isNullOrEmpty(accountType)) {
            return "Loại tài khoản không được để trống.";
        }
        if (ValidationUtil.isNullOrEmpty(balanceText)) {
            return "Số dư không được để trống.";
        }

        accountName = accountName.trim();

        // Kiểm tra xem tên tài khoản mới có bị trùng của chính user này ở tài khoản khác không
        List<Account> existing = accountDAO.findByUserId(userId);
        for (Account acc : existing) {
            if (acc.getAccountId() != accountId && acc.getAccountName().equalsIgnoreCase(accountName)) {
                return "Tên tài khoản này đã tồn tại ở tài khoản khác.";
            }
        }

        BigDecimal balance;
        try {
            String cleanText = balanceText.replace(".", "").replace(",", "").trim();
            balance = new BigDecimal(cleanText);
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                return "Số dư tài khoản không được âm.";
            }
        } catch (NumberFormatException e) {
            return "Số tiền số dư không hợp lệ.";
        }

        Account acc = accountDAO.findById(accountId);
        if (acc == null) {
            return "Tài khoản không tồn tại.";
        }

        acc.setAccountName(accountName);
        acc.setAccountType(accountType);
        acc.setBalance(balance);
        acc.setActive(isActive);

        boolean success = accountDAO.update(acc);
        if (success) {
            return null; // Thành công
        }
        return "Lỗi cơ sở dữ liệu. Không thể cập nhật tài khoản.";
    }

    /**
     * Xóa tài khoản.
     */
    public String deleteAccount(int accountId) {
        boolean success = accountDAO.delete(accountId);
        if (success) {
            return null; // Thành công
        }
        return "Lỗi hệ thống. Không thể xóa hoặc ngưng hoạt động tài khoản này.";
    }
}
