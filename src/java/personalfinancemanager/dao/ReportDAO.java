package personalfinancemanager.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object phục vụ báo cáo và thống kê dữ liệu.
 */
public class ReportDAO extends BaseDAO {

    /**
     * Lớp lưu trữ cấu trúc dữ liệu dòng báo cáo Cơ cấu theo Danh mục.
     */
    public static class CategoryReportEntry {
        private String categoryName;
        private BigDecimal totalAmount;

        public CategoryReportEntry(String categoryName, BigDecimal totalAmount) {
            this.categoryName = categoryName;
            this.totalAmount = totalAmount;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
    }

    /**
     * Lớp lưu trữ cấu trúc dữ liệu xu hướng thu chi hàng tháng.
     */
    public static class MonthlyReportTrendEntry {
        private int month;
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;

        public MonthlyReportTrendEntry(int month, BigDecimal totalIncome, BigDecimal totalExpense) {
            this.month = month;
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
        }

        public int getMonth() {
            return month;
        }

        public BigDecimal getTotalIncome() {
            return totalIncome;
        }

        public BigDecimal getTotalExpense() {
            return totalExpense;
        }
    }

    /**
     * Lấy báo cáo thống kê tổng tiền theo danh mục cho một tháng/năm cụ thể.
     */
    public List<CategoryReportEntry> getCategoryReport(int userId, String type, int month, int year) {
        List<CategoryReportEntry> list = new ArrayList<>();
        String sql = "SELECT category_name, SUM(amount) AS total_amount " +
                     "FROM vw_TransactionDetails " +
                     "WHERE user_id = ? AND type = ? AND MONTH(transaction_date) = ? AND YEAR(transaction_date) = ? " +
                     "GROUP BY category_name " +
                     "ORDER BY total_amount DESC";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setInt(3, month);
            ps.setInt(4, year);
            rs = ps.executeQuery();

            while (rs.next()) {
                String catName = rs.getString("category_name");
                BigDecimal amount = rs.getBigDecimal("total_amount");
                list.add(new CategoryReportEntry(catName, amount != null ? amount : BigDecimal.ZERO));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn báo cáo theo danh mục: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }

    /**
     * Lấy báo cáo xu hướng thu chi 12 tháng của một năm cụ thể.
     */
    public List<MonthlyReportTrendEntry> getMonthlyReportTrend(int userId, int year) {
        List<MonthlyReportTrendEntry> list = new ArrayList<>();
        // Truy vấn tổng hợp thu nhập & chi tiêu theo từng tháng từ view vw_MonthlyReport
        String sql = "SELECT month, " +
                     "       SUM(CASE WHEN type = 'INCOME' THEN total_amount ELSE 0 END) AS total_income, " +
                     "       SUM(CASE WHEN type = 'EXPENSE' THEN total_amount ELSE 0 END) AS total_expense " +
                     "FROM vw_MonthlyReport " +
                     "WHERE user_id = ? AND year = ? " +
                     "GROUP BY month " +
                     "ORDER BY month ASC";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, year);
            rs = ps.executeQuery();

            // Khởi tạo trước mảng 12 tháng với giá trị bằng 0
            MonthlyReportTrendEntry[] monthsData = new MonthlyReportTrendEntry[12];
            for (int i = 0; i < 12; i++) {
                monthsData[i] = new MonthlyReportTrendEntry(i + 1, BigDecimal.ZERO, BigDecimal.ZERO);
            }

            // Ghi đè dữ liệu thực tế từ cơ sở dữ liệu
            while (rs.next()) {
                int month = rs.getInt("month");
                BigDecimal income = rs.getBigDecimal("total_income");
                BigDecimal expense = rs.getBigDecimal("total_expense");
                
                if (month >= 1 && month <= 12) {
                    monthsData[month - 1] = new MonthlyReportTrendEntry(
                        month,
                        income != null ? income : BigDecimal.ZERO,
                        expense != null ? expense : BigDecimal.ZERO
                    );
                }
            }

            // Nạp dữ liệu vào danh sách trả về
            for (MonthlyReportTrendEntry entry : monthsData) {
                list.add(entry);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn xu hướng thu chi năm: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }
}
