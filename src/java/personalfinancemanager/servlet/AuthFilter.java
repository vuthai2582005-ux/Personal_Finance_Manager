package personalfinancemanager.servlet;

import personalfinancemanager.model.User;
import personalfinancemanager.util.SessionManager;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter xử lý xác thực và quản lý Session ThreadLocal cho các Web API.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/api/*"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Khởi tạo filter
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Thiết lập mã hóa UTF-8 cho request/response
        httpRequest.setCharacterEncoding("UTF-8");
        httpResponse.setCharacterEncoding("UTF-8");

        // Lấy thông tin người dùng từ Session
        User currentUser = (User) httpRequest.getSession().getAttribute("currentUser");

        // Lấy đường dẫn tương đối của API
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        boolean isPublicApi = path.equals("/api/auth/login") || path.equals("/api/auth/register") || path.equals("/api/auth/me");

        if (currentUser == null && !isPublicApi) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().print("{\"success\":false,\"message\":\"Phiên làm việc hết hạn hoặc chưa đăng nhập. Vui lòng đăng nhập lại.\"}");
            return;
        }

        try {
            if (currentUser != null) {
                // Thiết lập người dùng hiện tại vào ThreadLocal của SessionManager
                SessionManager.getInstance().setCurrentUser(currentUser);
            }

            // Tiếp tục chuỗi filter / servlet
            chain.doFilter(request, response);
            
        } finally {
            // Luôn dọn dẹp ThreadLocal khi kết thúc request để tránh rò rỉ bộ nhớ (memory leak)
            SessionManager.getInstance().clear();
        }
    }

    @Override
    public void destroy() {
        // Hủy filter
    }
}
