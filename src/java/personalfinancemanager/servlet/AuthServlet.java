package personalfinancemanager.servlet;

import personalfinancemanager.controller.AuthController;
import personalfinancemanager.model.User;
import personalfinancemanager.util.JsonResponseHelper;
import personalfinancemanager.util.SessionManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet xử lý các API liên quan đến xác thực người dùng.
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/api/auth/*"})
public class AuthServlet extends HttpServlet {

    private AuthController authController;

    @Override
    public void init() throws ServletException {
        authController = new AuthController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        if ("/me".equals(pathInfo)) {
            handleMe(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        if ("/login".equals(pathInfo)) {
            handleLogin(request, response);
        } else if ("/register".equals(pathInfo)) {
            handleRegister(request, response);
        } else if ("/logout".equals(pathInfo)) {
            handleLogout(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleMe(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            JsonResponseHelper.sendJson(response, "{\"loggedIn\":false}");
        } else {
            String json = String.format(
                "{\"loggedIn\":true,\"user\":{\"userId\":%d,\"username\":\"%s\",\"fullName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\"}}",
                currentUser.getUserId(),
                JsonResponseHelper.escapeJson(currentUser.getUsername()),
                JsonResponseHelper.escapeJson(currentUser.getFullName()),
                JsonResponseHelper.escapeJson(currentUser.getEmail()),
                JsonResponseHelper.escapeJson(currentUser.getPhone())
            );
            JsonResponseHelper.sendJson(response, json);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String errorMsg = authController.login(username, password);

        if (errorMsg == null) {
            // Đăng nhập thành công, lưu thông tin vào HttpSession
            User user = SessionManager.getInstance().getCurrentUser();
            request.getSession().setAttribute("currentUser", user);

            String json = String.format(
                "{\"success\":true,\"message\":\"Đăng nhập thành công!\",\"user\":{\"userId\":%d,\"username\":\"%s\",\"fullName\":\"%s\"}}",
                user.getUserId(),
                JsonResponseHelper.escapeJson(user.getUsername()),
                JsonResponseHelper.escapeJson(user.getFullName())
            );
            JsonResponseHelper.sendJson(response, json);
        } else {
            JsonResponseHelper.sendError(response, errorMsg);
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");

        String errorMsg = authController.register(username, password, confirmPassword, fullName, email, phone);

        if (errorMsg == null) {
            JsonResponseHelper.sendSuccess(response, "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
        } else {
            JsonResponseHelper.sendError(response, errorMsg);
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        request.getSession().removeAttribute("currentUser");
        request.getSession().invalidate();
        SessionManager.getInstance().logout();

        JsonResponseHelper.sendSuccess(response, "Đăng xuất thành công!");
    }
}
