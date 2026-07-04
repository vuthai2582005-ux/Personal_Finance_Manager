package personalfinancemanager.controller;

import personalfinancemanager.dao.UserDAO;
import personalfinancemanager.dao.CategoryDAO;
import personalfinancemanager.model.User;
import personalfinancemanager.util.PasswordUtil;
import personalfinancemanager.util.SessionManager;
import personalfinancemanager.util.ValidationUtil;

/**
 * Controller xử lý logic Đăng nhập và Đăng ký.
 */
public class AuthController {
    private final UserDAO userDAO;
    private final CategoryDAO categoryDAO;

    public AuthController() {
        this.userDAO = new UserDAO();
        this.categoryDAO = new CategoryDAO();
    }

    /**
     * Xử lý đăng nhập hệ thống.
     * 
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return null nếu đăng nhập thành công, chuỗi thông báo lỗi tiếng Việt nếu thất bại
     */
    public String login(String username, String password) {
        if (ValidationUtil.isNullOrEmpty(username) || ValidationUtil.isNullOrEmpty(password)) {
            return "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.";
        }

        username = username.trim();

        // Tìm kiếm người dùng
        User user = userDAO.findByUsername(username);
        if (user == null) {
            return "Tên đăng nhập không tồn tại.";
        }

        // Kiểm tra mật khẩu băm
        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            return "Mật khẩu không chính xác.";
        }

        // Lưu thông tin người dùng vào Session
        SessionManager.getInstance().setCurrentUser(user);
        return null; // Thành công
    }

    /**
     * Xử lý đăng ký tài khoản người dùng mới.
     * 
     * @return null nếu đăng ký thành công, chuỗi thông báo lỗi tiếng Việt nếu thất bại
     */
    public String register(String username, String password, String confirmPassword, String fullName, String email, String phone) {
        // 1. Kiểm tra các trường bắt buộc
        if (ValidationUtil.isNullOrEmpty(username)) {
            return "Tên đăng nhập không được để trống.";
        }
        if (ValidationUtil.isNullOrEmpty(password)) {
            return "Mật khẩu không được để trống.";
        }
        if (ValidationUtil.isNullOrEmpty(fullName)) {
            return "Họ và tên không được để trống.";
        }

        username = username.trim();
        fullName = fullName.trim();

        // Kiểm tra độ dài mật khẩu
        if (password.length() < 6) {
            return "Mật khẩu phải chứa ít nhất 6 ký tự.";
        }

        // Kiểm tra xác nhận mật khẩu
        if (!password.equals(confirmPassword)) {
            return "Mật khẩu xác nhận không khớp.";
        }

        // 2. Kiểm tra email hợp lệ (nếu nhập)
        if (!ValidationUtil.isNullOrEmpty(email)) {
            email = email.trim();
            if (!ValidationUtil.isValidEmail(email)) {
                return "Định dạng email không hợp lệ.";
            }
            if (userDAO.findByEmail(email) != null) {
                return "Email này đã được sử dụng bởi tài khoản khác.";
            }
        } else {
            email = null;
        }

        // 3. Kiểm tra số điện thoại (nếu nhập)
        if (!ValidationUtil.isNullOrEmpty(phone)) {
            phone = phone.trim();
            if (!ValidationUtil.isValidPhone(phone)) {
                return "Số điện thoại không hợp lệ (phải gồm 10 số bắt đầu bằng 03,05,07,08,09).";
            }
        } else {
            phone = null;
        }

        // 4. Kiểm tra tên đăng nhập tồn tại
        if (userDAO.findByUsername(username) != null) {
            return "Tên đăng nhập đã được sử dụng.";
        }

        // 5. Tạo đối tượng User và mã hóa mật khẩu
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(PasswordUtil.hashPassword(password));
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPhone(phone);

        // 6. Thực thi ghi dữ liệu
        boolean success = userDAO.insert(newUser);
        if (success) {
            // Tự động thêm các danh mục mặc định cho User mới đăng ký
            categoryDAO.insertDefaultCategories(newUser.getUserId());
            return null; // Thành công
        }

        return "Có lỗi xảy ra trong quá trình đăng ký. Vui lòng thử lại sau.";
    }
}
