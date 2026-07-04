package personalfinancemanager.view.account;

import personalfinancemanager.config.AppConstants;
import personalfinancemanager.controller.AccountController;
import personalfinancemanager.model.Account;
import personalfinancemanager.util.SessionManager;
import personalfinancemanager.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Hộp thoại Popup Modal dùng để Thêm mới hoặc Chỉnh sửa thông tin tài khoản.
 */
public class AccountFormDialog extends JDialog {
    private final AccountController accountController;
    private final Account targetAccount; // Null nếu là Thêm mới, khác Null nếu là Chỉnh sửa
    private boolean isSaved = false;

    private JTextField txtAccountName;
    private JComboBox<String> cbAccountType;
    private JTextField txtBalance;
    private JCheckBox chkActive;
    private JLabel lblError;
    private RoundedButton btnSave;
    private RoundedButton btnCancel;

    // Map chuyển đổi giữa hiển thị Tiếng Việt và Giá trị lưu DB
    private static final Map<String, String> typeMapDisplayToDb = new HashMap<>();
    private static final Map<String, String> typeMapDbToDisplay = new HashMap<>();

    static {
        typeMapDisplayToDb.put("Tiền mặt", "CASH");
        typeMapDisplayToDb.put("Ngân hàng", "BANK");
        typeMapDisplayToDb.put("Ví điện tử", "E_WALLET");
        typeMapDisplayToDb.put("Thẻ tín dụng", "CREDIT_CARD");
        typeMapDisplayToDb.put("Tiết kiệm", "SAVINGS");

        for (Map.Entry<String, String> entry : typeMapDisplayToDb.entrySet()) {
            typeMapDbToDisplay.put(entry.getValue(), entry.getKey());
        }
    }

    public AccountFormDialog(Frame parent, Account account) {
        super(parent, account == null ? "Thêm Tài Khoản Mới" : "Chỉnh Sửa Tài Khoản", true);
        this.accountController = new AccountController();
        this.targetAccount = account;

        initComponents();
        populateData();
    }

    private void initComponents() {
        setSize(450, 400);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        setContentPane(mainPanel);

        // Tiêu đề form
        JLabel lblTitle = new JLabel(targetAccount == null ? "Tạo Tài Khoản" : "Cập Nhật Tài Khoản");
        lblTitle.setFont(AppConstants.FONT_TITLE);
        lblTitle.setForeground(AppConstants.COLOR_PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Vùng nhập liệu (Form) sử dụng GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.gridx = 0;

        // Tên tài khoản
        gbc.gridy = 0;
        JLabel lblName = new JLabel("Tên tài khoản (*):");
        lblName.setFont(AppConstants.FONT_BODY);
        lblName.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblName, gbc);

        gbc.gridy = 1;
        txtAccountName = new JTextField();
        txtAccountName.setFont(AppConstants.FONT_BODY);
        txtAccountName.setPreferredSize(new Dimension(350, 35));
        formPanel.add(txtAccountName, gbc);

        // Loại tài khoản
        gbc.gridy = 2;
        JLabel lblType = new JLabel("Loại tài khoản:");
        lblType.setFont(AppConstants.FONT_BODY);
        lblType.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblType, gbc);

        gbc.gridy = 3;
        String[] types = {"Tiền mặt", "Ngân hàng", "Ví điện tử", "Thẻ tín dụng", "Tiết kiệm"};
        cbAccountType = new JComboBox<>(types);
        cbAccountType.setFont(AppConstants.FONT_BODY);
        cbAccountType.setPreferredSize(new Dimension(350, 35));
        formPanel.add(cbAccountType, gbc);

        // Số dư
        gbc.gridy = 4;
        JLabel lblBal = new JLabel(targetAccount == null ? "Số dư ban đầu (*):" : "Số dư hiện tại (*):");
        lblBal.setFont(AppConstants.FONT_BODY);
        lblBal.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblBal, gbc);

        gbc.gridy = 5;
        txtBalance = new JTextField("0");
        txtBalance.setFont(AppConstants.FONT_BODY);
        txtBalance.setPreferredSize(new Dimension(350, 35));
        formPanel.add(txtBalance, gbc);

        // Trạng thái hoạt động (chỉ hiện khi Sửa)
        gbc.gridy = 6;
        chkActive = new JCheckBox("Còn hoạt động (Hiển thị trong hệ thống)");
        chkActive.setFont(AppConstants.FONT_SMALL);
        chkActive.setOpaque(false);
        chkActive.setSelected(true);
        if (targetAccount == null) {
            chkActive.setVisible(false);
        }
        formPanel.add(chkActive, gbc);

        // Nhãn báo lỗi
        gbc.gridy = 7;
        lblError = new JLabel(" ");
        lblError.setFont(AppConstants.FONT_SMALL);
        lblError.setForeground(AppConstants.COLOR_DANGER);
        formPanel.add(lblError, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Hàng nút bấm ở dưới cùng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        btnCancel = new RoundedButton("Hủy bỏ", new Color(189, 195, 199));
        btnCancel.setForeground(Color.DARK_GRAY);
        btnCancel.setPreferredSize(new Dimension(100, 35));
        buttonPanel.add(btnCancel);

        btnSave = new RoundedButton("Lưu lại", AppConstants.COLOR_SUCCESS);
        btnSave.setPreferredSize(new Dimension(100, 35));
        buttonPanel.add(btnSave);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- XỬ LÝ SỰ KIỆN ---

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSave();
            }
        });
    }

    /**
     * Điền thông tin cũ vào các trường nếu ở chế độ Sửa.
     */
    private void populateData() {
        if (targetAccount != null) {
            txtAccountName.setText(targetAccount.getAccountName());
            String displayType = typeMapDbToDisplay.getOrDefault(targetAccount.getAccountType(), "Tiền mặt");
            cbAccountType.setSelectedItem(displayType);
            txtBalance.setText(targetAccount.getBalance().toPlainString());
            chkActive.setSelected(targetAccount.isActive());
        }
    }

    private void handleSave() {
        String name = txtAccountName.getText();
        String displayType = (String) cbAccountType.getSelectedItem();
        String dbType = typeMapDisplayToDb.get(displayType);
        String balanceStr = txtBalance.getText();
        int userId = SessionManager.getInstance().getCurrentUser().getUserId();

        lblError.setText(" "); // Reset lỗi

        String errorMsg;
        if (targetAccount == null) {
            // Nghiệp vụ Thêm mới
            errorMsg = accountController.addAccount(name, dbType, balanceStr, userId);
        } else {
            // Nghiệp vụ Cập nhật
            boolean active = chkActive.isSelected();
            errorMsg = accountController.updateAccount(targetAccount.getAccountId(), name, dbType, balanceStr, active, userId);
        }

        if (errorMsg != null) {
            lblError.setText(errorMsg);
        } else {
            isSaved = true;
            dispose();
        }
    }

    public boolean isSaved() {
        return isSaved;
    }
}
