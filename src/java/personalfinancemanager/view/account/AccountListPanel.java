package personalfinancemanager.view.account;

import personalfinancemanager.config.AppConstants;
import personalfinancemanager.controller.AccountController;
import personalfinancemanager.model.Account;
import personalfinancemanager.util.CurrencyFormatter;
import personalfinancemanager.util.SessionManager;
import personalfinancemanager.view.components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Giao diện quản lý danh sách tài khoản (CRUD Accounts - Tiếng Việt).
 */
public class AccountListPanel extends JPanel {
    private final AccountController accountController;
    private List<Account> accountList;

    private JTable tblAccounts;
    private DefaultTableModel tableModel;
    private RoundedButton btnAdd;
    private RoundedButton btnEdit;
    private RoundedButton btnDelete;

    private static final Map<String, String> typeMapDbToDisplay = new HashMap<>();

    static {
        typeMapDbToDisplay.put("CASH", "Tiền mặt");
        typeMapDbToDisplay.put("BANK", "Ngân hàng");
        typeMapDbToDisplay.put("E_WALLET", "Ví điện tử");
        typeMapDbToDisplay.put("CREDIT_CARD", "Thẻ tín dụng");
        typeMapDbToDisplay.put("SAVINGS", "Tiết kiệm");
    }

    public AccountListPanel() {
        this.accountController = new AccountController();
        initComponents();
        refreshData();
    }

    private void initComponents() {
        setBackground(AppConstants.COLOR_BG_LIGHT);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // 1. TIÊU ĐỀ TRANG VÀ THANH CÔNG CỤ (NÚT THÊM/SỬA/XÓA)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh Sách Tài Khoản");
        lblTitle.setFont(AppConstants.FONT_TITLE);
        lblTitle.setForeground(AppConstants.COLOR_PRIMARY);
        topPanel.add(lblTitle, BorderLayout.WEST);

        // Thanh chứa các nút hành động bên phải
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        toolbarPanel.setOpaque(false);

        btnAdd = new RoundedButton("Thêm Mới", AppConstants.COLOR_ACCENT);
        btnAdd.setPreferredSize(new Dimension(120, 35));
        toolbarPanel.add(btnAdd);

        btnEdit = new RoundedButton("Chỉnh Sửa", AppConstants.COLOR_WARNING);
        btnEdit.setPreferredSize(new Dimension(120, 35));
        toolbarPanel.add(btnEdit);

        btnDelete = new RoundedButton("Xóa Bỏ", AppConstants.COLOR_DANGER);
        btnDelete.setPreferredSize(new Dimension(120, 35));
        toolbarPanel.add(btnDelete);

        topPanel.add(toolbarPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2. PANEL CHÍNH CHỨA BẢNG TÀI KHOẢN
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] columns = {"Tên tài khoản", "Loại tài khoản", "Số dư hiện tại"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp trên bảng
            }
        };

        tblAccounts = new JTable(tableModel);
        tblAccounts.setFont(AppConstants.FONT_BODY);
        tblAccounts.setRowHeight(40);
        tblAccounts.setShowGrid(false);
        tblAccounts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom Header bảng
        JTableHeader header = tblAccounts.getTableHeader();
        header.setFont(AppConstants.FONT_SUBTITLE);
        header.setBackground(AppConstants.COLOR_BG_LIGHT);
        header.setForeground(AppConstants.COLOR_TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 40));

        // Renderer căn lề trái và phải
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        rightRenderer.setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 14));

        tblAccounts.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        tblAccounts.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        tblAccounts.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(tblAccounts);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // --- XỬ LÝ SỰ KIỆN ---

        // Nút Thêm Mới
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(AccountListPanel.this);
                AccountFormDialog dialog = new AccountFormDialog(parentFrame, null);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    refreshData();
                }
            }
        });

        // Nút Chỉnh Sửa
        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tblAccounts.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(AccountListPanel.this, 
                        "Vui lòng chọn tài khoản muốn chỉnh sửa từ danh sách.", 
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Account selectedAccount = accountList.get(selectedRow);
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(AccountListPanel.this);
                AccountFormDialog dialog = new AccountFormDialog(parentFrame, selectedAccount);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    refreshData();
                }
            }
        });

        // Nút Xóa Bỏ
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tblAccounts.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(AccountListPanel.this, 
                        "Vui lòng chọn tài khoản muốn xóa từ danh sách.", 
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Account selectedAccount = accountList.get(selectedRow);
                int confirm = JOptionPane.showConfirmDialog(AccountListPanel.this,
                    "Bạn có chắc chắn muốn xóa tài khoản \"" + selectedAccount.getAccountName() + "\"?\n" +
                    "Nếu tài khoản đã có giao dịch phát sinh, hệ thống sẽ ngưng hoạt động tài khoản này thay vì xóa vật lý.",
                    "Xác nhận xóa tài khoản",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    String errorMsg = accountController.deleteAccount(selectedAccount.getAccountId());
                    if (errorMsg != null) {
                        JOptionPane.showMessageDialog(AccountListPanel.this, errorMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(AccountListPanel.this, "Đã xử lý tài khoản thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        refreshData();
                    }
                }
            }
        });
    }

    /**
     * Tải lại toàn bộ dữ liệu tài khoản từ cơ sở dữ liệu lên bảng.
     */
    public void refreshData() {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        
        int userId = SessionManager.getInstance().getCurrentUser().getUserId();
        accountList = accountController.getAccountsByUserId(userId);

        tableModel.setRowCount(0); // Xóa dữ liệu dòng cũ
        for (Account acc : accountList) {
            String typeText = typeMapDbToDisplay.getOrDefault(acc.getAccountType(), "Tiền mặt");
            tableModel.addRow(new Object[]{
                acc.getAccountName(),
                typeText,
                CurrencyFormatter.format(acc.getBalance())
            });
        }
    }
}
