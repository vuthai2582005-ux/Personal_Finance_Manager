package personalfinancemanager.dao;

import personalfinancemanager.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho danh mục thu/chi (Categories).
 */
public class CategoryDAO extends BaseDAO {

    /**
     * Thêm các danh mục mặc định cho người dùng mới đăng ký.
     */
    public boolean insertDefaultCategories(int userId) {
        String sql = "INSERT INTO Categories (user_id, category_name, type, icon, color, is_default) VALUES (?, ?, ?, ?, ?, 1)";
        
        Object[][] defaultCategories = {
            // Chi tiêu (EXPENSE)
            {"Ăn uống", "EXPENSE", "fork-knife", "#FF6B6B"},
            {"Di chuyển", "EXPENSE", "car", "#FF8E53"},
            {"Mua sắm", "EXPENSE", "shopping-bag", "#FFA94D"},
            {"Hóa đơn & Tiện ích", "EXPENSE", "zap", "#FFD93D"},
            {"Giải trí", "EXPENSE", "gamepad", "#6BCB77"},
            {"Y tế", "EXPENSE", "heart-pulse", "#4D96FF"},
            {"Giáo dục", "EXPENSE", "book-open", "#C77DFF"},
            {"Chi tiêu khác", "EXPENSE", "more-horizontal", "#ADB5BD"},
            
            // Thu nhập (INCOME)
            {"Lương", "INCOME", "briefcase", "#51CF66"},
            {"Thưởng", "INCOME", "gift", "#339AF0"},
            {"Đầu tư", "INCOME", "trending-up", "#F06595"},
            {"Thu nhập khác", "INCOME", "plus-circle", "#74C0FC"}
        };

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);

            for (Object[] cat : defaultCategories) {
                ps.setInt(1, userId);
                ps.setString(2, (String) cat[0]);
                ps.setString(3, (String) cat[1]);
                ps.setString(4, (String) cat[2]);
                ps.setString(5, (String) cat[3]);
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm danh mục mặc định cho user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            closeResources(conn, ps, null);
        }
        return false;
    }

    /**
     * Lấy toàn bộ danh mục của người dùng.
     */
    public List<Category> findByUserId(int userId) {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM Categories WHERE user_id = ? ORDER BY type DESC, category_name ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapCategory(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh mục theo user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }

    /**
     * Lấy danh mục theo loại (INCOME hoặc EXPENSE).
     */
    public List<Category> findByType(int userId, String type) {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM Categories WHERE user_id = ? AND type = ? ORDER BY category_name ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, type);
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapCategory(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh mục theo loại: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }

    /**
     * Tìm danh mục theo ID.
     */
    public Category findById(int categoryId) {
        String sql = "SELECT * FROM Categories WHERE category_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, categoryId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapCategory(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh mục theo ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    /**
     * Thêm danh mục mới.
     */
    public boolean insert(Category category) {
        String sql = "INSERT INTO Categories (user_id, category_name, type, icon, color, is_default) VALUES (?, ?, ?, ?, ?, 0)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, category.getUserId());
            ps.setString(2, category.getCategoryName());
            ps.setString(3, category.getType());
            ps.setString(4, category.getIcon());
            ps.setString(5, category.getColor());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    category.setCategoryId(rs.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thêm danh mục: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return false;
    }

    /**
     * Cập nhật danh mục.
     */
    public boolean update(Category category) {
        String sql = "UPDATE Categories SET category_name = ?, type = ?, icon = ?, color = ? WHERE category_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getType());
            ps.setString(3, category.getIcon());
            ps.setString(4, category.getColor());
            ps.setInt(5, category.getCategoryId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật danh mục: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, null);
        }
        return false;
    }

    /**
     * Xóa danh mục.
     */
    public boolean delete(int categoryId) {
        String sql = "DELETE FROM Categories WHERE category_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa danh mục: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, null);
        }
        return false;
    }

    private Category mapCategory(ResultSet rs) throws SQLException {
        Category cat = new Category();
        cat.setCategoryId(rs.getInt("category_id"));
        cat.setUserId(rs.getInt("user_id"));
        cat.setCategoryName(rs.getString("category_name"));
        cat.setType(rs.getString("type"));
        cat.setIcon(rs.getString("icon"));
        cat.setColor(rs.getString("color"));
        cat.setDefault(rs.getBoolean("is_default"));
        return cat;
    }
}
