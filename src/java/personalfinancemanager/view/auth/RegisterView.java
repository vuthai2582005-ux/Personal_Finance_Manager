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
 * Giao diện Đăng ký tài khoản (Swing - Tiếng Việt).
 */
public class RegisterView extends JFrame {
    private final AuthController authController;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JButton btnRegister;
    private JLabel lblLoginLink;
    private JLabel lblError;

    public RegisterView() {
        this.authController = new AuthController();
        initComponents();
    }

    private void initComponents() {
        setTitle("Quản Lý Tài Chính Cá Nhân - Đăng Ký");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel chính sử dụng BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        // 1. PANEL BÊN TRÁI: Branding
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(AppConstants.COLOR_PRIMARY);
        leftPanel.setPreferredSize(new Dimension(350, 550));
        leftPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        gbcLeft.insets = new Insets(10, 20, 10, 20);
        gbcLeft.anchor = GridBagConstraints.CENTER;

        JLabel lblLogo = new JLabel("PFM");
        lblLogo.setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 72));
        lblLogo.setForeground(new Color(46, 204, 113)); // Màu xanh lục nổi bật
        leftPanel.add(lblLogo, gbcLeft);

        gbcLeft.gridy = 1;
        JLabel lblAppName = new JLabel("ĐĂNG KÝ THÀNH VIÊN");
        lblAppName.setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 22));
        lblAppName.setForeground(Color.WHITE);
        leftPanel.add(lblAppName, gbcLeft);

        gbcLeft.gridy = 2;
        JLabel lblAppDesc = new JLabel("<html><center>Chỉ mất chưa đầy 1 phút để tạo<br>tài khoản quản lý chi tiêu của riêng bạn</center></html>");
        lblAppDesc.setFont(AppConstants.FONT_BODY);
        lblAppDesc.setForeground(new Color(200, 214, 229));
        leftPanel.add(lblAppDesc, gbcLeft);

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // 2. PANEL BÊN PHẢI: Form Đăng Ký
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        rightPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.fill = GridBagConstraints.HORIZONTAL;
        gbcRight.insets = new Insets(6, 0, 6, 0);
        gbcRight.gridx = 0;

        // Tiêu đề Form
        gbcRight.gridy = 0;
        JLabel lblTitle = new JLabel("Đăng Ký Tài Khoản");
        lblTitle.setFont(AppConstants.FONT_TITLE);
        lblTitle.setForeground(AppConstants.COLOR_PRIMARY);
        rightPanel.add(lblTitle, gbcRight);

        // Tên đăng nhập
        gbcRight.gridy = 1;
        JLabel lblUsername = new JLabel("Tên đăng nhập (*):");
        lblUsername.setFont(AppConstants.FONT_SMALL);
        lblUsername.setForeground(AppConstants.COLOR_TEXT_DARK);
        rightPanel.add(lblUsername, gbcRight);

        gbcRight.gridy = 2;
        txtUsername = new JTextField();
        txtUsername.setFont(AppConstants.FONT_BODY);
        txtUsername.setPreferredSize(new Dimension(320, 30));
        rightPanel.add(txtUsername, gbcRight);

        // Họ và tên
        gbcRight.gridy = 3;
        JLabel lblFullName = new JLabel("Họ và tên (*):");
        lblFullName.setFont(AppConstants.FONT_SMALL);
        lblFullName.setForeground(AppConstants.COLOR_TEXT_DARK);
        rightPanel.add(lblFullName, gbcRight);

        gbcRight.gridy = 4;
        txtFullName = new JTextField();
        txtFullName.setFont(AppConstants.FONT_BODY);
        txtFullName.setPreferredSize(new Dimension(320, 30));
        rightPanel.add(txtFullName, gbcRight);

        // Mật khẩu
        gbcRight.gridy = 5;
        JLabel lblPassword = new JLabel("Mật khẩu (*):");
        lblPassword.setFont(AppConstants.FONT_SMALL);
        lblPassword.setForeground(AppConstants.COLOR_TEXT_DARK);
        rightPanel.add(lblPassword, gbcRight);

        gbcRight.gridy = 6;
        txtPassword = new JPasswordField();
        txtPassword.setFont(AppConstants.FONT_BODY);
        txtPassword.setPreferredSize(new Dimension(320, 30));
        rightPanel.add(txtPassword, gbcRight);

        // Xác nhận mật khẩu
        gbcRight.gridy = 7;
        JLabel lblConfirmPassword = new JLabel("Xác nhận mật khẩu (*):");
        lblConfirmPassword.setFont(AppConstants.FONT_SMALL);
        lblConfirmPassword.setForeground(AppConstants.COLOR_TEXT_DARK);
        rightPanel.add(lblConfirmPassword, gbcRight);

        gbcRight.gridy = 8;
        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setFont(AppConstants.FONT_BODY);
        txtConfirmPassword.setPreferredSize(new Dimension(320, 30));
        rightPanel.add(txtConfirmPassword, gbcRight);

        // Email
        gbcRight.gridy = 9;
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(AppConstants.FONT_SMALL);
        lblEmail.setForeground(AppConstants.COLOR_TEXT_DARK);
        rightPanel.add(lblEmail, gbcRight);

        gbcRight.gridy = 10;
        txtEmail = new JTextField();
        txtEmail.setFont(AppConstants.FONT_BODY);
        txtEmail.setPreferredSize(new Dimension(320, 30));
        rightPanel.add(txtEmail, gbcRight);

        // Số điện thoại
        gbcRight.gridy = 11;
        JLabel lblPhone = new JLabel("Số điện thoại:");
        lblPhone.setFont(AppConstants.FONT_SMALL);
        lblPhone.setForeground(AppConstants.COLOR_TEXT_DARK);
        rightPanel.add(lblPhone, gbcRight);

        gbcRight.gridy = 12;
        txtPhone = new JTextField();
        txtPhone.setFont(AppConstants.FONT_BODY);
        txtPhone.setPreferredSize(new Dimension(320, 30));
        rightPanel.add(txtPhone, gbcRight);

        // Nhãn thông báo lỗi
        gbcRight.gridy = 13;
        lblError = new JLabel(" ");
        lblError.setFont(AppConstants.FONT_SMALL);
        lblError.setForeground(AppConstants.COLOR_DANGER);
        rightPanel.add(lblError, gbcRight);

        // Nút Đăng ký
        gbcRight.gridy = 14;
        gbcRight.insets = new Insets(10, 0, 10, 0);
        btnRegister = new JButton("Đăng Ký");
        btnRegister.setFont(AppConstants.FONT_SUBTITLE);
        btnRegister.setBackground(AppConstants.COLOR_SUCCESS);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setPreferredSize(new Dimension(320, 35));
        btnRegister.setFocusPainted(false);
        rightPanel.add(btnRegister, gbcRight);

        // Quay lại đăng nhập
        gbcRight.gridy = 15;
        gbcRight.insets = new Insets(0, 0, 0, 0);
        lblLoginLink = new JLabel("Đã có tài khoản? Đăng nhập tại đây");
        lblLoginLink.setFont(AppConstants.FONT_BODY);
        lblLoginLink.setForeground(AppConstants.COLOR_ACCENT);
        lblLoginLink.setHorizontalAlignment(SwingConstants.CENTER);
        lblLoginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightPanel.add(lblLoginLink, gbcRight);

        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // --- XỬ LÝ SỰ KIỆN ---

        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });

        lblLoginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new LoginView().setVisible(true);
            }
        });
    }

    private void handleRegister() {
        String username = txtUsername.getText();
        String fullName = txtFullName.getText();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        String email = txtEmail.getText();
        String phone = txtPhone.getText();

        lblError.setText(" "); // Reset lỗi

        // Gọi controller thực hiện đăng ký
        String errorMsg = authController.register(username, password, confirmPassword, fullName, email, phone);
        if (errorMsg != null) {
            lblError.setText(errorMsg);
        } else {
            JOptionPane.showMessageDialog(this, "Đăng ký tài khoản thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginView().setVisible(true);
        }
    }
}
