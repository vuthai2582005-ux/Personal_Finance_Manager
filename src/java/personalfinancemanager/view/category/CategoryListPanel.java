package personalfinancemanager.view.category;

import personalfinancemanager.config.AppConstants;
import personalfinancemanager.controller.CategoryController;
import personalfinancemanager.model.Category;
import personalfinancemanager.util.SessionManager;
import personalfinancemanager.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Giao diện quản lý danh mục thu/chi (CRUD Categories - Tiếng Việt).
 */
public class CategoryListPanel extends JPanel {
    private final CategoryController categoryController;
    private List<Category> categoryList;
    private String currentFilterType = "ALL"; // ALL, INCOME, EXPENSE

    private JTable tblCategories;
    private DefaultTableModel tableModel;
    private RoundedButton btnAdd;
    private RoundedButton btnEdit;
    private RoundedButton btnDelete;

    private JRadioButton radAll;
    private JRadioButton radIncome;
    private JRadioButton radExpense;

    public CategoryListPanel() {
        this.categoryController = new CategoryController();
        initComponents();
        refreshData();
    }

    private void initComponents() {
        setBackground(AppConstants.COLOR_BG_LIGHT);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // 1. THANH TIÊU ĐỀ & HÀNH ĐỘNG
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh Mục Thu/Chi");
        lblTitle.setFont(AppConstants.FONT_TITLE);
        lblTitle.setForeground(AppConstants.COLOR_PRIMARY);
        topPanel.add(lblTitle, BorderLayout.WEST);

        // Nút hành động
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        toolbarPanel.setOpaque(false);

        btnAdd = new RoundedButton("Thêm Mới", AppConstants.COLOR_ACCENT);
        btnAdd.setPreferredSize(new Dimension(120, 35));
        toolbarPanel.add(btnAdd);

        btnEdit = new RoundedButton("Chỉnh Sửa", AppConstants.COLOR_WARNING);
        btnEdit.setPreferredSize(new Dimension(120, 35));
        toolbarPanel.add(btnEdit);

        btnDelete = new RoundedButton("Xóa Bỏ", AppConstants.COLOR_DANGER);
        btnDelete.setPreferredSize(new Dimension(120, 35));
        toolbarPanel.add(btnDelete);

        topPanel.add(toolbarPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2. PANEL CHÍNH CHỨA BỘ LỌC VÀ BẢNG
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Bộ lọc nhanh (Tất cả / Thu nhập / Chi tiêu)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        filterPanel.setOpaque(false);

        JLabel lblFilter = new JLabel("Bộ lọc nhanh:");
        lblFilter.setFont(AppConstants.FONT_SUBTITLE);
        lblFilter.setForeground(AppConstants.COLOR_TEXT_DARK);
        filterPanel.add(lblFilter);

        radAll = new JRadioButton("Tất cả", true);
        radAll.setFont(AppConstants.FONT_BODY);
        radAll.setOpaque(false);

        radIncome = new JRadioButton("Khoản thu");
        radIncome.setFont(AppConstants.FONT_BODY);
        radIncome.setOpaque(false);

        radExpense = new JRadioButton("Khoản chi");
        radExpense.setFont(AppConstants.FONT_BODY);
        radExpense.setOpaque(false);

        ButtonGroup filterGroup = new ButtonGroup();
        filterGroup.add(radAll);
        filterGroup.add(radIncome);
        filterGroup.add(radExpense);

        filterPanel.add(radAll);
        filterPanel.add(radIncome);
        filterPanel.add(radExpense);

        centerPanel.add(filterPanel, BorderLayout.NORTH);

        // Bảng danh mục
        String[] columns = {"Tên danh mục", "Phân loại", "Màu sắc", "Mặc định hệ thống"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblCategories = new JTable(tableModel);
        tblCategories.setFont(AppConstants.FONT_BODY);
        tblCategories.setRowHeight(35);
        tblCategories.setShowGrid(false);
        tblCategories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom Header
        JTableHeader header = tblCategories.getTableHeader();
        header.setFont(AppConstants.FONT_SUBTITLE);
        header.setBackground(AppConstants.COLOR_BG_LIGHT);
        header.setForeground(AppConstants.COLOR_TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 38));

        // Custom Renderer vẽ ô màu sắc trực quan
        tblCategories.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String hex = (String) value;
                try {
                    c.setBackground(Color.decode(hex));
                    c.setForeground(Color.decode(hex)); // Ẩn text bằng cách tô chữ trùng màu nền
                    setText(""); // Xóa text hiển thị mã hex để nhìn chuyên nghiệp hơn
                } catch (Exception e) {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblCategories);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // --- SỰ KIỆN ---

        // Xử lý sự kiện click bộ lọc nhanh
        ActionListener filterListener = e -> {
            if (radAll.isSelected()) {
                currentFilterType = "ALL";
            } else if (radIncome.isSelected()) {
                currentFilterType = "INCOME";
            } else if (radExpense.isSelected()) {
                currentFilterType = "EXPENSE";
            }
            refreshData();
        };

        radAll.addActionListener(filterListener);
        radIncome.addActionListener(filterListener);
        radExpense.addActionListener(filterListener);

        // Nút Thêm mới
        btnAdd.addActionListener(e -> {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(CategoryListPanel.this);
            CategoryFormDialog dialog = new CategoryFormDialog(parentFrame, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshData();
            }
        });

        // Nút Chỉnh sửa
        btnEdit.addActionListener(e -> {
            int selectedRow = tblCategories.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(CategoryListPanel.this,
                    "Vui lòng chọn danh mục muốn chỉnh sửa từ danh sách.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Category selectedCat = categoryList.get(selectedRow);
            if (selectedCat.isDefault()) {
                JOptionPane.showMessageDialog(CategoryListPanel.this,
                    "Không thể chỉnh sửa danh mục mặc định của hệ thống.",
                    "Không cho phép", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(CategoryListPanel.this);
            CategoryFormDialog dialog = new CategoryFormDialog(parentFrame, selectedCat);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshData();
            }
        });

        // Nút Xóa
        btnDelete.addActionListener(e -> {
            int selectedRow = tblCategories.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(CategoryListPanel.this,
                    "Vui lòng chọn danh mục muốn xóa từ danh sách.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Category selectedCat = categoryList.get(selectedRow);
            if (selectedCat.isDefault()) {
                JOptionPane.showMessageDialog(CategoryListPanel.this,
                    "Không thể xóa danh mục mặc định của hệ thống.",
                    "Không cho phép", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(CategoryListPanel.this,
                "Bạn có chắc chắn muốn xóa danh mục \"" + selectedCat.getCategoryName() + "\"?\n" +
                "Lưu ý: Không thể xóa danh mục đã liên kết với giao dịch hoặc ngân sách.",
                "Xác nhận xóa danh mục",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                String errorMsg = categoryController.deleteCategory(selectedCat.getCategoryId());
                if (errorMsg != null) {
                    JOptionPane.showMessageDialog(CategoryListPanel.this, errorMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(CategoryListPanel.this, "Đã xóa danh mục thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            }
        });
    }

    /**
     * Làm mới dữ liệu bảng từ DB.
     */
    public void refreshData() {
        if (SessionManager.getInstance().getCurrentUser() == null) return;

        int userId = SessionManager.getInstance().getCurrentUser().getUserId();
        
        if ("INCOME".equals(currentFilterType)) {
            categoryList = categoryController.getCategoriesByType(userId, "INCOME");
        } else if ("EXPENSE".equals(currentFilterType)) {
            categoryList = categoryController.getCategoriesByType(userId, "EXPENSE");
        } else {
            categoryList = categoryController.getCategoriesByUserId(userId);
        }

        tableModel.setRowCount(0);
        for (Category cat : categoryList) {
            String typeText = "INCOME".equals(cat.getType()) ? "Thu nhập" : "Chi tiêu";
            String defaultText = cat.isDefault() ? "Có (Hệ thống)" : "Không";
            tableModel.addRow(new Object[]{
                cat.getCategoryName(),
                typeText,
                cat.getColor(),
                defaultText
            });
        }
    }
}
