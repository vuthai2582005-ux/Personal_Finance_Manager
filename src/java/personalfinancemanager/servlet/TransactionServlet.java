package personalfinancemanager.servlet;

import personalfinancemanager.controller.TransactionController;
import personalfinancemanager.dao.TransactionDAO;
import personalfinancemanager.model.Transaction;
import personalfinancemanager.model.User;
import personalfinancemanager.util.DateUtil;
import personalfinancemanager.util.JsonResponseHelper;
import personalfinancemanager.util.SessionManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.util.List;

/**
 * Servlet xử lý các API liên quan đến giao dịch thu chi.
 */
@WebServlet(name = "TransactionServlet", urlPatterns = {"/api/transactions/*"})
public class TransactionServlet extends HttpServlet {

    private TransactionController transactionController;
    private TransactionDAO transactionDAO;

    @Override
    public void init() throws ServletException {
        transactionController = new TransactionController();
        transactionDAO = new TransactionDAO(); // Để dùng trực tiếp phương thức lấy giao dịch gần đây
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        int userId = currentUser.getUserId();

        String limitParam = request.getParameter("limit");
        List<Transaction> list;

        if (limitParam != null && !limitParam.isEmpty()) {
            int limit = Integer.parseInt(limitParam);
            list = transactionDAO.getRecentTransactions(userId, limit);
        } else {
            // Lọc giao dịch
            Integer accountId = null;
            String accParam = request.getParameter("accountId");
            if (accParam != null && !accParam.isEmpty()) {
                accountId = Integer.parseInt(accParam);
            }

            Integer categoryId = null;
            String catParam = request.getParameter("categoryId");
            if (catParam != null && !catParam.isEmpty()) {
                categoryId = Integer.parseInt(catParam);
            }

            String type = request.getParameter("type");
            if (type != null && type.isEmpty()) {
                type = null;
            }

            Date fromDate = null;
            String fromParam = request.getParameter("fromDate"); // Yêu cầu dạng dd/MM/yyyy hoặc yyyy-MM-dd
            if (fromParam != null && !fromParam.isEmpty()) {
                try {
                    if (fromParam.contains("-")) {
                        fromDate = Date.valueOf(fromParam);
                    } else {
                        fromDate = DateUtil.parseDate(fromParam);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi parse fromDate: " + fromParam);
                }
            }

            Date toDate = null;
            String toParam = request.getParameter("toDate");
            if (toParam != null && !toParam.isEmpty()) {
                try {
                    if (toParam.contains("-")) {
                        toDate = Date.valueOf(toParam);
                    } else {
                        toDate = DateUtil.parseDate(toParam);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi parse toDate: " + toParam);
                }
            }

            list = transactionController.getTransactionsByFilters(userId, accountId, categoryId, type, fromDate, toDate);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            Transaction t = list.get(i);
            sb.append(String.format(
                "{\"transactionId\":%d,\"accountId\":%d,\"accountName\":\"%s\",\"categoryId\":%d,\"categoryName\":\"%s\",\"type\":\"%s\",\"amount\":%s,\"transactionDate\":\"%s\",\"rawDate\":\"%s\",\"description\":\"%s\"}",
                t.getTransactionId(),
                t.getAccountId(),
                JsonResponseHelper.escapeJson(t.getAccountName()),
                t.getCategoryId(),
                JsonResponseHelper.escapeJson(t.getCategoryName()),
                JsonResponseHelper.escapeJson(t.getType()),
                t.getAmount().toString(),
                DateUtil.formatDate(t.getTransactionDate()),
                t.getTransactionDate().toString(),
                JsonResponseHelper.escapeJson(t.getDescription())
            ));
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");

        JsonResponseHelper.sendJson(response, sb.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");

        if ("add".equals(action)) {
            int accountId = Integer.parseInt(request.getParameter("accountId"));
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            String type = request.getParameter("type");
            String amount = request.getParameter("amount");
            String date = request.getParameter("date"); // Định dạng có thể là yyyy-MM-dd hoặc dd/MM/yyyy
            String description = request.getParameter("description");

            // Chuẩn hóa định dạng ngày từ yyyy-MM-dd sang dd/MM/yyyy để chuyển vào controller
            if (date != null && date.contains("-")) {
                try {
                    Date sqlDate = Date.valueOf(date);
                    date = DateUtil.formatDate(sqlDate);
                } catch (IllegalArgumentException e) {
                    JsonResponseHelper.sendError(response, "Định dạng ngày không hợp lệ.");
                    return;
                }
            }

            String errorMsg = transactionController.addTransaction(accountId, categoryId, type, amount, date, description);
            if (errorMsg == null) {
                JsonResponseHelper.sendSuccess(response, "Thêm giao dịch thành công!");
            } else {
                JsonResponseHelper.sendError(response, errorMsg);
            }
        } else if ("update".equals(action)) {
            int transactionId = Integer.parseInt(request.getParameter("transactionId"));
            int accountId = Integer.parseInt(request.getParameter("accountId"));
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            String type = request.getParameter("type");
            String amount = request.getParameter("amount");
            String date = request.getParameter("date");
            String description = request.getParameter("description");

            if (date != null && date.contains("-")) {
                try {
                    Date sqlDate = Date.valueOf(date);
                    date = DateUtil.formatDate(sqlDate);
                } catch (IllegalArgumentException e) {
                    JsonResponseHelper.sendError(response, "Định dạng ngày không hợp lệ.");
                    return;
                }
            }

            String errorMsg = transactionController.updateTransaction(transactionId, accountId, categoryId, type, amount, date, description);
            if (errorMsg == null) {
                JsonResponseHelper.sendSuccess(response, "Cập nhật giao dịch thành công!");
            } else {
                JsonResponseHelper.sendError(response, errorMsg);
            }
        } else if ("delete".equals(action)) {
            int transactionId = Integer.parseInt(request.getParameter("transactionId"));
            String errorMsg = transactionController.deleteTransaction(transactionId);
            if (errorMsg == null) {
                JsonResponseHelper.sendSuccess(response, "Xóa giao dịch thành công!");
            } else {
                JsonResponseHelper.sendError(response, errorMsg);
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
