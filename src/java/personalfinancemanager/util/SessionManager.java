package personalfinancemanager.util;

import personalfinancemanager.model.User;

/**
 * Singleton quản lý phiên đăng nhập hiện tại của người dùng trong bộ nhớ.
 */
public class SessionManager {
    private static SessionManager instance;
    private final ThreadLocal<User> currentUserThreadLocal = new ThreadLocal<>();

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUserThreadLocal.get();
    }

    public void setCurrentUser(User currentUser) {
        currentUserThreadLocal.set(currentUser);
    }

    public boolean isLoggedIn() {
        return currentUserThreadLocal.get() != null;
    }

    public void logout() {
        currentUserThreadLocal.remove();
    }

    public void clear() {
        currentUserThreadLocal.remove();
    }
}
