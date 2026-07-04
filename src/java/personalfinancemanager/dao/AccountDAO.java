package personalfinancemanager.dao;

import personalfinancemanager.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho bảng Accounts (Tài khoản tài chính).
 */
public class AccountDAO extends BaseDAO {

    /**
     * Lấy danh sách tài khoản hoạt động của người dùng.
     */
    public List<Account> findByUserId(int userId) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM Accounts WHERE user_id = ? AND is_active = 1 ORDER BY created_at DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapAccount(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm tài khoản theo user_id: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }

    /**
     * Lấy chi tiết tài khoản theo ID.
     */
    public Account findById(int accountId) {
        String sql = "SELECT * FROM Accounts WHERE account_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, accountId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapAccount(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm tài khoản theo ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    /**
     * Thêm tài khoản mới.
     */
    public boolean insert(Account account) {
        String sql = "INSERT INTO Accounts (user_id, account_name, account_type, balance, currency, is_active, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, GETDATE())";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, account.getUserId());
            ps.setString(2, account.getAccountName());
            ps.setString(3, account.getAccountType());
            ps.setBigDecimal(4, account.getBalance());
            ps.setString(5, account.getCurrency());
            ps.setBoolean(6, account.isActive());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    account.setAccountId(rs.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm tài khoản mới: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return false;
    }

    /**
     * Cập nhật thông tin tài khoản (Tên, loại, số dư).
     */
    public boolean update(Account account) {
        String sql = "UPDATE Accounts SET account_name = ?, account_type = ?, balance = ?, is_active = ? WHERE account_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, account.getAccountName());
            ps.setString(2, account.getAccountType());
            ps.setBigDecimal(3, account.getBalance());
            ps.setBoolean(4, account.isActive());
            ps.setInt(5, account.getAccountId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật tài khoản: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, null);
        }
        return false;
    }

    /**
     * Xóa tài khoản (Đặt trạng thái hoặc xóa vật lý nếu không ràng buộc ngoại khóa).
     * Dùng xóa vật lý vì database có Cascade Delete hoặc ta sẽ kiểm tra giao dịch ở controller.
     */
    public boolean delete(int accountId) {
        // Kiểm tra xem tài khoản có giao dịch nào ràng buộc không trước khi xóa
        if (hasTransactions(accountId)) {
            // Không được xóa vật lý -> Đổi trạng thái hoạt động thành false
            String sql = "UPDATE Accounts SET is_active = 0 WHERE account_id = ?";
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = getConnection();
                ps = conn.prepareStatement(sql);
                ps.setInt(1, accountId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Lỗi đổi trạng thái hoạt động tài khoản: " + e.getMessage());
                return false;
            } finally {
                closeResources(conn, ps, null);
            }
        } else {
            // Xóa vật lý
            String sql = "DELETE FROM Accounts WHERE account_id = ?";
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = getConnection();
                ps = conn.prepareStatement(sql);
                ps.setInt(1, accountId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Lỗi xóa vật lý tài khoản: " + e.getMessage());
                return false;
            } finally {
                closeResources(conn, ps, null);
            }
        }
    }

    /**
     * Kiểm tra xem tài khoản có chứa giao dịch nào không.
     */
    private boolean hasTransactions(int accountId) {
        String sql = "SELECT COUNT(*) FROM Transactions WHERE account_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, accountId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return false;
    }

    /**
     * Tính tổng số dư của tất cả tài khoản đang hoạt động của người dùng.
     */
    public BigDecimal getTotalBalanceByUserId(int userId) {
        String sql = "SELECT SUM(balance) AS total FROM Accounts WHERE user_id = ? AND is_active = 1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy tổng số dư tài khoản: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return BigDecimal.ZERO;
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setAccountId(rs.getInt("account_id"));
        account.setUserId(rs.getInt("user_id"));
        account.setAccountName(rs.getString("account_name"));
        account.setAccountType(rs.getString("account_type"));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setCurrency(rs.getString("currency"));
        account.setActive(rs.getBoolean("is_active"));
        account.setCreatedAt(rs.getTimestamp("created_at"));
        return account;
    }
}
