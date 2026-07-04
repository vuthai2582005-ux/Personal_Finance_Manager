package personalfinancemanager.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiện ích mã hóa và kiểm tra mật khẩu bằng BCrypt.
 */
public class PasswordUtil {

    /**
     * Mã hóa mật khẩu dạng thô thành chuỗi băm (Cost factor mặc định = 10).
     * 
     * @param plainPassword Mật khẩu thô
     * @return Chuỗi băm BCrypt
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Mật khẩu không được phép rỗng.");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    /**
     * Kiểm tra xem mật khẩu dạng thô có khớp với mật khẩu băm đã lưu không.
     * 
     * @param plainPassword Mật khẩu thô do người dùng nhập
     * @param hashedPassword Mật khẩu băm lưu trong cơ sở dữ liệu
     * @return true nếu khớp, ngược lại là false
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            System.err.println("Lỗi khi kiểm tra mật khẩu BCrypt: " + e.getMessage());
            return false;
        }
    }
}
