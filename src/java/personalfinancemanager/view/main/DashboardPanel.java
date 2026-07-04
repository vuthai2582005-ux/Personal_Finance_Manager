package personalfinancemanager.view.main;

import personalfinancemanager.config.AppConstants;
import personalfinancemanager.dao.AccountDAO;
import personalfinancemanager.dao.TransactionDAO;
import personalfinancemanager.model.Transaction;
import personalfinancemanager.model.User;
import personalfinancemanager.util.CurrencyFormatter;
import personalfinancemanager.util.DateUtil;
import personalfinancemanager.util.SessionManager;
import personalfinancemanager.view.components.CardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Giao diện trang Tổng quan (Dashboard) hiển thị tóm tắt thu chi và giao dịch gần đây.
 */
public class DashboardPanel extends JPanel {
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    private CardPanel cardIncome;
    private CardPanel cardExpense;
    private CardPanel cardBalance;

    private JTable tblRecentTransactions;
    private DefaultTableModel tableModel;

    public DashboardPanel() {
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();

        initComponents();
        refreshData();
    }

    private void initComponents() {
        setBackground(AppConstants.COLOR_BG_LIGHT);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // 1. PHẦN TIÊU ĐỀ TRANG
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Tổng Quan");
        lblTitle.setFont(AppConstants.FONT_TITLE);
        lblTitle.setForeground(AppConstants.COLOR_PRIMARY);
        titlePanel.add(lblTitle, BorderLayout.WEST);

        JLabel lblMonth = new JLabel("Tháng " + LocalDate.now().getMonthValue() + "/" + LocalDate.now().getYear());
        lblMonth.setFont(AppConstants.FONT_SUBTITLE);
        lblMonth.setForeground(AppConstants.COLOR_TEXT_LIGHT);
        titlePanel.add(lblMonth, BorderLayout.EAST);

        add(titlePanel, BorderLayout.NORTH);

        // 2. PHẦN GIỮA: Thẻ tóm tắt số liệu & Danh sách giao dịch
        JPanel centerPanel = new JPanel(new BorderLayout(20, 25));
        centerPanel.setOpaque(false);

        // Hàng trên: 3 Thẻ thống kê số liệu
        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsGrid.setOpaque(false);

        // Thẻ Số Dư (Xanh đen đậm)
        cardBalance = new CardPanel(
            "SỐ DƯ HIỆN CÓ",
            "0 ₫",
            "💳",
            AppConstants.COLOR_PRIMARY,
            Color.WHITE
        );
        cardsGrid.add(cardBalance);

        // Thẻ Thu Nhập (Xanh lục thành công)
        cardIncome = new CardPanel(
            "TỔNG THU NHẬP THÁNG NÀY",
            "0 ₫",
            "📈",
            AppConstants.COLOR_SUCCESS,
            Color.WHITE
        );
        cardsGrid.add(cardIncome);

        // Thẻ Chi Tiêu (Đỏ nguy hiểm)
        cardExpense = new CardPanel(
            "TỔNG CHI TIÊU THÁNG NÀY",
            "0 ₫",
            "📉",
            AppConstants.COLOR_DANGER,
            Color.WHITE
        );
        cardsGrid.add(cardExpense);

        centerPanel.add(cardsGrid, BorderLayout.NORTH);

        // Vùng dưới: Bảng Giao dịch gần đây
        JPanel tableContainer = new JPanel(new BorderLayout(10, 10));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTableTitle = new JLabel("Giao Dịch Gần Đây");
        lblTableTitle.setFont(AppConstants.FONT_SUBTITLE);
        lblTableTitle.setForeground(AppConstants.COLOR_PRIMARY);
        tableContainer.add(lblTableTitle, BorderLayout.NORTH);

        // Khởi tạo bảng dữ liệu
        String[] columns = {"Ngày", "Loại", "Danh mục", "Số tiền", "Tài khoản", "Ghi chú"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép sửa trực tiếp trên bảng
            }
        };

        tblRecentTransactions = new JTable(tableModel);
        tblRecentTransactions.setFont(AppConstants.FONT_BODY);
        tblRecentTransactions.setRowHeight(35);
        tblRecentTransactions.setShowGrid(false);
        tblRecentTransactions.setIntercellSpacing(new Dimension(0, 0));
        tblRecentTransactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Tùy chỉnh Header của bảng
        JTableHeader header = tblRecentTransactions.getTableHeader();
        header.setFont(AppConstants.FONT_SUBTITLE);
        header.setBackground(AppConstants.COLOR_BG_LIGHT);
        header.setForeground(AppConstants.COLOR_TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 40));

        // Renderer để căn chỉnh và tô màu cột
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        
        // Custom renderer riêng cho cột Số tiền và Loại giao dịch
        tblRecentTransactions.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
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
        for (int i = 0; i < tblRecentTransactions.getColumnCount(); i++) {
            if (i != 3) {
                tblRecentTransactions.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
            }
        }

        JScrollPane scrollPane = new JScrollPane(tblRecentTransactions);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tableContainer, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Làm mới số liệu trên các thẻ và tải danh sách giao dịch mới nhất từ DB.
     */
    public void refreshData() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        int userId = currentUser.getUserId();
        LocalDate now = LocalDate.now();

        // 1. Cập nhật thẻ số dư tài khoản
        BigDecimal totalBalance = accountDAO.getTotalBalanceByUserId(userId);
        cardBalance.setValue(CurrencyFormatter.format(totalBalance));

        // 2. Cập nhật thẻ tổng thu/chi trong tháng
        Map<String, BigDecimal> summary = transactionDAO.getMonthlySummary(userId, now.getMonthValue(), now.getYear());
        BigDecimal totalIncome = summary.getOrDefault("INCOME", BigDecimal.ZERO);
        BigDecimal totalExpense = summary.getOrDefault("EXPENSE", BigDecimal.ZERO);

        cardIncome.setValue(CurrencyFormatter.format(totalIncome));
        cardExpense.setValue(CurrencyFormatter.format(totalExpense));

        // 3. Tải danh sách 5 giao dịch gần đây vào bảng
        tableModel.setRowCount(0); // Xóa dòng cũ
        List<Transaction> recentList = transactionDAO.getRecentTransactions(userId, 5);

        for (Transaction t : recentList) {
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
