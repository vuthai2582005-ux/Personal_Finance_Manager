package personalfinancemanager.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp quản lý kết nối SQL Server (Singleton Pattern).
 */
public class DatabaseConnection {
    private static final String DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    static {
        try {
            // Đăng ký JDBC Driver của Microsoft SQL Server
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy Microsoft SQL Server JDBC Driver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy kết nối mới tới database.
     * Người gọi có trách nhiệm đóng kết nối này sau khi sử dụng (ví dụ sử dụng try-with-resources).
     * 
     * @return Connection tới SQL Server
     * @throws SQLException nếu xảy ra lỗi kết nối
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(AppConstants.DB_URL, AppConstants.DB_USER, AppConstants.DB_PASSWORD);
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu: URL=" + AppConstants.DB_URL);
            System.err.println("Chi tiết lỗi: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Hàm kiểm tra nhanh kết nối cơ sở dữ liệu.
     * 
     * @return true nếu kết nối thành công, false nếu thất bại
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Kết nối SQL Server thành công!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Kết nối thử nghiệm thất bại!");
        }
        return false;
    }
}
