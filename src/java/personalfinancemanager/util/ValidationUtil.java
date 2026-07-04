package personalfinancemanager.util;

import java.util.regex.Pattern;

/**
 * Tiện ích kiểm tra tính hợp lệ của dữ liệu đầu vào.
 */
public class ValidationUtil {
    
    // Regex cho Email theo chuẩn RFC 5322
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Regex cho Số điện thoại Việt Nam (10 số, bắt đầu bằng 03, 05, 07, 08, 09)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(03|05|07|08|09)\\d{8}$"
    );

    /**
     * Kiểm tra chuỗi có bị rỗng hoặc null không.
     */
    public static boolean isNullOrEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Kiểm tra định dạng Email hợp lệ.
     */
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Kiểm tra định dạng số điện thoại Việt Nam.
     */
    public static boolean isValidPhone(String phone) {
        if (isNullOrEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }
}
