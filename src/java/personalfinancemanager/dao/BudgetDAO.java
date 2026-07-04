package personalfinancemanager.dao;

import personalfinancemanager.model.Budget;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho bảng Budgets (Ngân sách chi tiêu).
 */
public class BudgetDAO extends BaseDAO {

    /**
     * Lấy danh sách ngân sách của người dùng (kèm thông tin Danh mục và trạng thái % sử dụng).
     * Query từ View vw_BudgetStatus.
     */
    public List<Budget> findByUserId(int userId) {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_BudgetStatus WHERE user_id = ? ORDER BY year DESC, month DESC, category_name ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            while (rs.next()) {
                Budget b = new Budget();
                b.setBudgetId(rs.getInt("budget_id"));
                b.setUserId(rs.getInt("user_id"));
                b.setMonth(rs.getInt("month"));
                b.setYear(rs.getInt("year"));
                b.setBudgetAmount(rs.getBigDecimal("budget_amount"));
                b.setSpentAmount(rs.getBigDecimal("spent_amount"));
                // Gán thêm thông tin mở rộng từ View
                b.setRemainingAmount(rs.getBigDecimal("remaining_amount"));
                b.setUsagePercent(rs.getDouble("usage_percent"));
                b.setCategoryName(rs.getString("category_name"));
                list.add(b);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách ngân sách theo user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }

    /**
     * Tìm ngân sách theo ID (kèm thông tin từ View).
     */
    public Budget findById(int budgetId) {
        String sql = "SELECT * FROM vw_BudgetStatus WHERE budget_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, budgetId);
            rs = ps.executeQuery();

            if (rs.next()) {
                Budget b = new Budget();
                b.setBudgetId(rs.getInt("budget_id"));
                b.setUserId(rs.getInt("user_id"));
                b.setMonth(rs.getInt("month"));
                b.setYear(rs.getInt("year"));
                b.setBudgetAmount(rs.getBigDecimal("budget_amount"));
                b.setSpentAmount(rs.getBigDecimal("spent_amount"));
                b.setRemainingAmount(rs.getBigDecimal("remaining_amount"));
                b.setUsagePercent(rs.getDouble("usage_percent"));
                b.setCategoryName(rs.getString("category_name"));
                return b;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy chi tiết ngân sách: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    /**
     * Tìm kiếm ngân sách cho một Danh mục cụ thể trong Tháng/Năm.
     */
    public Budget findByUserAndCategoryAndMonth(int userId, int categoryId, int month, int year) {
        String sql = "SELECT * FROM Budgets WHERE user_id = ? AND category_id = ? AND month = ? AND year = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ps.setInt(3, month);
            ps.setInt(4, year);
            rs = ps.executeQuery();

            if (rs.next()) {
                Budget b = new Budget();
                b.setBudgetId(rs.getInt("budget_id"));
                b.setUserId(rs.getInt("user_id"));
                b.setCategoryId(rs.getInt("category_id"));
                b.setMonth(rs.getInt("month"));
                b.setYear(rs.getInt("year"));
                b.setBudgetAmount(rs.getBigDecimal("budget_amount"));
                b.setSpentAmount(rs.getBigDecimal("spent_amount"));
                return b;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm kiếm ngân sách theo danh mục và tháng: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    /**
     * Thêm ngân sách mới.
     * Tự động tính toán số tiền đã tiêu ban đầu dựa trên các giao dịch chi tiêu trước đó trong tháng/năm.
     */
    public boolean insert(Budget budget) {
        String sqlInsert = "INSERT INTO Budgets (user_id, category_id, month, year, budget_amount, spent_amount, created_at) " +
                           "VALUES (?, ?, ?, ?, ?, ?, GETDATE())";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            
            // Tính số tiền đã chi tiêu của danh mục này trong tháng hiện tại trước khi tạo ngân sách
            BigDecimal initialSpent = calculateInitialSpent(conn, budget.getUserId(), budget.getCategoryId(), budget.getMonth(), budget.getYear());
            budget.setSpentAmount(initialSpent);

            ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, budget.getUserId());
            ps.setInt(2, budget.getCategoryId());
            ps.setInt(3, budget.getMonth());
            ps.setInt(4, budget.getYear());
            ps.setBigDecimal(5, budget.getBudgetAmount());
            ps.setBigDecimal(6, budget.getSpentAmount());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    budget.setBudgetId(rs.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thêm ngân sách mới: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return false;
    }

    /**
     * Cập nhật hạn mức ngân sách.
     */
    public boolean update(Budget budget) {
        String sql = "UPDATE Budgets SET budget_amount = ? WHERE budget_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setBigDecimal(1, budget.getBudgetAmount());
            ps.setInt(2, budget.getBudgetId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật ngân sách: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, null);
        }
        return false;
    }

    /**
     * Xóa ngân sách.
     */
    public boolean delete(int budgetId) {
        String sql = "DELETE FROM Budgets WHERE budget_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, budgetId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa ngân sách: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, null);
        }
        return false;
    }

    /**
     * Tính toán tổng chi tiêu của một Danh mục trong Tháng/Năm từ bảng Transactions.
     */
    private BigDecimal calculateInitialSpent(Connection conn, int userId, int categoryId, int month, int year) throws SQLException {
        String sql = "SELECT SUM(t.amount) AS total FROM Transactions t " +
                     "INNER JOIN Accounts a ON t.account_id = a.account_id " +
                     "WHERE a.user_id = ? AND t.category_id = ? AND t.type = 'EXPENSE' " +
                     "AND MONTH(t.transaction_date) = ? AND YEAR(t.transaction_date) = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ps.setInt(3, month);
            ps.setInt(4, year);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }
}
