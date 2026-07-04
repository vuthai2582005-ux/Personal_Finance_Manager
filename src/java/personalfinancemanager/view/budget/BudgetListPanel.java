package personalfinancemanager.view.budget;

import personalfinancemanager.config.AppConstants;
import personalfinancemanager.controller.BudgetController;
import personalfinancemanager.model.Budget;
import personalfinancemanager.util.CurrencyFormatter;
import personalfinancemanager.util.SessionManager;
import personalfinancemanager.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;

/**
 * Giao diện quản lý danh sách ngân sách chi tiêu và tiến độ (JProgressBar - Tiếng Việt).
 */
public class BudgetListPanel extends JPanel {
    private final BudgetController budgetController;
    private List<Budget> budgetList;

    private JTable tblBudgets;
    private DefaultTableModel tableModel;
    private RoundedButton btnAdd;
    private RoundedButton btnEdit;
    private RoundedButton btnDelete;

    public BudgetListPanel() {
        this.budgetController = new BudgetController();
        initComponents();
        refreshData();
    }

    private void initComponents() {
        setBackground(AppConstants.COLOR_BG_LIGHT);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // 1. TIÊU ĐỀ TRANG & HÀNH ĐỘNG
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Ngân Sách Chi Tiêu");
        lblTitle.setFont(AppConstants.FONT_TITLE);
        lblTitle.setForeground(AppConstants.COLOR_PRIMARY);
        topPanel.add(lblTitle, BorderLayout.WEST);

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

        // 2. PANEL BẢNG HIỂN THỊ
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] columns = {"Tháng/Năm", "Danh mục", "Hạn mức ngân sách", "Đã chi tiêu", "Còn lại", "Tiến độ sử dụng"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblBudgets = new JTable(tableModel);
        tblBudgets.setFont(AppConstants.FONT_BODY);
        tblBudgets.setRowHeight(40);
        tblBudgets.setShowGrid(false);
        tblBudgets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom Header
        JTableHeader header = tblBudgets.getTableHeader();
        header.setFont(AppConstants.FONT_SUBTITLE);
        header.setBackground(AppConstants.COLOR_BG_LIGHT);
        header.setForeground(AppConstants.COLOR_TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 40));

        // Cài đặt Renderers căn chỉnh
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        tblBudgets.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        tblBudgets.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        tblBudgets.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tblBudgets.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        // Custom Renderer cột Còn Lại (Nếu bị âm -> Chữ đỏ đậm cảnh báo)
        tblBudgets.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                BigDecimal val = (BigDecimal) value;
                if (val != null) {
                    setText(CurrencyFormatter.format(val));
                    if (val.compareTo(BigDecimal.ZERO) < 0) {
                        setForeground(AppConstants.COLOR_DANGER);
                        setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 14));
                    } else {
                        setForeground(AppConstants.COLOR_TEXT_DARK);
                        setFont(AppConstants.FONT_BODY);
                    }
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        // Custom TableCellRenderer cột Tiến Độ hiển thị thanh JProgressBar bo góc
        tblBudgets.getColumnModel().getColumn(5).setCellRenderer(new ProgressCellRenderer());

        JScrollPane scrollPane = new JScrollPane(tblBudgets);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // --- SỰ KIỆN NÚT BẤM ---

        // Thêm ngân sách
        btnAdd.addActionListener(e -> {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(BudgetListPanel.this);
            BudgetFormDialog dialog = new BudgetFormDialog(parentFrame, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshData();
            }
        });

        // Chỉnh sửa ngân sách
        btnEdit.addActionListener(e -> {
            int selectedRow = tblBudgets.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(BudgetListPanel.this,
                    "Vui lòng chọn ngân sách muốn chỉnh sửa từ danh sách.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Budget selectedBudget = budgetList.get(selectedRow);
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(BudgetListPanel.this);
            BudgetFormDialog dialog = new BudgetFormDialog(parentFrame, selectedBudget);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshData();
            }
        });

        // Xóa ngân sách
        btnDelete.addActionListener(e -> {
            int selectedRow = tblBudgets.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(BudgetListPanel.this,
                    "Vui lòng chọn ngân sách muốn xóa từ danh sách.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Budget selectedBudget = budgetList.get(selectedRow);
            int confirm = JOptionPane.showConfirmDialog(BudgetListPanel.this,
                "Bạn có chắc chắn muốn xóa hạn mức ngân sách của \"" + selectedBudget.getCategoryName() + "\" không?",
                "Xác nhận xóa ngân sách",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                String errorMsg = budgetController.deleteBudget(selectedBudget.getBudgetId());
                if (errorMsg != null) {
                    JOptionPane.showMessageDialog(BudgetListPanel.this, errorMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(BudgetListPanel.this, "Đã xóa ngân sách thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            }
        });
    }

    /**
     * Tải lại dữ liệu từ DB.
     */
    public void refreshData() {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        int userId = SessionManager.getInstance().getCurrentUser().getUserId();

        budgetList = budgetController.getBudgetsByUserId(userId);

        tableModel.setRowCount(0);
        for (Budget b : budgetList) {
            String timeText = "Tháng " + b.getMonth() + "/" + b.getYear();
            tableModel.addRow(new Object[]{
                timeText,
                b.getCategoryName(),
                CurrencyFormatter.format(b.getBudgetAmount()),
                CurrencyFormatter.format(b.getSpentAmount()),
                b.getRemainingAmount(), // Renderer tự format
                b.getUsagePercent() // Đối tượng kiểu Double để vẽ progress bar
            });
        }
    }

    /**
     * Renderer vẽ thanh JProgressBar cho cột Tiến Độ.
     */
    private static class ProgressCellRenderer extends JProgressBar implements TableCellRenderer {
        public ProgressCellRenderer() {
            super(0, 100);
            setStringPainted(true); // Vẽ số phần trăm %
            setBorderPainted(false);
            setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            double percentDouble = 0;
            if (value instanceof Number) {
                percentDouble = ((Number) value).doubleValue();
            }

            int percent = (int) percentDouble;
            setValue(Math.min(percent, 100)); // Hạn chế tối đa vẽ thanh đầy 100%
            setString(percent + "%");

            // Chọn màu dựa trên tỷ lệ % đã chi tiêu trên ngân sách
            if (percent < 80) {
                setForeground(AppConstants.COLOR_SUCCESS); // Xanh lục tươi mát (<80%)
            } else if (percent <= 100) {
                setForeground(AppConstants.COLOR_WARNING); // Màu cam cảnh báo (80%-100%)
            } else {
                setForeground(AppConstants.COLOR_DANGER); // Màu đỏ vượt hạn mức (>100%)
            }

            // Màu nền xám nhạt cho thanh tiến độ
            setBackground(new Color(236, 240, 241));

            return this;
        }
    }
}
