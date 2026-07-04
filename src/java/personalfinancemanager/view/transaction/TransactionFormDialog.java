package personalfinancemanager.view.transaction;

import personalfinancemanager.config.AppConstants;
import personalfinancemanager.controller.TransactionController;
import personalfinancemanager.dao.AccountDAO;
import personalfinancemanager.dao.CategoryDAO;
import personalfinancemanager.model.Account;
import personalfinancemanager.model.Category;
import personalfinancemanager.model.Transaction;
import personalfinancemanager.util.DateUtil;
import personalfinancemanager.util.SessionManager;
import personalfinancemanager.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;

/**
 * Hộp thoại Popup Modal dùng để Thêm mới hoặc Chỉnh sửa thông tin giao dịch.
 */
public class TransactionFormDialog extends JDialog {
    private final TransactionController transactionController;
    private final AccountDAO accountDAO;
    private final CategoryDAO categoryDAO;
    private final Transaction targetTransaction;
    private boolean isSaved = false;

    private JComboBox<Account> cbAccount;
    private JComboBox<Category> cbCategory;
    private JComboBox<String> cbType;
    private JTextField txtAmount;
    private JTextField txtDate;
    private JTextField txtDescription;
    private JLabel lblError;
    private RoundedButton btnSave;
    private RoundedButton btnCancel;

    public TransactionFormDialog(Frame parent, Transaction transaction) {
        super(parent, transaction == null ? "Thêm Giao Dịch Mới" : "Chỉnh Sửa Giao Dịch", true);
        this.transactionController = new TransactionController();
        this.accountDAO = new AccountDAO();
        this.categoryDAO = new CategoryDAO();
        this.targetTransaction = transaction;

        initComponents();
        loadDropdownData();
        populateData();
    }

    private void initComponents() {
        setSize(480, 480);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        setContentPane(mainPanel);

        // Tiêu đề
        JLabel lblTitle = new JLabel(targetTransaction == null ? "Tạo Giao Dịch" : "Cập Nhật Giao Dịch");
        lblTitle.setFont(AppConstants.FONT_TITLE);
        lblTitle.setForeground(AppConstants.COLOR_PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Form nhập liệu
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 4, 0);
        gbc.gridx = 0;

        // 1. Loại giao dịch (Thu nhập / Chi tiêu)
        gbc.gridy = 0;
        JLabel lblType = new JLabel("Loại giao dịch:");
        lblType.setFont(AppConstants.FONT_BODY);
        lblType.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblType, gbc);

        gbc.gridy = 1;
        String[] types = {"Chi tiêu (EXPENSE)", "Thu nhập (INCOME)"};
        cbType = new JComboBox<>(types);
        cbType.setFont(AppConstants.FONT_BODY);
        cbType.setPreferredSize(new Dimension(380, 32));
        formPanel.add(cbType, gbc);

        // 2. Tài khoản
        gbc.gridy = 2;
        JLabel lblAcc = new JLabel("Tài khoản:");
        lblAcc.setFont(AppConstants.FONT_BODY);
        lblAcc.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblAcc, gbc);

        gbc.gridy = 3;
        cbAccount = new JComboBox<>();
        cbAccount.setFont(AppConstants.FONT_BODY);
        cbAccount.setPreferredSize(new Dimension(380, 32));
        formPanel.add(cbAccount, gbc);

        // 3. Danh mục
        gbc.gridy = 4;
        JLabel lblCat = new JLabel("Danh mục:");
        lblCat.setFont(AppConstants.FONT_BODY);
        lblCat.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblCat, gbc);

        gbc.gridy = 5;
        cbCategory = new JComboBox<>();
        cbCategory.setFont(AppConstants.FONT_BODY);
        cbCategory.setPreferredSize(new Dimension(380, 32));
        formPanel.add(cbCategory, gbc);

        // 4. Số tiền
        gbc.gridy = 6;
        JLabel lblAmount = new JLabel("Số tiền (*):");
        lblAmount.setFont(AppConstants.FONT_BODY);
        lblAmount.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblAmount, gbc);

        gbc.gridy = 7;
        txtAmount = new JTextField();
        txtAmount.setFont(AppConstants.FONT_BODY);
        txtAmount.setPreferredSize(new Dimension(380, 32));
        formPanel.add(txtAmount, gbc);

        // 5. Ngày giao dịch
        gbc.gridy = 8;
        JLabel lblDate = new JLabel("Ngày giao dịch (dd/MM/yyyy) (*):");
        lblDate.setFont(AppConstants.FONT_BODY);
        lblDate.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblDate, gbc);

        gbc.gridy = 9;
        txtDate = new JTextField(DateUtil.formatDate(LocalDate.now()));
        txtDate.setFont(AppConstants.FONT_BODY);
        txtDate.setPreferredSize(new Dimension(380, 32));
        formPanel.add(txtDate, gbc);

        // 6. Ghi chú
        gbc.gridy = 10;
        JLabel lblDesc = new JLabel("Ghi chú:");
        lblDesc.setFont(AppConstants.FONT_BODY);
        lblDesc.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblDesc, gbc);

        gbc.gridy = 11;
        txtDescription = new JTextField();
        txtDescription.setFont(AppConstants.FONT_BODY);
        txtDescription.setPreferredSize(new Dimension(380, 32));
        formPanel.add(txtDescription, gbc);

        // Nhãn thông báo lỗi
        gbc.gridy = 12;
        lblError = new JLabel(" ");
        lblError.setFont(AppConstants.FONT_SMALL);
        lblError.setForeground(AppConstants.COLOR_DANGER);
        formPanel.add(lblError, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Hàng nút dưới cùng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        btnCancel = new RoundedButton("Hủy bỏ", new Color(189, 195, 199));
        btnCancel.setForeground(Color.DARK_GRAY);
        btnCancel.setPreferredSize(new Dimension(90, 32));
        buttonPanel.add(btnCancel);

        btnSave = new RoundedButton("Lưu lại", AppConstants.COLOR_SUCCESS);
        btnSave.setPreferredSize(new Dimension(90, 32));
        buttonPanel.add(btnSave);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- XỬ LÝ SỰ KIỆN ---

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> handleSave());

        // Lắng nghe thay đổi loại giao dịch để lọc danh mục tương ứng
        cbType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterCategoriesByType();
            }
        });
    }

    /**
     * Tải dữ liệu vào combobox tài khoản và danh mục.
     */
    private void loadDropdownData() {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        int userId = SessionManager.getInstance().getCurrentUser().getUserId();

        // Load Tài khoản
        List<Account> accounts = accountDAO.findByUserId(userId);
        cbAccount.removeAllItems();
        for (Account a : accounts) {
            cbAccount.addItem(a);
        }

        // Lọc danh mục theo loại mặc định (Chi tiêu)
        filterCategoriesByType();
    }

    /**
     * Lọc và nạp danh mục dựa vào loại giao dịch đang chọn.
     */
    private void filterCategoriesByType() {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        int userId = SessionManager.getInstance().getCurrentUser().getUserId();

        String selectedType = (String) cbType.getSelectedItem();
        String type = selectedType.contains("INCOME") ? "INCOME" : "EXPENSE";

        List<Category> categories = categoryDAO.findByType(userId, type);
        cbCategory.removeAllItems();
        for (Category c : categories) {
            cbCategory.addItem(c);
        }
    }

    private void populateData() {
        if (targetTransaction != null) {
            if ("INCOME".equals(targetTransaction.getType())) {
                cbType.setSelectedIndex(1);
            } else {
                cbType.setSelectedIndex(0);
            }

            // Lọc lại để đồng bộ danh mục
            filterCategoriesByType();

            // Chọn Account cũ
            for (int i = 0; i < cbAccount.getItemCount(); i++) {
                if (cbAccount.getItemAt(i).getAccountId() == targetTransaction.getAccountId()) {
                    cbAccount.setSelectedIndex(i);
                    break;
                }
            }

            // Chọn Category cũ
            for (int i = 0; i < cbCategory.getItemCount(); i++) {
                if (cbCategory.getItemAt(i).getCategoryId() == targetTransaction.getCategoryId()) {
                    cbCategory.setSelectedIndex(i);
                    break;
                }
            }

            txtAmount.setText(targetTransaction.getAmount().toPlainString());
            txtDate.setText(DateUtil.formatDate(targetTransaction.getTransactionDate()));
            txtDescription.setText(targetTransaction.getDescription() != null ? targetTransaction.getDescription() : "");
        }
    }

    private void handleSave() {
        Account selectedAcc = (Account) cbAccount.getSelectedItem();
        Category selectedCat = (Category) cbCategory.getSelectedItem();
        String selectedType = (String) cbType.getSelectedItem();
        String type = selectedType.contains("INCOME") ? "INCOME" : "EXPENSE";
        String amountStr = txtAmount.getText();
        String dateStr = txtDate.getText();
        String desc = txtDescription.getText();

        lblError.setText(" ");

        if (selectedAcc == null) {
            lblError.setText("Vui lòng chọn tài khoản giao dịch.");
            return;
        }
        if (selectedCat == null) {
            lblError.setText("Vui lòng chọn danh mục thu chi.");
            return;
        }

        String errorMsg;
        if (targetTransaction == null) {
            errorMsg = transactionController.addTransaction(
                selectedAcc.getAccountId(),
                selectedCat.getCategoryId(),
                type,
                amountStr,
                dateStr,
                desc
            );
        } else {
            errorMsg = transactionController.updateTransaction(
                targetTransaction.getTransactionId(),
                selectedAcc.getAccountId(),
                selectedCat.getCategoryId(),
                type,
                amountStr,
                dateStr,
                desc
            );
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
