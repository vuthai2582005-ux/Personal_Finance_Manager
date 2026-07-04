package personalfinancemanager.servlet;

import personalfinancemanager.controller.BudgetController;
import personalfinancemanager.model.Budget;
import personalfinancemanager.model.User;
import personalfinancemanager.util.JsonResponseHelper;
import personalfinancemanager.util.SessionManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet xử lý các API liên quan đến ngân sách chi tiêu.
 */
@WebServlet(name = "BudgetServlet", urlPatterns = {"/api/budgets/*"})
public class BudgetServlet extends HttpServlet {

    private BudgetController budgetController;

    @Override
    public void init() throws ServletException {
        budgetController = new BudgetController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        List<Budget> budgets = budgetController.getBudgetsByUserId(currentUser.getUserId());

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < budgets.size(); i++) {
            Budget b = budgets.get(i);
            sb.append(String.format(
                "{\"budgetId\":%d,\"categoryId\":%d,\"categoryName\":\"%s\",\"month\":%d,\"year\":%d,\"budgetAmount\":%s,\"spentAmount\":%s}",
                b.getBudgetId(),
                b.getCategoryId(),
                JsonResponseHelper.escapeJson(b.getCategoryName()),
                b.getMonth(),
                b.getYear(),
                b.getBudgetAmount().toString(),
                b.getSpentAmount().toString()
            ));
            if (i < budgets.size() - 1) {
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
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if ("add".equals(action)) {
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            int month = Integer.parseInt(request.getParameter("month"));
            int year = Integer.parseInt(request.getParameter("year"));
            String limit = request.getParameter("limit");

            String errorMsg = budgetController.addBudget(categoryId, month, year, limit, currentUser.getUserId());
            if (errorMsg == null) {
                JsonResponseHelper.sendSuccess(response, "Thêm ngân sách thành công!");
            } else {
                JsonResponseHelper.sendError(response, errorMsg);
            }
        } else if ("update".equals(action)) {
            int budgetId = Integer.parseInt(request.getParameter("budgetId"));
            String limit = request.getParameter("limit");

            String errorMsg = budgetController.updateBudget(budgetId, limit);
            if (errorMsg == null) {
                JsonResponseHelper.sendSuccess(response, "Cập nhật ngân sách thành công!");
            } else {
                JsonResponseHelper.sendError(response, errorMsg);
            }
        } else if ("delete".equals(action)) {
            int budgetId = Integer.parseInt(request.getParameter("budgetId"));
            String errorMsg = budgetController.deleteBudget(budgetId);
            if (errorMsg == null) {
                JsonResponseHelper.sendSuccess(response, "Xóa ngân sách thành công!");
            } else {
                JsonResponseHelper.sendError(response, errorMsg);
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
