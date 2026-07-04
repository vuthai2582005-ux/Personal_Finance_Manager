package personalfinancemanager.view.budget;

import personalfinancemanager.config.AppConstants;
import personalfinancemanager.controller.BudgetController;
import personalfinancemanager.dao.CategoryDAO;
import personalfinancemanager.model.Category;
import personalfinancemanager.model.Budget;
import personalfinancemanager.util.SessionManager;
import personalfinancemanager.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Hộp thoại Popup Modal dùng để Thêm mới hoặc Chỉnh sửa hạn mức ngân sách chi tiêu.
 */
public class BudgetFormDialog extends JDialog {
    private final BudgetController budgetController;
    private final CategoryDAO categoryDAO;
    private final Budget targetBudget;
    private boolean isSaved = false;

    private JComboBox<Category> cbCategory;
    private JComboBox<Integer> cbMonth;
    private JSpinner spinYear;
    private JTextField txtLimit;
    private JLabel lblError;
    private RoundedButton btnSave;
    private RoundedButton btnCancel;

    public BudgetFormDialog(Frame parent, Budget budget) {
        super(parent, budget == null ? "Thiết Lập Ngân Sách Mới" : "Chỉnh Sửa Ngân Sách", true);
        this.budgetController = new BudgetController();
        this.categoryDAO = new CategoryDAO();
        this.targetBudget = budget;

        initComponents();
        loadCategories();
        populateData();
    }

    private void initComponents() {
        setSize(450, 420);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        setContentPane(mainPanel);

        // Tiêu đề
        JLabel lblTitle = new JLabel(targetBudget == null ? "Tạo Hạn Mức Ngân Sách" : "Cập Nhật Hạn Mức");
        lblTitle.setFont(AppConstants.FONT_TITLE);
        lblTitle.setForeground(AppConstants.COLOR_PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 4, 0);
        gbc.gridx = 0;

        // 1. Danh mục chi tiêu
        gbc.gridy = 0;
        JLabel lblCat = new JLabel("Danh mục chi tiêu (*):");
        lblCat.setFont(AppConstants.FONT_BODY);
        lblCat.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblCat, gbc);

        gbc.gridy = 1;
        cbCategory = new JComboBox<>();
        cbCategory.setFont(AppConstants.FONT_BODY);
        cbCategory.setPreferredSize(new Dimension(350, 32));
        formPanel.add(cbCategory, gbc);

        // 2. Chọn Tháng
        gbc.gridy = 2;
        JLabel lblMonth = new JLabel("Tháng:");
        lblMonth.setFont(AppConstants.FONT_BODY);
        lblMonth.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblMonth, gbc);

        gbc.gridy = 3;
        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        cbMonth = new JComboBox<>(months);
        cbMonth.setFont(AppConstants.FONT_BODY);
        cbMonth.setSelectedItem(LocalDate.now().getMonthValue());
        cbMonth.setPreferredSize(new Dimension(350, 32));
        formPanel.add(cbMonth, gbc);

        // 3. Chọn Năm
        gbc.gridy = 4;
        JLabel lblYear = new JLabel("Năm:");
        lblYear.setFont(AppConstants.FONT_BODY);
        lblYear.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblYear, gbc);

        gbc.gridy = 5;
        int currentYear = LocalDate.now().getYear();
        SpinnerNumberModel yearModel = new SpinnerNumberModel(currentYear, currentYear - 5, currentYear + 10, 1);
        spinYear = new JSpinner(yearModel);
        spinYear.setFont(AppConstants.FONT_BODY);
        // Thiết lập không cho format dấu phẩy ở hiển thị năm (ví dụ: 2,026 -> 2026)
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinYear, "#");
        spinYear.setEditor(editor);
        spinYear.setPreferredSize(new Dimension(350, 32));
        formPanel.add(spinYear, gbc);

        // 4. Hạn mức tiền
        gbc.gridy = 6;
        JLabel lblLimit = new JLabel("Hạn mức ngân sách (*):");
        lblLimit.setFont(AppConstants.FONT_BODY);
        lblLimit.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblLimit, gbc);

        gbc.gridy = 7;
        txtLimit = new JTextField();
        txtLimit.setFont(AppConstants.FONT_BODY);
        txtLimit.setPreferredSize(new Dimension(350, 32));
        formPanel.add(txtLimit, gbc);

        // Báo lỗi
        gbc.gridy = 8;
        lblError = new JLabel(" ");
        lblError.setFont(AppConstants.FONT_SMALL);
        lblError.setForeground(AppConstants.COLOR_DANGER);
        formPanel.add(lblError, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Nút
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

        // --- SỰ KIỆN ---
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> handleSave());
    }

    private void loadCategories() {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        int userId = SessionManager.getInstance().getCurrentUser().getUserId();

        // Chỉ nạp các danh mục CHI TIÊU (EXPENSE) để lập ngân sách
        List<Category> list = categoryDAO.findByType(userId, "EXPENSE");
        cbCategory.removeAllItems();
        for (Category c : list) {
            cbCategory.addItem(c);
        }
    }

    private void populateData() {
        if (targetBudget != null) {
            // Khi cập nhật ngân sách hiện có, không cho sửa Danh mục, Tháng, Năm
            cbCategory.setEnabled(false);
            cbMonth.setEnabled(false);
            spinYear.setEnabled(false);

            // Tìm và chọn category
            // Lưu ý: Đối tượng targetBudget tải từ View chỉ chứa categoryName mà không chứa categoryId
            // Tuy nhiên ta chỉ cần hiển thị text danh mục để người dùng xem
            cbCategory.removeAllItems();
            Category fakeCat = new Category();
            fakeCat.setCategoryName(targetBudget.getCategoryName());
            cbCategory.addItem(fakeCat);
            cbCategory.setSelectedIndex(0);

            cbMonth.setSelectedItem(targetBudget.getMonth());
            spinYear.setValue(targetBudget.getYear());
            txtLimit.setText(targetBudget.getBudgetAmount().toPlainString());
        }
    }

    private void handleSave() {
        String limitStr = txtLimit.getText();
        lblError.setText(" ");

        String errorMsg;
        if (targetBudget == null) {
            Category selectedCat = (Category) cbCategory.getSelectedItem();
            if (selectedCat == null) {
                lblError.setText("Vui lòng chọn danh mục chi tiêu.");
                return;
            }
            int categoryId = selectedCat.getCategoryId();
            int month = (Integer) cbMonth.getSelectedItem();
            int year = (Integer) spinYear.getValue();
            int userId = SessionManager.getInstance().getCurrentUser().getUserId();

            errorMsg = budgetController.addBudget(categoryId, month, year, limitStr, userId);
        } else {
            errorMsg = budgetController.updateBudget(targetBudget.getBudgetId(), limitStr);
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
