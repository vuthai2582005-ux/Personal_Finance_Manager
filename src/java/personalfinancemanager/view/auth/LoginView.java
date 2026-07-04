package personalfinancemanager.view.auth;

import personalfinancemanager.config.AppConstants;
import personalfinancemanager.controller.AuthController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Giao diện Đăng nhập (Swing - Tiếng Việt).
 */
public class LoginView extends JFrame {
    private final AuthController authController;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblRegisterLink;
    private JLabel lblError;

    public LoginView() {
        this.authController = new AuthController();
        initComponents();
    }

    private void initComponents() {
        setTitle("Quản Lý Tài Chính Cá Nhân - Đăng Nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel chính sử dụng BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        // 1. PANEL BÊN TRÁI: Quảng bá, giới thiệu ứng dụng
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(AppConstants.COLOR_PRIMARY);
        leftPanel.setPreferredSize(new Dimension(350, 500));
        leftPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        gbcLeft.insets = new Insets(10, 20, 10, 20);
        gbcLeft.anchor = GridBagConstraints.CENTER;

        JLabel lblLogo = new JLabel("PFM");
        lblLogo.setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 72));
        lblLogo.setForeground(new Color(241, 196, 15)); // Màu vàng nổi bật
        leftPanel.add(lblLogo, gbcLeft);

        gbcLeft.gridy = 1;
        JLabel lblAppName = new JLabel("TÀI CHÍNH CÁ NHÂN");
        lblAppName.setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 24));
        lblAppName.setForeground(Color.WHITE);
        leftPanel.add(lblAppName, gbcLeft);

        gbcLeft.gridy = 2;
        JLabel lblAppDesc = new JLabel("<html><center>Giải pháp quản lý chi tiêu<br>thông minh và hiệu quả của bạn</center></html>");
        lblAppDesc.setFont(AppConstants.FONT_BODY);
        lblAppDesc.setForeground(new Color(200, 214, 229));
        leftPanel.add(lblAppDesc, gbcLeft);

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // 2. PANEL BÊN PHẢI: Form Đăng Nhập
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(40, 50, 40, 50));
        rightPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.fill = GridBagConstraints.HORIZONTAL;
        gbcRight.insets = new Insets(10, 0, 10, 0);
        gbcRight.gridx = 0;

        // Tiêu đề Form
        gbcRight.gridy = 0;
        JLabel lblTitle = new JLabel("Đăng Nhập");
        lblTitle.setFont(AppConstants.FONT_TITLE);
        lblTitle.setForeground(AppConstants.COLOR_PRIMARY);
        rightPanel.add(lblTitle, gbcRight);

        // Nhãn phụ
        gbcRight.gridy = 1;
        JLabel lblSubtitle = new JLabel("Vui lòng đăng nhập để tiếp tục");
        lblSubtitle.setFont(AppConstants.FONT_SMALL);
        lblSubtitle.setForeground(AppConstants.COLOR_TEXT_LIGHT);
        rightPanel.add(lblSubtitle, gbcRight);

        // Nhãn Username
        gbcRight.gridy = 2;
        gbcRight.insets = new Insets(15, 0, 5, 0);
        JLabel lblUsername = new JLabel("Tên đăng nhập:");
        lblUsername.setFont(AppConstants.FONT_BODY);
        lblUsername.setForeground(AppConstants.COLOR_TEXT_DARK);
        rightPanel.add(lblUsername, gbcRight);

        // Trường nhập Username
        gbcRight.gridy = 3;
        gbcRight.insets = new Insets(0, 0, 10, 0);
        txtUsername = new JTextField();
        txtUsername.setFont(AppConstants.FONT_BODY);
        txtUsername.setPreferredSize(new Dimension(300, 35));
        rightPanel.add(txtUsername, gbcRight);

        // Nhãn Password
        gbcRight.gridy = 4;
        gbcRight.insets = new Insets(10, 0, 5, 0);
        JLabel lblPassword = new JLabel("Mật khẩu:");
        lblPassword.setFont(AppConstants.FONT_BODY);
        lblPassword.setForeground(AppConstants.COLOR_TEXT_DARK);
        rightPanel.add(lblPassword, gbcRight);

        // Trường nhập Password
        gbcRight.gridy = 5;
        gbcRight.insets = new Insets(0, 0, 10, 0);
        txtPassword = new JPasswordField();
        txtPassword.setFont(AppConstants.FONT_BODY);
        txtPassword.setPreferredSize(new Dimension(300, 35));
        rightPanel.add(txtPassword, gbcRight);

        // Nhãn hiển thị lỗi nếu có
        gbcRight.gridy = 6;
        lblError = new JLabel(" ");
        lblError.setFont(AppConstants.FONT_SMALL);
        lblError.setForeground(AppConstants.COLOR_DANGER);
        rightPanel.add(lblError, gbcRight);

        // Nút Đăng nhập
        gbcRight.gridy = 7;
        gbcRight.insets = new Insets(15, 0, 15, 0);
        btnLogin = new JButton("Đăng Nhập");
        btnLogin.setFont(AppConstants.FONT_SUBTITLE);
        btnLogin.setBackground(AppConstants.COLOR_ACCENT);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(300, 40));
        btnLogin.setFocusPainted(false);
        rightPanel.add(btnLogin, gbcRight);

        // Liên kết đăng ký
        gbcRight.gridy = 8;
        gbcRight.insets = new Insets(0, 0, 0, 0);
        lblRegisterLink = new JLabel("Chưa có tài khoản? Đăng ký ngay");
        lblRegisterLink.setFont(AppConstants.FONT_BODY);
        lblRegisterLink.setForeground(AppConstants.COLOR_ACCENT);
        lblRegisterLink.setHorizontalAlignment(SwingConstants.CENTER);
        lblRegisterLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightPanel.add(lblRegisterLink, gbcRight);

        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // --- XỬ LÝ SỰ KIỆN ---

        // Bấm nút đăng nhập
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        // Bấm phím Enter ở password field
        txtPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        // Click liên kết Đăng ký
        lblRegisterLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Chuyển sang màn hình Đăng ký
                dispose();
                new RegisterView().setVisible(true);
            }
        });
    }

    private void handleLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        lblError.setText(" "); // Reset lỗi

        // Gọi controller xử lý
        String errorMsg = authController.login(username, password);
        if (errorMsg != null) {
            lblError.setText(errorMsg);
        } else {
            // Đăng nhập thành công -> Chuyển sang MainFrame (JFrame chính)
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
            
            // Mở MainFrame chính của chương trình
            // Tạm thời mở một dialog placeholder cho Phase này nếu MainFrame chưa sẵn sàng
            try {
                Class<?> mainFrameClass = Class.forName("personalfinancemanager.view.main.MainFrame");
                JFrame mainFrame = (JFrame) mainFrameClass.getDeclaredConstructor().newInstance();
                mainFrame.setVisible(true);
            } catch (Exception ex) {
                // Nếu chưa code MainFrame thì hiển thị thông báo
                JOptionPane.showMessageDialog(null, 
                    "Hệ thống đã xác thực thành công. Giao diện chính (MainFrame) sẽ được hoàn thiện ở Phase sau.", 
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
