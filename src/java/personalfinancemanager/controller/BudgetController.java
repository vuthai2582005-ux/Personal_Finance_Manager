package personalfinancemanager.controller;

import personalfinancemanager.dao.BudgetDAO;
import personalfinancemanager.model.Budget;
import personalfinancemanager.util.ValidationUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller xử lý logic nghiệp vụ cho Ngân sách chi tiêu.
 */
public class BudgetController {
    private final BudgetDAO budgetDAO;

    public BudgetController() {
        this.budgetDAO = new BudgetDAO();
    }

    public List<Budget> getBudgetsByUserId(int userId) {
        return budgetDAO.findByUserId(userId);
    }

    /**
     * Tạo ngân sách mới cho người dùng.
     * 
     * @return null nếu thành công, thông báo lỗi nếu thất bại.
     */
    public String addBudget(int categoryId, int month, int year, String limitStr, int userId) {
        if (categoryId <= 0) {
            return "Vui lòng chọn danh mục chi tiêu.";
        }
        if (month < 1 || month > 12) {
            return "Tháng không hợp lệ (yêu cầu từ 1 đến 12).";
        }
        if (year < 2000) {
            return "Năm không hợp lệ.";
        }
        if (ValidationUtil.isNullOrEmpty(limitStr)) {
            return "Hạn mức ngân sách không được để trống.";
        }

        BigDecimal limit;
        try {
            String cleanText = limitStr.replace(".", "").replace(",", "").trim();
            limit = new BigDecimal(cleanText);
            if (limit.compareTo(BigDecimal.ZERO) <= 0) {
                return "Hạn mức ngân sách phải lớn hơn 0.";
            }
        } catch (NumberFormatException e) {
            return "Số tiền hạn mức không hợp lệ.";
        }

        // Kiểm tra xem đã có ngân sách cho danh mục này trong tháng/năm này chưa
        Budget existing = budgetDAO.findByUserAndCategoryAndMonth(userId, categoryId, month, year);
        if (existing != null) {
            return "Đã tồn tại thiết lập ngân sách cho danh mục này trong tháng " + month + "/" + year + ".";
        }

        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setCategoryId(categoryId);
        budget.setMonth(month);
        budget.setYear(year);
        budget.setBudgetAmount(limit);

        boolean success = budgetDAO.insert(budget);
        if (success) {
            return null; // Thành công
        }
        return "Lỗi cơ sở dữ liệu. Không thể lưu ngân sách.";
    }

    /**
     * Cập nhật hạn mức ngân sách hiện có.
     * 
     * @return null nếu thành công, thông báo lỗi nếu thất bại.
     */
    public String updateBudget(int budgetId, String limitStr) {
        if (ValidationUtil.isNullOrEmpty(limitStr)) {
            return "Hạn mức ngân sách không được để trống.";
        }

        BigDecimal limit;
        try {
            String cleanText = limitStr.replace(".", "").replace(",", "").trim();
            limit = new BigDecimal(cleanText);
            if (limit.compareTo(BigDecimal.ZERO) <= 0) {
                return "Hạn mức ngân sách phải lớn hơn 0.";
            }
        } catch (NumberFormatException e) {
            return "Số tiền hạn mức không hợp lệ.";
        }

        Budget budget = budgetDAO.findById(budgetId);
        if (budget == null) {
            return "Không tìm thấy thiết lập ngân sách trên hệ thống.";
        }

        budget.setBudgetAmount(limit);

        boolean success = budgetDAO.update(budget);
        if (success) {
            return null; // Thành công
        }
        return "Lỗi cơ sở dữ liệu. Không thể cập nhật ngân sách.";
    }

    /**
     * Xóa ngân sách.
     */
    public String deleteBudget(int budgetId) {
        boolean success = budgetDAO.delete(budgetId);
        if (success) {
            return null;
        }
        return "Lỗi cơ sở dữ liệu. Không thể xóa ngân sách.";
    }
}
