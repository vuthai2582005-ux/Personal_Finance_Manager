package personalfinancemanager.view.category;

import personalfinancemanager.config.AppConstants;
import personalfinancemanager.controller.CategoryController;
import personalfinancemanager.model.Category;
import personalfinancemanager.util.SessionManager;
import personalfinancemanager.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Hộp thoại Popup Modal dùng để Thêm mới hoặc Chỉnh sửa danh mục thu/chi.
 */
public class CategoryFormDialog extends JDialog {
    private final CategoryController categoryController;
    private final Category targetCategory;
    private boolean isSaved = false;

    private JTextField txtCategoryName;
    private JComboBox<String> cbType;
    private JComboBox<String> cbColor;
    private JLabel lblError;
    private RoundedButton btnSave;
    private RoundedButton btnCancel;

    // Danh sách phối màu đẹp mắt
    private static final Map<String, String> colorMap = new LinkedHashMap<>();

    static {
        colorMap.put("Đỏ cá hồng", "#FF6B6B");
        colorMap.put("Cam hoàng hôn", "#FF8E53");
        colorMap.put("Cam sáng", "#FFA94D");
        colorMap.put("Vàng hổ phách", "#FFD93D");
        colorMap.put("Xanh lục tươi", "#6BCB77");
        colorMap.put("Xanh lá đậm", "#51CF66");
        colorMap.put("Xanh lam pastel", "#4D96FF");
        colorMap.put("Xanh dương dịu", "#339AF0");
        colorMap.put("Tím hoa cà", "#C77DFF");
        colorMap.put("Hồng anh đào", "#F06595");
        colorMap.put("Xám ngọc trai", "#ADB5BD");
    }

    public CategoryFormDialog(Frame parent, Category category) {
        super(parent, category == null ? "Thêm Danh Mục Mới" : "Chỉnh Sửa Danh Mục", true);
        this.categoryController = new CategoryController();
        this.targetCategory = category;

        initComponents();
        populateData();
    }

    private void initComponents() {
        setSize(420, 360);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        setContentPane(mainPanel);

        // Tiêu đề
        JLabel lblTitle = new JLabel(targetCategory == null ? "Tạo Danh Mục" : "Cập Nhật Danh Mục");
        lblTitle.setFont(AppConstants.FONT_TITLE);
        lblTitle.setForeground(AppConstants.COLOR_PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Form nhập liệu
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 4, 0);
        gbc.gridx = 0;

        // Tên danh mục
        gbc.gridy = 0;
        JLabel lblName = new JLabel("Tên danh mục (*):");
        lblName.setFont(AppConstants.FONT_BODY);
        lblName.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblName, gbc);

        gbc.gridy = 1;
        txtCategoryName = new JTextField();
        txtCategoryName.setFont(AppConstants.FONT_BODY);
        txtCategoryName.setPreferredSize(new Dimension(320, 35));
        formPanel.add(txtCategoryName, gbc);

        // Loại danh mục
        gbc.gridy = 2;
        JLabel lblType = new JLabel("Phân loại:");
        lblType.setFont(AppConstants.FONT_BODY);
        lblType.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblType, gbc);

        gbc.gridy = 3;
        String[] types = {"Chi tiêu (EXPENSE)", "Thu nhập (INCOME)"};
        cbType = new JComboBox<>(types);
        cbType.setFont(AppConstants.FONT_BODY);
        cbType.setPreferredSize(new Dimension(320, 35));
        formPanel.add(cbType, gbc);

        // Màu sắc
        gbc.gridy = 4;
        JLabel lblCol = new JLabel("Màu sắc đại diện:");
        lblCol.setFont(AppConstants.FONT_BODY);
        lblCol.setForeground(AppConstants.COLOR_TEXT_DARK);
        formPanel.add(lblCol, gbc);

        gbc.gridy = 5;
        String[] colors = colorMap.keySet().toArray(new String[0]);
        cbColor = new JComboBox<>(colors);
        cbColor.setFont(AppConstants.FONT_BODY);
        cbColor.setPreferredSize(new Dimension(320, 35));
        formPanel.add(cbColor, gbc);

        // Nhãn hiển thị lỗi
        gbc.gridy = 6;
        lblError = new JLabel(" ");
        lblError.setFont(AppConstants.FONT_SMALL);
        lblError.setForeground(AppConstants.COLOR_DANGER);
        formPanel.add(lblError, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Nút lưu / hủy
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

    private void populateData() {
        if (targetCategory != null) {
            txtCategoryName.setText(targetCategory.getCategoryName());
            
            if ("INCOME".equals(targetCategory.getType())) {
                cbType.setSelectedIndex(1);
            } else {
                cbType.setSelectedIndex(0);
            }

            // Chọn màu khớp trong Map
            String hex = targetCategory.getColor();
            for (Map.Entry<String, String> entry : colorMap.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(hex)) {
                    cbColor.setSelectedItem(entry.getKey());
                    break;
                }
            }
        }
    }

    private void handleSave() {
        String name = txtCategoryName.getText();
        String selectedType = (String) cbType.getSelectedItem();
        String type = selectedType.contains("INCOME") ? "INCOME" : "EXPENSE";
        String colorName = (String) cbColor.getSelectedItem();
        String colorHex = colorMap.get(colorName);
        int userId = SessionManager.getInstance().getCurrentUser().getUserId();

        lblError.setText(" ");

        String errorMsg;
        if (targetCategory == null) {
            errorMsg = categoryController.addCategory(name, type, colorHex, userId);
        } else {
            errorMsg = categoryController.updateCategory(targetCategory.getCategoryId(), name, type, colorHex, userId);
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
