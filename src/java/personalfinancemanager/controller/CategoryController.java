package personalfinancemanager.controller;

import personalfinancemanager.config.DatabaseConnection;
import personalfinancemanager.dao.CategoryDAO;
import personalfinancemanager.model.Category;
import personalfinancemanager.util.ValidationUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller xử lý logic nghiệp vụ cho Danh mục thu/chi.
 */
public class CategoryController {
    private final CategoryDAO categoryDAO;

    public CategoryController() {
        this.categoryDAO = new CategoryDAO();
    }

    public List<Category> getCategoriesByUserId(int userId) {
        return categoryDAO.findByUserId(userId);
    }

    public List<Category> getCategoriesByType(int userId, String type) {
        return categoryDAO.findByType(userId, type);
    }

    /**
     * Thêm danh mục mới.
     * 
     * @return null nếu thành công, thông báo lỗi nếu có lỗi.
     */
    public String addCategory(String categoryName, String type, String color, int userId) {
        if (ValidationUtil.isNullOrEmpty(categoryName)) {
            return "Tên danh mục không được để trống.";
        }
        if (ValidationUtil.isNullOrEmpty(type)) {
            return "Loại danh mục không được để trống.";
        }

        categoryName = categoryName.trim();

        // Kiểm tra xem tên danh mục đã có trong cùng loại của user này chưa
        List<Category> list = categoryDAO.findByType(userId, type);
        for (Category cat : list) {
            if (cat.getCategoryName().equalsIgnoreCase(categoryName)) {
                return "Tên danh mục này đã tồn tại trong nhóm này.";
            }
        }

        Category category = new Category();
        category.setUserId(userId);
        category.setCategoryName(categoryName);
        category.setType(type);
        category.setIcon("folder"); // Giá trị icon mặc định
        category.setColor(color != null ? color : "#ADB5BD");

        boolean success = categoryDAO.insert(category);
        if (success) {
            return null;
        }
        return "Lỗi cơ sở dữ liệu. Không thể thêm danh mục.";
    }

    /**
     * Cập nhật danh mục.
     * 
     * @return null nếu thành công, thông báo lỗi nếu có lỗi.
     */
    public String updateCategory(int categoryId, String categoryName, String type, String color, int userId) {
        if (ValidationUtil.isNullOrEmpty(categoryName)) {
            return "Tên danh mục không được để trống.";
        }
        if (ValidationUtil.isNullOrEmpty(type)) {
            return "Loại danh mục không được để trống.";
        }

        categoryName = categoryName.trim();

        Category category = categoryDAO.findById(categoryId);
        if (category == null) {
            return "Danh mục không tồn tại.";
        }

        // Ngăn chỉnh sửa nếu là danh mục mặc định của hệ thống
        if (category.isDefault()) {
            return "Không thể sửa đổi danh mục mặc định của hệ thống.";
        }

        // Kiểm tra tên trùng ở danh mục khác
        List<Category> list = categoryDAO.findByType(userId, type);
        for (Category cat : list) {
            if (cat.getCategoryId() != categoryId && cat.getCategoryName().equalsIgnoreCase(categoryName)) {
                return "Tên danh mục này đã tồn tại ở danh mục khác.";
            }
        }

        category.setCategoryName(categoryName);
        category.setType(type);
        category.setColor(color != null ? color : "#ADB5BD");

        boolean success = categoryDAO.update(category);
        if (success) {
            return null;
        }
        return "Lỗi cơ sở dữ liệu. Không thể cập nhật danh mục.";
    }

    /**
     * Xóa danh mục.
     * 
     * @return null nếu thành công, thông báo lỗi tiếng Việt nếu thất bại.
     */
    public String deleteCategory(int categoryId) {
        Category category = categoryDAO.findById(categoryId);
        if (category == null) {
            return "Danh mục không tồn tại.";
        }

        // Ngăn xóa nếu là danh mục hệ thống mặc định
        if (category.isDefault()) {
            return "Không thể xóa danh mục mặc định của hệ thống.";
        }

        // Kiểm tra xem danh mục có đang được sử dụng trong Transactions hoặc Budgets không
        if (isCategoryReferenced(categoryId)) {
            return "Không thể xóa danh mục này vì đã có dữ liệu giao dịch hoặc ngân sách liên quan.";
        }

        boolean success = categoryDAO.delete(categoryId);
        if (success) {
            return null;
        }
        return "Lỗi cơ sở dữ liệu. Không thể xóa danh mục.";
    }

    /**
     * Kiểm tra xem danh mục có bị ràng buộc ngoại khóa bởi bảng khác không.
     */
    private boolean isCategoryReferenced(int categoryId) {
        String sqlTrans = "SELECT COUNT(*) FROM Transactions WHERE category_id = ?";
        String sqlBudget = "SELECT COUNT(*) FROM Budgets WHERE category_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Kiểm tra Transactions
            try (PreparedStatement ps = conn.prepareStatement(sqlTrans)) {
                ps.setInt(1, categoryId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return true;
                    }
                }
            }
            // Kiểm tra Budgets
            try (PreparedStatement ps = conn.prepareStatement(sqlBudget)) {
                ps.setInt(1, categoryId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
