package personalfinancemanager.dao;

import personalfinancemanager.model.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object cho bảng Transactions (Giao dịch).
 */
public class TransactionDAO extends BaseDAO {

    /**
     * Lấy danh sách giao dịch gần đây của người dùng (giới hạn số lượng).
     */
    public List<Transaction> getRecentTransactions(int userId, int limit) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT TOP (" + limit + ") * FROM vw_TransactionDetails WHERE user_id = ? " +
                     "ORDER BY transaction_date DESC, created_at DESC";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách giao dịch gần đây: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }

    /**
     * Lấy tổng thu nhập và tổng chi tiêu của người dùng trong tháng cụ thể của năm.
     */
    public Map<String, BigDecimal> getMonthlySummary(int userId, int month, int year) {
        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("INCOME", BigDecimal.ZERO);
        summary.put("EXPENSE", BigDecimal.ZERO);

        String sql = "SELECT type, SUM(amount) AS total FROM vw_TransactionDetails " +
                     "WHERE user_id = ? AND MONTH(transaction_date) = ? AND YEAR(transaction_date) = ? " +
                     "GROUP BY type";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            rs = ps.executeQuery();

            while (rs.next()) {
                String type = rs.getString("type");
                BigDecimal total = rs.getBigDecimal("total");
                if (total != null) {
                    summary.put(type, total);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy tổng hợp thu chi theo tháng: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return summary;
    }

    /**
     * Tìm chi tiết giao dịch theo ID.
     */
    public Transaction findById(int transactionId) {
        String sql = "SELECT * FROM vw_TransactionDetails WHERE transaction_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, transactionId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapTransaction(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm kiếm giao dịch theo ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    /**
     * Tìm kiếm và lọc danh sách giao dịch với nhiều tiêu chí.
     */
    public List<Transaction> findByFilters(int userId, Integer accountId, Integer categoryId, String type, Date fromDate, Date toDate) {
        List<Transaction> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM vw_TransactionDetails WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (accountId != null) {
            sql.append(" AND account_id = ?");
            params.add(accountId);
        }
        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        if (type != null && !type.equals("ALL")) {
            sql.append(" AND type = ?");
            params.add(type);
        }
        if (fromDate != null) {
            sql.append(" AND transaction_date >= ?");
            params.add(fromDate);
        }
        if (toDate != null) {
            sql.append(" AND transaction_date <= ?");
            params.add(toDate);
        }

        sql.append(" ORDER BY transaction_date DESC, created_at DESC");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn lọc giao dịch: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }

    /**
     * Thêm mới một giao dịch và cập nhật Số dư tài khoản cùng hạn mức Ngân sách.
     * Chạy dưới dạng 1 Transaction ACID.
     */
    public boolean insert(Transaction t) {
        String sqlInsert = "INSERT INTO Transactions (account_id, category_id, type, amount, transaction_date, description, created_at) " +
                           "VALUES (?, ?, ?, ?, ?, ?, GETDATE())";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // BẮT ĐẦU TRANSACTION

            // 1. Ghi giao dịch mới
            ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, t.getAccountId());
            ps.setInt(2, t.getCategoryId());
            ps.setString(3, t.getType());
            ps.setBigDecimal(4, t.getAmount());
            ps.setDate(5, t.getTransactionDate());
            ps.setString(6, t.getDescription());
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                t.setTransactionId(rs.getInt(1));
            }

            // Lấy user_id của tài khoản để thao tác ngân sách
            int userId = getUserIdByAccountId(conn, t.getAccountId());

            // 2. Đồng bộ số dư tài khoản
            updateAccountBalance(conn, t.getAccountId(), t.getAmount(), t.getType(), false);

            // 3. Đồng bộ chi tiêu của ngân sách (chỉ áp dụng đối với EXPENSE)
            if ("EXPENSE".equals(t.getType())) {
                updateBudgetSpentAmount(conn, userId, t.getCategoryId(), t.getTransactionDate(), t.getAmount(), false);
            }

            conn.commit(); // COMMIT TRANSACTION
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm giao dịch (Rollback): " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return false;
    }

    /**
     * Xóa một giao dịch và khôi phục Số dư tài khoản cùng hạn mức Ngân sách.
     * Chạy dưới dạng 1 Transaction ACID.
     */
    public boolean delete(int transactionId) {
        Transaction t = findById(transactionId);
        if (t == null) return false;

        String sqlDelete = "DELETE FROM Transactions WHERE transaction_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // BẮT ĐẦU TRANSACTION

            // Lấy user_id của tài khoản
            int userId = getUserIdByAccountId(conn, t.getAccountId());

            // 1. Hoàn trả số dư tài khoản (truyền tham số đảo ngược)
            updateAccountBalance(conn, t.getAccountId(), t.getAmount(), t.getType(), true);

            // 2. Hoàn trả chi tiêu ngân sách (truyền tham số đảo ngược)
            if ("EXPENSE".equals(t.getType())) {
                updateBudgetSpentAmount(conn, userId, t.getCategoryId(), t.getTransactionDate(), t.getAmount(), true);
            }

            // 3. Xóa giao dịch
            ps = conn.prepareStatement(sqlDelete);
            ps.setInt(1, transactionId);
            ps.executeUpdate();

            conn.commit(); // COMMIT TRANSACTION
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa giao dịch (Rollback): " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            closeResources(conn, ps, null);
        }
        return false;
    }

    /**
     * Cập nhật một giao dịch (Sửa thông tin).
     * Khôi phục toàn bộ thay đổi cũ và áp dụng thay đổi mới trong một ACID Transaction.
     */
    public boolean update(Transaction newT, Transaction oldT) {
        String sqlUpdate = "UPDATE Transactions SET account_id = ?, category_id = ?, type = ?, " +
                           "amount = ?, transaction_date = ?, description = ? WHERE transaction_id = ?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // BẮT ĐẦU TRANSACTION

            // --- A. HOÀN TÁC DỮ LIỆU CŨ ---
            int oldUserId = getUserIdByAccountId(conn, oldT.getAccountId());
            // Hoàn tác số dư tài khoản cũ (đảo ngược)
            updateAccountBalance(conn, oldT.getAccountId(), oldT.getAmount(), oldT.getType(), true);
            // Hoàn tác ngân sách cũ (đảo ngược)
            if ("EXPENSE".equals(oldT.getType())) {
                updateBudgetSpentAmount(conn, oldUserId, oldT.getCategoryId(), oldT.getTransactionDate(), oldT.getAmount(), true);
            }

            // --- B. ÁP DỤNG DỮ LIỆU MỚI ---
            int newUserId = getUserIdByAccountId(conn, newT.getAccountId());
            // Áp dụng số dư tài khoản mới
            updateAccountBalance(conn, newT.getAccountId(), newT.getAmount(), newT.getType(), false);
            // Áp dụng ngân sách mới
            if ("EXPENSE".equals(newT.getType())) {
                updateBudgetSpentAmount(conn, newUserId, newT.getCategoryId(), newT.getTransactionDate(), newT.getAmount(), false);
            }

            // Cập nhật bản ghi Transactions
            ps = conn.prepareStatement(sqlUpdate);
            ps.setInt(1, newT.getAccountId());
            ps.setInt(2, newT.getCategoryId());
            ps.setString(3, newT.getType());
            ps.setBigDecimal(4, newT.getAmount());
            ps.setDate(5, newT.getTransactionDate());
            ps.setString(6, newT.getDescription());
            ps.setInt(7, newT.getTransactionId());
            ps.executeUpdate();

            conn.commit(); // COMMIT TRANSACTION
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật giao dịch (Rollback): " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            closeResources(conn, ps, null);
        }
        return false;
    }

    // --- CÁC HÀM TIỆN ÍCH TRONG TRANSACTION ---

    private int getUserIdByAccountId(Connection conn, int accountId) throws SQLException {
        String sql = "SELECT user_id FROM Accounts WHERE account_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        throw new SQLException("Không tìm thấy tài khoản có ID=" + accountId);
    }

    private void updateAccountBalance(Connection conn, int accountId, BigDecimal amount, String type, boolean isRevert) throws SQLException {
        String sql;
        boolean isExpense = "EXPENSE".equals(type);
        
        // Nếu là chi tiêu và không phải hoàn tác (hoặc là thu nhập và đang hoàn tác) -> trừ số dư
        boolean shouldSubtract = (isExpense && !isRevert) || (!isExpense && isRevert);

        if (shouldSubtract) {
            sql = "UPDATE Accounts SET balance = balance - ? WHERE account_id = ?";
        } else {
            sql = "UPDATE Accounts SET balance = balance + ? WHERE account_id = ?";
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setInt(2, accountId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Không thể cập nhật số dư của tài khoản ID=" + accountId);
            }
        }
    }

    private void updateBudgetSpentAmount(Connection conn, int userId, int categoryId, Date date, BigDecimal amount, boolean isRevert) throws SQLException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH là 0-indexed
        int year = cal.get(Calendar.YEAR);

        String sql;
        if (isRevert) {
            sql = "UPDATE Budgets SET spent_amount = spent_amount - ? " +
                  "WHERE user_id = ? AND category_id = ? AND month = ? AND year = ?";
        } else {
            sql = "UPDATE Budgets SET spent_amount = spent_amount + ? " +
                  "WHERE user_id = ? AND category_id = ? AND month = ? AND year = ?";
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setInt(2, userId);
            ps.setInt(3, categoryId);
            ps.setInt(4, month);
            ps.setInt(5, year);
            ps.executeUpdate();
            // Không bắt buộc phải ném lỗi nếu không update dòng nào vì user có thể không đặt ngân sách cho danh mục này.
        }
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setType(rs.getString("type"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setTransactionDate(rs.getDate("transaction_date"));
        t.setDescription(rs.getString("description"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setAccountId(rs.getInt("account_id"));
        t.setAccountName(rs.getString("account_name"));
        t.setCategoryId(rs.getInt("category_id"));
        t.setCategoryName(rs.getString("category_name"));
        t.setCategoryColor(rs.getString("category_color"));
        t.setCategoryIcon(rs.getString("category_icon"));
        return t;
    }
}
