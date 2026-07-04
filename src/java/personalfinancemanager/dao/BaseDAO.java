package personalfinancemanager.dao;

import personalfinancemanager.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Lớp cơ sở (Abstract Base Class) cho tất cả các DAO.
 */
public abstract class BaseDAO {
    
    /**
     * Lấy kết nối cơ sở dữ liệu.
     * 
     * @return Connection đối tượng kết nối
     * @throws SQLException nếu kết nối thất bại
     */
    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    /**
     * Đóng an toàn các tài nguyên JDBC để tránh rò rỉ tài nguyên.
     */
    protected void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Lỗi đóng ResultSet: " + e.getMessage());
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Lỗi đóng Statement: " + e.getMessage());
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Lỗi đóng Connection: " + e.getMessage());
            }
        }
    }
}
