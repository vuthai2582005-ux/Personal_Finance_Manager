package personalfinancemanager.view.main;

import personalfinancemanager.config.AppConstants;
import personalfinancemanager.util.SessionManager;
import personalfinancemanager.view.auth.LoginView;
import personalfinancemanager.view.account.AccountListPanel;
import personalfinancemanager.view.category.CategoryListPanel;
import personalfinancemanager.view.transaction.TransactionListPanel;
import personalfinancemanager.view.budget.BudgetListPanel;
import personalfinancemanager.view.report.ReportPanel;
import personalfinancemanager.view.components.SidebarPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Khung giao diện chính của ứng dụng sau khi đăng nhập thành công.
 */
public class MainFrame extends JFrame {
    private SidebarPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Các Panels chức năng chính
    private DashboardPanel dashboardPanel;
    private AccountListPanel accountPanel;
    private TransactionListPanel transactionPanel;
    private CategoryListPanel categoryPanel;
    private BudgetListPanel budgetPanel;
    private ReportPanel reportPanel;

    public MainFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Quản Lý Tài Chỉnh Cá Nhân");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(AppConstants.WINDOW_WIDTH, AppConstants.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 650));

        // Layout chính của MainFrame
        setLayout(new BorderLayout());

        // 1. Sidebar điều hướng (Bên trái)
        sidebarPanel = new SidebarPanel();
        add(sidebarPanel, BorderLayout.WEST);

        // 2. Vùng chứa nội dung chính (Bên phải) sử dụng CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(AppConstants.COLOR_BG_LIGHT);
        add(contentPanel, BorderLayout.CENTER);

        // Khởi tạo các trang nội dung
        initPages();

        // 3. Lắng nghe sự kiện chuyển trang từ Sidebar
        sidebarPanel.addSidebarListener(new SidebarPanel.SidebarListener() {
            @Override
            public void onMenuSelected(String menuName) {
                switch (menuName) {
                    case "Tổng Quan":
                        dashboardPanel.refreshData();
                        cardLayout.show(contentPanel, "Tổng Quan");
                        break;
                    case "Tài Khoản":
                        accountPanel.refreshData();
                        cardLayout.show(contentPanel, "Tài Khoản");
                        break;
                    case "Giao Dịch":
                        transactionPanel.refreshData();
                        cardLayout.show(contentPanel, "Giao Dịch");
                        break;
                    case "Danh Mục":
                        categoryPanel.refreshData();
                        cardLayout.show(contentPanel, "Danh Mục");
                        break;
                    case "Ngân Sách":
                        budgetPanel.refreshData();
                        cardLayout.show(contentPanel, "Ngân Sách");
                        break;
                    case "Báo Cáo":
                        reportPanel.refreshData();
                        cardLayout.show(contentPanel, "Báo Cáo");
                        break;
                    case "Đăng Xuất":
                        handleLogout();
                        break;
                }
            }
        });
    }

    private void initPages() {
        // A. Trang Tổng quan (Dashboard)
        dashboardPanel = new DashboardPanel();
        contentPanel.add(dashboardPanel, "Tổng Quan");

        // B. Trang Tài khoản
        accountPanel = new AccountListPanel();
        contentPanel.add(accountPanel, "Tài Khoản");

        // C. Trang Giao dịch
        transactionPanel = new TransactionListPanel();
        contentPanel.add(transactionPanel, "Giao Dịch");

        // D. Trang Danh mục
        categoryPanel = new CategoryListPanel();
        contentPanel.add(categoryPanel, "Danh Mục");

        // E. Trang Ngân sách
        budgetPanel = new BudgetListPanel();
        contentPanel.add(budgetPanel, "Ngân Sách");

        // F. Trang Báo cáo
        reportPanel = new ReportPanel();
        contentPanel.add(reportPanel, "Báo Cáo");
    }

    /**
     * Tạo panel tạm thời để hiển thị khi chức năng chưa được hoàn thiện.
     */
    private JPanel createPlaceholderPanel(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppConstants.COLOR_BG_LIGHT);
        JLabel label = new JLabel(text);
        label.setFont(AppConstants.FONT_TITLE);
        label.setForeground(AppConstants.COLOR_TEXT_LIGHT);
        panel.add(label);
        return panel;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn đăng xuất không?",
            "Xác nhận đăng xuất",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            this.dispose();
            // Quay lại màn hình đăng nhập
            new LoginView().setVisible(true);
        }
    }
}
