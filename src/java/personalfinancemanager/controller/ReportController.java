package personalfinancemanager.controller;

import personalfinancemanager.dao.ReportDAO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller điều phối các nghiệp vụ báo cáo, thống kê và tính toán tỷ lệ.
 */
public class ReportController {
    private final ReportDAO reportDAO;

    public ReportController() {
        this.reportDAO = new ReportDAO();
    }

    /**
     * Lấy danh sách cơ cấu thu/chi theo danh mục.
     */
    public List<ReportDAO.CategoryReportEntry> getCategoryReport(int userId, String type, int month, int year) {
        return reportDAO.getCategoryReport(userId, type, month, year);
    }

    /**
     * Lấy danh sách xu hướng thu chi 12 tháng.
     */
    public List<ReportDAO.MonthlyReportTrendEntry> getMonthlyReportTrend(int userId, int year) {
        return reportDAO.getMonthlyReportTrend(userId, year);
    }

    /**
     * Tính tổng số tiền từ danh sách báo cáo danh mục.
     */
    public BigDecimal calculateTotalCategoryAmount(List<ReportDAO.CategoryReportEntry> entries) {
        BigDecimal total = BigDecimal.ZERO;
        if (entries != null) {
            for (ReportDAO.CategoryReportEntry entry : entries) {
                total = total.add(entry.getTotalAmount());
            }
        }
        return total;
    }
}
