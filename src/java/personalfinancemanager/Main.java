package personalfinancemanager;

import personalfinancemanager.config.DatabaseConnection;
import personalfinancemanager.view.auth.LoginView;

import javax.swing.*;

/**
 * Điểm khởi chạy của ứng dụng Quản Lý Tài Chính Cá Nhân.
 */
public class Main {
    public static void main(String[] args) {
        // 1. Cấu hình giao diện Look and Feel (Sử dụng Nimbus L&F)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể thiết lập Look and Feel Nimbus. Sử dụng giao diện mặc định.");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // 2. Chạy thử nghiệm kết nối Cơ sở dữ liệu trong background
        new Thread(() -> {
            System.out.println("Đang kiểm tra kết nối SQL Server...");
            boolean dbSuccess = DatabaseConnection.testConnection();
            if (!dbSuccess) {
                System.err.println("CẢNH BÁO: Không thể kết nối tới cơ sở dữ liệu. Vui lòng kiểm tra lại cấu hình SQL Server!");
            }
        }).start();

        // 3. Khởi chạy giao diện Đăng nhập
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
}
