package personalfinancemanager.servlet;

import personalfinancemanager.controller.CategoryController;
import personalfinancemanager.model.Category;
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
 * Servlet xử lý các API liên quan đến danh mục thu chi.
 */
@WebServlet(name = "CategoryServlet", urlPatterns = {"/api/categories/*"})
public class CategoryServlet extends HttpServlet {

    private CategoryController categoryController;

    @Override
    public void init() throws ServletException {
        categoryController = new CategoryController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String type = request.getParameter("type");
        
        List<Category> categories;
        if (type != null && ("INCOME".equalsIgnoreCase(type) || "EXPENSE".equalsIgnoreCase(type))) {
            categories = categoryController.getCategoriesByType(currentUser.getUserId(), type.toUpperCase());
        } else {
            categories = categoryController.getCategoriesByUserId(currentUser.getUserId());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            sb.append(String.format(
                "{\"categoryId\":%d,\"categoryName\":\"%s\",\"type\":\"%s\",\"icon\":\"%s\",\"color\":\"%s\",\"isDefault\":%b}",
                cat.getCategoryId(),
                JsonResponseHelper.escapeJson(cat.getCategoryName()),
                JsonResponseHelper.escapeJson(cat.getType()),
                JsonResponseHelper.escapeJson(cat.getIcon()),
                JsonResponseHelper.escapeJson(cat.getColor()),
                cat.isDefault()
            ));
            if (i < categories.size() - 1) {
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
            String categoryName = request.getParameter("categoryName");
            String type = request.getParameter("type");
            String color = request.getParameter("color");

            String errorMsg = categoryController.addCategory(categoryName, type, color, currentUser.getUserId());
            if (errorMsg == null) {
                JsonResponseHelper.sendSuccess(response, "Thêm danh mục thành công!");
            } else {
                JsonResponseHelper.sendError(response, errorMsg);
            }
        } else if ("update".equals(action)) {
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            String categoryName = request.getParameter("categoryName");
            String type = request.getParameter("type");
            String color = request.getParameter("color");

            String errorMsg = categoryController.updateCategory(categoryId, categoryName, type, color, currentUser.getUserId());
            if (errorMsg == null) {
                JsonResponseHelper.sendSuccess(response, "Cập nhật danh mục thành công!");
            } else {
                JsonResponseHelper.sendError(response, errorMsg);
            }
        } else if ("delete".equals(action)) {
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            String errorMsg = categoryController.deleteCategory(categoryId);
            if (errorMsg == null) {
                JsonResponseHelper.sendSuccess(response, "Xóa danh mục thành công!");
            } else {
                JsonResponseHelper.sendError(response, errorMsg);
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
