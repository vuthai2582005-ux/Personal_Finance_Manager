package personalfinancemanager.servlet;

import personalfinancemanager.controller.AccountController;
import personalfinancemanager.model.Account;
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
 * Servlet xử lý các API liên quan đến tài khoản tài chính.
 */
@WebServlet(name = "AccountServlet", urlPatterns = {"/api/accounts/*"})
public class AccountServlet extends HttpServlet {

    private AccountController accountController;

    @Override
    public void init() throws ServletException {
        accountController = new AccountController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        List<Account> accounts = accountController.getAccountsByUserId(currentUser.getUserId());

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < accounts.size(); i++) {
            Account acc = accounts.get(i);
            sb.append(String.format(
                "{\"accountId\":%d,\"accountName\":\"%s\",\"accountType\":\"%s\",\"balance\":%s,\"currency\":\"%s\",\"active\":%b}",
                acc.getAccountId(),
                JsonResponseHelper.escapeJson(acc.getAccountName()),
                JsonResponseHelper.escapeJson(acc.getAccountType()),
                acc.getBalance().toString(),
                JsonResponseHelper.escapeJson(acc.getCurrency()),
                acc.isActive()
            ));
            if (i < accounts.size() - 1) {
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
            String accountName = request.getParameter("accountName");
            String accountType = request.getParameter("accountType");
            String initialBalance = request.getParameter("initialBalance");

            String errorMsg = accountController.addAccount(accountName, accountType, initialBalance, currentUser.getUserId());
            if (errorMsg == null) {
                JsonResponseHelper.sendSuccess(response, "Thêm tài khoản thành công!");
            } else {
                JsonResponseHelper.sendError(response, errorMsg);
            }
        } else if ("update".equals(action)) {
            int accountId = Integer.parseInt(request.getParameter("accountId"));
            String accountName = request.getParameter("accountName");
            String accountType = request.getParameter("accountType");
            String balance = request.getParameter("balance");
            boolean isActive = Boolean.parseBoolean(request.getParameter("active"));

            String errorMsg = accountController.updateAccount(accountId, accountName, accountType, balance, isActive, currentUser.getUserId());
            if (errorMsg == null) {
                JsonResponseHelper.sendSuccess(response, "Cập nhật tài khoản thành công!");
            } else {
                JsonResponseHelper.sendError(response, errorMsg);
            }
        } else if ("delete".equals(action)) {
            int accountId = Integer.parseInt(request.getParameter("accountId"));
            String errorMsg = accountController.deleteAccount(accountId);
            if (errorMsg == null) {
                JsonResponseHelper.sendSuccess(response, "Xóa tài khoản thành công!");
            } else {
                JsonResponseHelper.sendError(response, errorMsg);
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
