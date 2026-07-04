package personalfinancemanager.view.transaction;

import personalfinancemanager.config.AppConstants;
import personalfinancemanager.controller.TransactionController;
import personalfinancemanager.dao.AccountDAO;
import personalfinancemanager.dao.CategoryDAO;
import personalfinancemanager.model.Account;
import personalfinancemanager.model.Category;
import personalfinancemanager.model.Transaction;
import personalfinancemanager.util.CurrencyFormatter;
import personalfinancemanager.util.DateUtil;
import personalfinancemanager.util.SessionManager;
import personalfinancemanager.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Date;
import java.text.ParseException;
import java.util.List;

/**
 * Giao diện quản lý danh sách giao dịch có bộ lọc nâng cao (CRUD Transactions - Tiếng Việt).
 */
public class TransactionListPanel extends JPanel {
    private final TransactionController transactionController;
    private final AccountDAO accountDAO;
    private final CategoryDAO categoryDAO;
    private List<Transaction> transactionList;

    private JTable tblTransactions;
    private DefaultTableModel tableModel;

    // Các trường lọc dữ liệu
    private JComboBox<Object> cbFilterAccount;
    private JComboBox<Object> cbFilterCategory;
    private JComboBox<String> cbFilterType;
    private JTextField txtFromDate;
    private JTextField txtToDate;

    private RoundedButton btnApplyFilter;
    private RoundedButton btnResetFilter;

    private RoundedButton btnAdd;
    private RoundedButton btnEdit;
    private RoundedButton btnDelete;

    public TransactionListPanel() {
        this.transactionController = new TransactionController();
        this.accountDAO = new AccountDAO();
        this.categoryDAO = new CategoryDAO();

        initComponents();
        loadFilterDropdowns();
        refreshData();
    }

    private void initComponents() {
        setBackground(AppConstants.COLOR_BG_LIGHT);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // 1. TIÊU ĐỀ TRANG VÀ NÚT THAO TÁC NHANH
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Lịch Sử Giao Dịch");
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

        // 2. PANEL TRUNG TÂM CHỨA THANH BỘ LỌC VÀ BẢNG DỮ LIỆU
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);

        // A. Thanh bộ lọc dữ liệu (Filter Panel)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        filterPanel.add(new JLabel("Tài khoản:"));
        cbFilterAccount = new JComboBox<>();
        cbFilterAccount.setFont(AppConstants.FONT_SMALL);
        cbFilterAccount.setPreferredSize(new Dimension(140, 30));
        filterPanel.add(cbFilterAccount);

        filterPanel.add(new JLabel("Danh mục:"));
        cbFilterCategory = new JComboBox<>();
        cbFilterCategory.setFont(AppConstants.FONT_SMALL);
        cbFilterCategory.setPreferredSize(new Dimension(140, 30));
        filterPanel.add(cbFilterCategory);

        filterPanel.add(new JLabel("Loại:"));
        String[] types = {"Tất cả loại", "Chi tiêu (EXPENSE)", "Thu nhập (INCOME)"};
        cbFilterType = new JComboBox<>(types);
        cbFilterType.setFont(AppConstants.FONT_SMALL);
        cbFilterType.setPreferredSize(new Dimension(130, 30));
        filterPanel.add(cbFilterType);

        filterPanel.add(new JLabel("Từ ngày:"));
        txtFromDate = new JTextField();
        txtFromDate.setFont(AppConstants.FONT_SMALL);
        txtFromDate.setPreferredSize(new Dimension(85, 30));
        txtFromDate.setToolTipText("Định dạng dd/MM/yyyy");
        filterPanel.add(txtFromDate);

        filterPanel.add(new JLabel("Đến ngày:"));
        txtToDate = new JTextField();
        txtToDate.setFont(AppConstants.FONT_SMALL);
        txtToDate.setPreferredSize(new Dimension(85, 30));
        txtToDate.setToolTipText("Định dạng dd/MM/yyyy");
        filterPanel.add(txtToDate);

        btnApplyFilter = new RoundedButton("Lọc", AppConstants.COLOR_PRIMARY);
        btnApplyFilter.setPreferredSize(new Dimension(70, 30));
        filterPanel.add(btnApplyFilter);

        btnResetFilter = new RoundedButton("Đặt Lại", new Color(189, 195, 199));
        btnResetFilter.setForeground(Color.DARK_GRAY);
        btnResetFilter.setPreferredSize(new Dimension(80, 30));
        filterPanel.add(btnResetFilter);

        centerPanel.add(filterPanel, BorderLayout.NORTH);

        // B. Bảng hiển thị giao dịch
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new EmptyBorder(10, 15, 15, 15));

        String[] columns = {"Ngày", "Loại", "Danh mục", "Số tiền", "Tài khoản", "Ghi chú"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblTransactions = new JTable(tableModel);
        tblTransactions.setFont(AppConstants.FONT_BODY);
        tblTransactions.setRowHeight(38);
        tblTransactions.setShowGrid(false);
        tblTransactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom Header
        JTableHeader header = tblTransactions.getTableHeader();
        header.setFont(AppConstants.FONT_SUBTITLE);
        header.setBackground(AppConstants.COLOR_BG_LIGHT);
        header.setForeground(AppConstants.COLOR_TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 38));

        // Custom Renderer cột số tiền (Thu nhập màu xanh, Chi tiêu màu đỏ)
        tblTransactions.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String type = (String) table.getValueAt(row, 1);
                if ("Thu nhập".equals(type)) {
                    c.setForeground(AppConstants.COLOR_SUCCESS);
                    setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 14));
                } else {
                    c.setForeground(AppConstants.COLOR_DANGER);
                    setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 14));
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        // Thiết lập Renderer mặc định cho các cột còn lại
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        for (int i = 0; i < tblTransactions.getColumnCount(); i++) {
            if (i != 3) {
                tblTransactions.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
            }
        }

        JScrollPane scrollPane = new JScrollPane(tblTransactions);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tableContainer, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // --- HÀNH ĐỘNG SỰ KIỆN ---

        // Thêm giao dịch
        btnAdd.addActionListener(e -> {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(TransactionListPanel.this);
            TransactionFormDialog dialog = new TransactionFormDialog(parentFrame, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshData();
            }
        });

        // Chỉnh sửa giao dịch
        btnEdit.addActionListener(e -> {
            int selectedRow = tblTransactions.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(TransactionListPanel.this,
                    "Vui lòng chọn giao dịch muốn chỉnh sửa từ danh sách.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Transaction selectedTrans = transactionList.get(selectedRow);
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(TransactionListPanel.this);
            TransactionFormDialog dialog = new TransactionFormDialog(parentFrame, selectedTrans);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshData();
            }
        });

        // Xóa giao dịch
        btnDelete.addActionListener(e -> {
            int selectedRow = tblTransactions.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(TransactionListPanel.this,
                    "Vui lòng chọn giao dịch muốn xóa từ danh sách.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Transaction selectedTrans = transactionList.get(selectedRow);
            int confirm = JOptionPane.showConfirmDialog(TransactionListPanel.this,
                "Bạn có chắc chắn muốn xóa giao dịch này không?\n" +
                "Lưu ý: Thao tác này sẽ tự động khôi phục lại số dư tài khoản và ngân sách liên quan.",
                "Xác nhận xóa giao dịch",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                String errorMsg = transactionController.deleteTransaction(selectedTrans.getTransactionId());
                if (errorMsg != null) {
                    JOptionPane.showMessageDialog(TransactionListPanel.this, errorMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(TransactionListPanel.this, "Đã xóa giao dịch thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            }
        });

        // Nút lọc
        btnApplyFilter.addActionListener(e -> refreshData());

        // Nút reset bộ lọc
        btnResetFilter.addActionListener(e -> {
            cbFilterAccount.setSelectedIndex(0);
            cbFilterCategory.setSelectedIndex(0);
            cbFilterType.setSelectedIndex(0);
            txtFromDate.setText("");
            txtToDate.setText("");
            refreshData();
        });
    }

    /**
     * Nạp dữ liệu các Combobox phục vụ lọc.
     */
    private void loadFilterDropdowns() {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        int userId = SessionManager.getInstance().getCurrentUser().getUserId();

        // 1. Nạp Accounts
        cbFilterAccount.removeAllItems();
        cbFilterAccount.addItem("Tất cả tài khoản");
        List<Account> accounts = accountDAO.findByUserId(userId);
        for (Account a : accounts) {
            cbFilterAccount.addItem(a);
        }

        // 2. Nạp Categories
        cbFilterCategory.removeAllItems();
        cbFilterCategory.addItem("Tất cả danh mục");
        List<Category> categories = categoryDAO.findByUserId(userId);
        for (Category c : categories) {
            cbFilterCategory.addItem(c);
        }
    }

    /**
     * Tải dữ liệu giao dịch từ DB lên bảng có áp dụng bộ lọc hiện tại.
     */
    public void refreshData() {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        int userId = SessionManager.getInstance().getCurrentUser().getUserId();

        // Đọc các giá trị bộ lọc
        Integer accountId = null;
        Object selAcc = cbFilterAccount.getSelectedItem();
        if (selAcc instanceof Account) {
            accountId = ((Account) selAcc).getAccountId();
        }

        Integer categoryId = null;
        Object selCat = cbFilterCategory.getSelectedItem();
        if (selCat instanceof Category) {
            categoryId = ((Category) selCat).getCategoryId();
        }

        String typeStr = (String) cbFilterType.getSelectedItem();
        String type = "ALL";
        if (typeStr.contains("EXPENSE")) {
            type = "EXPENSE";
        } else if (typeStr.contains("INCOME")) {
            type = "INCOME";
        }

        Date fromDate = null;
        if (!txtFromDate.getText().trim().isEmpty()) {
            try {
                fromDate = DateUtil.parseDate(txtFromDate.getText().trim());
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Định dạng ngày lọc 'Từ ngày' không hợp lệ (dd/MM/yyyy).", "Lỗi lọc dữ liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Date toDate = null;
        if (!txtToDate.getText().trim().isEmpty()) {
            try {
                toDate = DateUtil.parseDate(txtToDate.getText().trim());
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Định dạng ngày lọc 'Đến ngày' không hợp lệ (dd/MM/yyyy).", "Lỗi lọc dữ liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Thực hiện truy vấn có bộ lọc
        transactionList = transactionController.getTransactionsByFilters(userId, accountId, categoryId, type, fromDate, toDate);

        // Nạp bảng
        tableModel.setRowCount(0);
        for (Transaction t : transactionList) {
            String typeText = "INCOME".equals(t.getType()) ? "Thu nhập" : "Chi tiêu";
            tableModel.addRow(new Object[]{
                DateUtil.formatDate(t.getTransactionDate()),
                typeText,
                t.getCategoryName(),
                CurrencyFormatter.format(t.getAmount()),
                t.getAccountName(),
                t.getDescription() != null ? t.getDescription() : ""
            });
        }
    }
}
