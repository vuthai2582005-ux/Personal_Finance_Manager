package personalfinancemanager.view.components;

import personalfinancemanager.config.AppConstants;
import personalfinancemanager.util.SessionManager;
import personalfinancemanager.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Thanh menu điều hướng bên trái (Sidebar).
 */
public class SidebarPanel extends JPanel {

    public interface SidebarListener {
        void onMenuSelected(String menuName);
    }

    private final List<SidebarListener> listeners = new ArrayList<>();
    private final List<JButton> menuButtons = new ArrayList<>();
    private JButton activeButton;

    public SidebarPanel() {
        setBackground(AppConstants.COLOR_PRIMARY);
        setPreferredSize(new Dimension(250, 750));
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 15, 30, 15));

        // 1. PHẦN TRÊN: Logo + Thông tin người dùng
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        // Logo
        JLabel lblLogo = new JLabel("PFM");
        lblLogo.setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 36));
        lblLogo.setForeground(new Color(241, 196, 15)); // Màu vàng kim
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(lblLogo);

        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Tên người dùng đang đăng nhập
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String name = currentUser != null ? currentUser.getFullName() : "Khách";
        JLabel lblUser = new JLabel(name);
        lblUser.setFont(AppConstants.FONT_SUBTITLE);
        lblUser.setForeground(Color.WHITE);
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(lblUser);

        // Nhãn phân chia dưới tên user
        JLabel lblRole = new JLabel("Thành viên");
        lblRole.setFont(AppConstants.FONT_SMALL);
        lblRole.setForeground(AppConstants.COLOR_TEXT_LIGHT);
        lblRole.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(lblRole);

        add(headerPanel, BorderLayout.NORTH);

        // 2. PHẦN GIỮA: Danh sách menu điều hướng
        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new GridLayout(8, 1, 0, 10)); // Grid cho các nút menu

        String[] menus = {
            "Tổng Quan",
            "Giao Dịch",
            "Tài Khoản",
            "Danh Mục",
            "Ngân Sách",
            "Báo Cáo"
        };

        for (String m : menus) {
            JButton btn = createMenuButton(m);
            menuPanel.add(btn);
            menuButtons.add(btn);
            
            // Đặt trang Tổng Quan làm mặc định active ban đầu
            if (m.equals("Tổng Quan")) {
                setActive(btn);
            }
        }

        add(menuPanel, BorderLayout.CENTER);

        // 3. PHẦN DƯỚI: Nút Đăng Xuất
        JButton btnLogout = createMenuButton("Đăng Xuất");
        // Đặt màu đặc trưng cho Đăng Xuất (đỏ nhạt)
        btnLogout.setForeground(new Color(231, 76, 60));
        btnLogout.setFont(AppConstants.FONT_SUBTITLE);

        add(btnLogout, BorderLayout.SOUTH);
    }

    public void addSidebarListener(SidebarListener listener) {
        listeners.add(listener);
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppConstants.FONT_BODY);
        btn.setForeground(new Color(189, 195, 199)); // Màu xám nhạt mặc định
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 45));

        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!text.equals("Đăng Xuất")) {
                    setActive(btn);
                }
                // Notify listeners
                for (SidebarListener l : listeners) {
                    l.onMenuSelected(text);
                }
            }
        });

        return btn;
    }

    /**
     * Đặt nút đang click làm Active (làm sáng chữ và tô đậm).
     */
    private void setActive(JButton btn) {
        if (activeButton != null) {
            activeButton.setForeground(new Color(189, 195, 199));
            activeButton.setFont(AppConstants.FONT_BODY);
        }
        activeButton = btn;
        activeButton.setForeground(Color.WHITE);
        activeButton.setFont(AppConstants.FONT_SUBTITLE);
    }
}
