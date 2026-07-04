package personalfinancemanager.config;

import java.awt.Color;
import java.awt.Font;

/**
 * Hằng số toàn hệ thống ứng dụng.
 */
public class AppConstants {
    // Cấu hình Database SQL Server
    public static final String DB_SERVER = "localhost";
    public static final int DB_PORT = 1433;
    public static final String DB_NAME = "PersonalFinanceDB";
    public static final String DB_USER = "sa";
    public static final String DB_PASSWORD = "123"; // Thay bằng mật khẩu SQL Server của bạn

    public static final String DB_URL = String.format(
        "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false;trustServerCertificate=true",
        DB_SERVER, DB_PORT, DB_NAME
    );

    // Kích thước cửa sổ chính
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 750;

    // Định dạng ngày và tiền tệ
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String CURRENCY_CODE = "VND";

    // Phối màu giao diện (Tiếng Việt)
    public static final Color COLOR_PRIMARY = new Color(24, 43, 73);      // Xanh đen đậm sang trọng
    public static final Color COLOR_ACCENT = new Color(41, 128, 185);     // Xanh dương nổi bật
    public static final Color COLOR_BG_LIGHT = new Color(245, 247, 250);   // Nền sáng nhẹ
    public static final Color COLOR_TEXT_DARK = new Color(44, 62, 80);     // Màu chữ tối chính
    public static final Color COLOR_TEXT_LIGHT = new Color(127, 140, 141); // Màu chữ chú thích xám
    public static final Color COLOR_SUCCESS = new Color(39, 174, 96);      // Màu xanh lục thành công/thu nhập
    public static final Color COLOR_DANGER = new Color(192, 57, 43);       // Màu đỏ nguy hiểm/chi tiêu
    public static final Color COLOR_WARNING = new Color(230, 126, 34);     // Màu cam cảnh báo vượt ngân sách
    public static final Color COLOR_CARD_BG = Color.WHITE;                 // Màu nền thẻ chức năng

    // Font chữ hệ thống
    public static final String FONT_FAMILY = "Segoe UI";
    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font(FONT_FAMILY, Font.BOLD, 16);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
}
