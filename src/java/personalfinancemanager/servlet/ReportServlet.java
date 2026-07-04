package personalfinancemanager.servlet;

import personalfinancemanager.controller.ReportController;
import personalfinancemanager.dao.ReportDAO;
import personalfinancemanager.model.User;
import personalfinancemanager.util.JsonResponseHelper;
import personalfinancemanager.util.SessionManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Servlet xử lý các API thống kê báo cáo.
 */
@WebServlet(name = "ReportServlet", urlPatterns = {"/api/reports/*"})
public class ReportServlet extends HttpServlet {

    private ReportController reportController;

    @Override
    public void init() throws ServletException {
        reportController = new ReportController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        User currentUser = SessionManager.getInstance().getCurrentUser();
        int userId = currentUser.getUserId();

        if ("/category".equals(pathInfo)) {
            // Lấy báo cáo cơ cấu danh mục
            String type = request.getParameter("type");
            if (type == null || type.isEmpty()) {
                type = "EXPENSE"; // Mặc định là chi tiêu
            } else {
                type = type.toUpperCase();
            }

            LocalDate now = LocalDate.now();
            int month = now.getMonthValue();
            int year = now.getYear();

            String monthStr = request.getParameter("month");
            if (monthStr != null && !monthStr.isEmpty()) {
                month = Integer.parseInt(monthStr);
            }

            String yearStr = request.getParameter("year");
            if (yearStr != null && !yearStr.isEmpty()) {
                year = Integer.parseInt(yearStr);
            }

            List<ReportDAO.CategoryReportEntry> entries = reportController.getCategoryReport(userId, type, month, year);

            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < entries.size(); i++) {
                ReportDAO.CategoryReportEntry entry = entries.get(i);
                sb.append(String.format(
                    "{\"categoryName\":\"%s\",\"totalAmount\":%s}",
                    JsonResponseHelper.escapeJson(entry.getCategoryName()),
                    entry.getTotalAmount().toString()
                ));
                if (i < entries.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");

            JsonResponseHelper.sendJson(response, sb.toString());

        } else if ("/trend".equals(pathInfo)) {
            // Lấy xu hướng thu chi 12 tháng
            int year = LocalDate.now().getYear();
            String yearStr = request.getParameter("year");
            if (yearStr != null && !yearStr.isEmpty()) {
                year = Integer.parseInt(yearStr);
            }

            List<ReportDAO.MonthlyReportTrendEntry> entries = reportController.getMonthlyReportTrend(userId, year);

            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < entries.size(); i++) {
                ReportDAO.MonthlyReportTrendEntry entry = entries.get(i);
                sb.append(String.format(
                    "{\"month\":%d,\"totalIncome\":%s,\"totalExpense\":%s}",
                    entry.getMonth(),
                    entry.getTotalIncome().toString(),
                    entry.getTotalExpense().toString()
                ));
                if (i < entries.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");

            JsonResponseHelper.sendJson(response, sb.toString());

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
