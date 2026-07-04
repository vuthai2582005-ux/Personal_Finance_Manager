package personalfinancemanager.view.report;

import personalfinancemanager.config.AppConstants;
import personalfinancemanager.controller.ReportController;
import personalfinancemanager.dao.ReportDAO;
import personalfinancemanager.util.CurrencyFormatter;
import personalfinancemanager.util.SessionManager;
import personalfinancemanager.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Giao diện Báo cáo & Thống kê sử dụng bảng số liệu JTable thuần (Tiếng Việt).
 */
public class ReportPanel extends JPanel {
    private final ReportController reportController;

    private JComboBox<Integer> cbMonth;
    private JSpinner spinYear;
    private JComboBox<String> cbType;
    private RoundedButton btnGenerate;

    private JTabbedPane tabbedPane;
    private JTable tblCategoryReport;
    private DefaultTableModel modelCategoryReport;

    private JTable tblTrendReport;
    private DefaultTableModel modelTrendReport;

    public ReportPanel() {
        this.reportController = new ReportController();
        initComponents();
        refreshData();
    }

    private void initComponents() {
        setBackground(AppConstants.COLOR_BG_LIGHT);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // 1. TIÊU ĐỀ TRANG
        JLabel lblTitle = new JLabel("Báo Cáo Thống Kê");
        lblTitle.setFont(AppConstants.FONT_TITLE);
        lblTitle.setForeground(AppConstants.COLOR_PRIMARY);
        add(lblTitle, BorderLayout.NORTH);

        // 2. PANEL TRUNG TÂM CHỨA BỘ LỌC VÀ BẢNG TAB
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);

        // A. Thanh bộ lọc thống kê (Filters)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        filterPanel.add(new JLabel("Tháng báo cáo:"));
        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        cbMonth = new JComboBox<>(months);
        cbMonth.setFont(AppConstants.FONT_SMALL);
        cbMonth.setSelectedItem(LocalDate.now().getMonthValue());
        cbMonth.setPreferredSize(new Dimension(80, 30));
        filterPanel.add(cbMonth);

        filterPanel.add(new JLabel("Năm:"));
        int currentYear = LocalDate.now().getYear();
        SpinnerNumberModel yearModel = new SpinnerNumberModel(currentYear, currentYear - 5, currentYear + 10, 1);
        spinYear = new JSpinner(yearModel);
        spinYear.setFont(AppConstants.FONT_SMALL);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinYear, "#");
        spinYear.setEditor(editor);
        spinYear.setPreferredSize(new Dimension(80, 30));
        filterPanel.add(spinYear);

        filterPanel.add(new JLabel("Phân loại cơ cấu:"));
        String[] types = {"Khoản chi (EXPENSE)", "Khoản thu (INCOME)"};
        cbType = new JComboBox<>(types);
        cbType.setFont(AppConstants.FONT_SMALL);
        cbType.setPreferredSize(new Dimension(170, 30));
        filterPanel.add(cbType);

        btnGenerate = new RoundedButton("Xuất Báo Cáo", AppConstants.COLOR_PRIMARY);
        btnGenerate.setPreferredSize(new Dimension(130, 30));
        filterPanel.add(btnGenerate);

        centerPanel.add(filterPanel, BorderLayout.NORTH);

        // B. Vùng hiển thị Tabs Báo Cáo
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(AppConstants.FONT_SUBTITLE);

        // Tab 1: Cơ cấu theo danh mục
        JPanel pnlCategory = new JPanel(new BorderLayout());
        pnlCategory.setBackground(Color.WHITE);
        pnlCategory.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] colsCategory = {"Tên danh mục", "Tổng số tiền", "Tỷ lệ phần trăm (%)"};
        modelCategoryReport = new DefaultTableModel(colsCategory, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblCategoryReport = new JTable(modelCategoryReport);
        tblCategoryReport.setFont(AppConstants.FONT_BODY);
        tblCategoryReport.setRowHeight(38);
        tblCategoryReport.setShowGrid(false);

        // Custom Table Header
        JTableHeader headerCat = tblCategoryReport.getTableHeader();
        headerCat.setFont(AppConstants.FONT_SUBTITLE);
        headerCat.setBackground(AppConstants.COLOR_BG_LIGHT);
        headerCat.setPreferredSize(new Dimension(0, 38));

        // Căn chỉnh cột số liệu
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        tblCategoryReport.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        tblCategoryReport.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        tblCategoryReport.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        JScrollPane scrollCat = new JScrollPane(tblCategoryReport);
        scrollCat.setBorder(BorderFactory.createEmptyBorder());
        pnlCategory.add(scrollCat, BorderLayout.CENTER);

        tabbedPane.addTab("Cơ Cấu Theo Danh Mục", pnlCategory);

        // Tab 2: Xu hướng hàng tháng
        JPanel pnlTrend = new JPanel(new BorderLayout());
        pnlTrend.setBackground(Color.WHITE);
        pnlTrend.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] colsTrend = {"Tháng", "Tổng Thu Nhập", "Tổng Chi Tiêu", "Tiết Kiệm Tích Lũy", "Tỷ Lệ Tiết Kiệm (%)"};
        modelTrendReport = new DefaultTableModel(colsTrend, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblTrendReport = new JTable(modelTrendReport);
        tblTrendReport.setFont(AppConstants.FONT_BODY);
        tblTrendReport.setRowHeight(38);
        tblTrendReport.setShowGrid(false);

        // Custom Header
        JTableHeader headerTrend = tblTrendReport.getTableHeader();
        headerTrend.setFont(AppConstants.FONT_SUBTITLE);
        headerTrend.setBackground(AppConstants.COLOR_BG_LIGHT);
        headerTrend.setPreferredSize(new Dimension(0, 38));

        tblTrendReport.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        tblTrendReport.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        tblTrendReport.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        
        // Custom Renderer cột Tiết Kiệm Tích Lũy (Nếu âm -> chữ đỏ đậm)
        tblTrendReport.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
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
                        setForeground(AppConstants.COLOR_SUCCESS);
                        setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 14));
                    }
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        // Custom Renderer cột Tỷ Lệ Tiết Kiệm (Nếu âm -> chữ đỏ đậm)
        tblTrendReport.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Double val = (Double) value;
                if (val != null) {
                    setText(String.format("%.1f %%", val));
                    if (val < 0) {
                        setForeground(AppConstants.COLOR_DANGER);
                        setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 14));
                    } else {
                        setForeground(AppConstants.COLOR_SUCCESS);
                        setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 14));
                    }
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        JScrollPane scrollTrend = new JScrollPane(tblTrendReport);
        scrollTrend.setBorder(BorderFactory.createEmptyBorder());
        pnlTrend.add(scrollTrend, BorderLayout.CENTER);

        tabbedPane.addTab("Xu Hướng Thu Chi Hàng Tháng", pnlTrend);

        centerPanel.add(tabbedPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // --- SỰ KIỆN ---
        btnGenerate.addActionListener(e -> refreshData());
    }

    /**
     * Làm mới dữ liệu báo cáo từ DB theo cấu hình bộ lọc hiện tại.
     */
    public void refreshData() {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        int userId = SessionManager.getInstance().getCurrentUser().getUserId();

        int month = (Integer) cbMonth.getSelectedItem();
        int year = (Integer) spinYear.getValue();
        String selectedType = (String) cbType.getSelectedItem();
        String type = selectedType.contains("INCOME") ? "INCOME" : "EXPENSE";

        // 1. Tải dữ liệu Tab 1: Báo cáo theo Danh mục
        modelCategoryReport.setRowCount(0);
        List<ReportDAO.CategoryReportEntry> catEntries = reportController.getCategoryReport(userId, type, month, year);
        BigDecimal sumAmount = reportController.calculateTotalCategoryAmount(catEntries);

        for (ReportDAO.CategoryReportEntry entry : catEntries) {
            double percent = 0.0;
            if (sumAmount.compareTo(BigDecimal.ZERO) > 0) {
                percent = entry.getTotalAmount()
                               .multiply(new BigDecimal(100))
                               .divide(sumAmount, 2, RoundingMode.HALF_UP)
                               .doubleValue();
            }
            
            modelCategoryReport.addRow(new Object[]{
                entry.getCategoryName(),
                CurrencyFormatter.format(entry.getTotalAmount()),
                String.format("%.2f %%", percent)
            });
        }

        // 2. Tải dữ liệu Tab 2: Xu hướng hàng tháng
        modelTrendReport.setRowCount(0);
        List<ReportDAO.MonthlyReportTrendEntry> trendEntries = reportController.getMonthlyReportTrend(userId, year);

        for (ReportDAO.MonthlyReportTrendEntry entry : trendEntries) {
            BigDecimal savings = entry.getTotalIncome().subtract(entry.getTotalExpense());
            double savingsRate = 0.0;
            if (entry.getTotalIncome().compareTo(BigDecimal.ZERO) > 0) {
                savingsRate = savings
                               .multiply(new BigDecimal(100))
                               .divide(entry.getTotalIncome(), 1, RoundingMode.HALF_UP)
                               .doubleValue();
            } else if (entry.getTotalExpense().compareTo(BigDecimal.ZERO) > 0) {
                // Nếu không có thu nhập mà chỉ có chi tiêu, tỷ lệ tiết kiệm âm 100%
                savingsRate = -100.0;
            }

            modelTrendReport.addRow(new Object[]{
                "Tháng " + entry.getMonth(),
                CurrencyFormatter.format(entry.getTotalIncome()),
                CurrencyFormatter.format(entry.getTotalExpense()),
                savings, // Renderer tự định dạng và tô màu
                savingsRate // Renderer tự định dạng và tô màu
            });
        }
    }
}
