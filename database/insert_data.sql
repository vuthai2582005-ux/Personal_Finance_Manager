-- ============================================================
-- FILE: insert_data.sql
-- MÔ TẢ: Insert dữ liệu mẫu vào Database PersonalFinanceDB
--         gồm 5 người dùng, tài khoản, danh mục, giao dịch,
--         và ngân sách để kiểm thử ứng dụng.
-- TÁC GIẢ: PersonalFinanceManager Project
-- NGÀY TẠO: 2026-07-04
--
-- HƯỚNG DẪN: Chạy file create_database.sql TRƯỚC, sau đó
--             copy toàn bộ nội dung file này vào SSMS và
--             nhấn F5 để thực thi.
--
-- MẬT KHẨU MẪU: Tất cả users đều dùng mật khẩu "Password123!"
--   BCrypt hash đã được tính sẵn (cost factor = 10)
-- ============================================================

USE PersonalFinanceDB;
GO

PRINT '============================================================';
PRINT ' BẮT ĐẦU INSERT DỮ LIỆU MẪU...';
PRINT '============================================================';
GO

-- ============================================================
-- BƯỚC 1: INSERT 5 NGƯỜI DÙNG (Users)
-- ============================================================
-- Lưu ý: password_hash tương ứng với mật khẩu "Password123!"
--         được mã hóa bằng BCrypt (cost factor = 10)
-- ============================================================

INSERT INTO Users (username, password_hash, full_name, email, phone)
VALUES
    -- User 1: Admin / quản trị viên
    (
        N'nguyenvanA',
        N'$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        N'Nguyễn Văn An',
        N'nguyenvanan@email.com',
        N'0901234567'
    ),

    -- User 2
    (
        N'tranthiB',
        N'$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        N'Trần Thị Bình',
        N'tranthiB@email.com',
        N'0912345678'
    ),

    -- User 3
    (
        N'levanC',
        N'$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        N'Lê Văn Chính',
        N'levanchinh@email.com',
        N'0923456789'
    ),

    -- User 4
    (
        N'phamthiD',
        N'$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        N'Phạm Thị Diễm',
        N'phamthidiem@email.com',
        N'0934567890'
    ),

    -- User 5
    (
        N'hoangvanE',
        N'$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        N'Hoàng Văn Em',
        N'hoangvanem@email.com',
        N'0945678901'
    );

PRINT '>> Đã insert 5 người dùng.';
GO


-- ============================================================
-- BƯỚC 2: INSERT TÀI KHOẢN (Accounts)
-- Mỗi user có 2-3 tài khoản khác nhau
-- ============================================================

INSERT INTO Accounts (user_id, account_name, account_type, balance, currency, is_active)
VALUES
    -- ---- User 1: Nguyễn Văn An ----
    (1, N'Tiền mặt cá nhân',       N'CASH',        5000000.00,  N'VND', 1),
    (1, N'Tài khoản Vietcombank',   N'BANK',        25000000.00, N'VND', 1),
    (1, N'Ví MoMo',                N'E_WALLET',    1500000.00,  N'VND', 1),

    -- ---- User 2: Trần Thị Bình ----
    (2, N'Tiền mặt',               N'CASH',        2000000.00,  N'VND', 1),
    (2, N'Tài khoản Techcombank',   N'BANK',        18500000.00, N'VND', 1),
    (2, N'Thẻ tín dụng HSBC',      N'CREDIT_CARD', 0.00,        N'VND', 1),

    -- ---- User 3: Lê Văn Chính ----
    (3, N'Tiền mặt',               N'CASH',        3200000.00,  N'VND', 1),
    (3, N'Tài khoản BIDV',         N'BANK',        42000000.00, N'VND', 1),
    (3, N'Tiết kiệm BIDV',         N'SAVINGS',     50000000.00, N'VND', 1),

    -- ---- User 4: Phạm Thị Diễm ----
    (4, N'Tiền mặt',               N'CASH',        800000.00,   N'VND', 1),
    (4, N'Tài khoản MB Bank',       N'BANK',        9500000.00,  N'VND', 1),
    (4, N'Ví ZaloPay',             N'E_WALLET',    650000.00,   N'VND', 1),

    -- ---- User 5: Hoàng Văn Em ----
    (5, N'Tiền mặt',               N'CASH',        1200000.00,  N'VND', 1),
    (5, N'Tài khoản ACB',          N'BANK',        31000000.00, N'VND', 1);

PRINT '>> Đã insert 14 tài khoản (2-3 tài khoản/user).';
GO


-- ============================================================
-- BƯỚC 3: INSERT DANH MỤC (Categories)
-- Mỗi user có đầy đủ danh mục mặc định
-- ============================================================

-- ---- DANH MỤC USER 1 ----
INSERT INTO Categories (user_id, category_name, type, icon, color, is_default)
VALUES
    -- Chi tiêu (EXPENSE)
    (1, N'Ăn uống',          N'EXPENSE', N'fork-knife',    N'#FF6B6B', 1),
    (1, N'Di chuyển',        N'EXPENSE', N'car',           N'#FF8E53', 1),
    (1, N'Mua sắm',          N'EXPENSE', N'shopping-bag',  N'#FFA94D', 1),
    (1, N'Hóa đơn & Tiện ích',N'EXPENSE',N'zap',           N'#FFD93D', 1),
    (1, N'Giải trí',         N'EXPENSE', N'gamepad',       N'#6BCB77', 1),
    (1, N'Y tế',             N'EXPENSE', N'heart-pulse',   N'#4D96FF', 1),
    (1, N'Giáo dục',         N'EXPENSE', N'book-open',     N'#C77DFF', 1),
    (1, N'Chi tiêu khác',    N'EXPENSE', N'more-horizontal',N'#ADB5BD',1),
    -- Thu nhập (INCOME)
    (1, N'Lương',            N'INCOME',  N'briefcase',     N'#51CF66', 1),
    (1, N'Thưởng',           N'INCOME',  N'gift',          N'#339AF0', 1),
    (1, N'Đầu tư',           N'INCOME',  N'trending-up',   N'#F06595', 1),
    (1, N'Thu nhập khác',    N'INCOME',  N'plus-circle',   N'#74C0FC', 1);

-- ---- DANH MỤC USER 2 ----
INSERT INTO Categories (user_id, category_name, type, icon, color, is_default)
VALUES
    (2, N'Ăn uống',          N'EXPENSE', N'fork-knife',    N'#FF6B6B', 1),
    (2, N'Di chuyển',        N'EXPENSE', N'car',           N'#FF8E53', 1),
    (2, N'Mua sắm',          N'EXPENSE', N'shopping-bag',  N'#FFA94D', 1),
    (2, N'Hóa đơn & Tiện ích',N'EXPENSE',N'zap',           N'#FFD93D', 1),
    (2, N'Giải trí',         N'EXPENSE', N'gamepad',       N'#6BCB77', 1),
    (2, N'Y tế',             N'EXPENSE', N'heart-pulse',   N'#4D96FF', 1),
    (2, N'Giáo dục',         N'EXPENSE', N'book-open',     N'#C77DFF', 1),
    (2, N'Chi tiêu khác',    N'EXPENSE', N'more-horizontal',N'#ADB5BD',1),
    (2, N'Lương',            N'INCOME',  N'briefcase',     N'#51CF66', 1),
    (2, N'Thưởng',           N'INCOME',  N'gift',          N'#339AF0', 1),
    (2, N'Đầu tư',           N'INCOME',  N'trending-up',   N'#F06595', 1),
    (2, N'Thu nhập khác',    N'INCOME',  N'plus-circle',   N'#74C0FC', 1);

-- ---- DANH MỤC USER 3 ----
INSERT INTO Categories (user_id, category_name, type, icon, color, is_default)
VALUES
    (3, N'Ăn uống',          N'EXPENSE', N'fork-knife',    N'#FF6B6B', 1),
    (3, N'Di chuyển',        N'EXPENSE', N'car',           N'#FF8E53', 1),
    (3, N'Mua sắm',          N'EXPENSE', N'shopping-bag',  N'#FFA94D', 1),
    (3, N'Hóa đơn & Tiện ích',N'EXPENSE',N'zap',           N'#FFD93D', 1),
    (3, N'Giải trí',         N'EXPENSE', N'gamepad',       N'#6BCB77', 1),
    (3, N'Y tế',             N'EXPENSE', N'heart-pulse',   N'#4D96FF', 1),
    (3, N'Giáo dục',         N'EXPENSE', N'book-open',     N'#C77DFF', 1),
    (3, N'Chi tiêu khác',    N'EXPENSE', N'more-horizontal',N'#ADB5BD',1),
    (3, N'Lương',            N'INCOME',  N'briefcase',     N'#51CF66', 1),
    (3, N'Thưởng',           N'INCOME',  N'gift',          N'#339AF0', 1),
    (3, N'Đầu tư',           N'INCOME',  N'trending-up',   N'#F06595', 1),
    (3, N'Thu nhập khác',    N'INCOME',  N'plus-circle',   N'#74C0FC', 1);

-- ---- DANH MỤC USER 4 ----
INSERT INTO Categories (user_id, category_name, type, icon, color, is_default)
VALUES
    (4, N'Ăn uống',          N'EXPENSE', N'fork-knife',    N'#FF6B6B', 1),
    (4, N'Di chuyển',        N'EXPENSE', N'car',           N'#FF8E53', 1),
    (4, N'Mua sắm',          N'EXPENSE', N'shopping-bag',  N'#FFA94D', 1),
    (4, N'Hóa đơn & Tiện ích',N'EXPENSE',N'zap',           N'#FFD93D', 1),
    (4, N'Giải trí',         N'EXPENSE', N'gamepad',       N'#6BCB77', 1),
    (4, N'Y tế',             N'EXPENSE', N'heart-pulse',   N'#4D96FF', 1),
    (4, N'Giáo dục',         N'EXPENSE', N'book-open',     N'#C77DFF', 1),
    (4, N'Chi tiêu khác',    N'EXPENSE', N'more-horizontal',N'#ADB5BD',1),
    (4, N'Lương',            N'INCOME',  N'briefcase',     N'#51CF66', 1),
    (4, N'Thưởng',           N'INCOME',  N'gift',          N'#339AF0', 1),
    (4, N'Đầu tư',           N'INCOME',  N'trending-up',   N'#F06595', 1),
    (4, N'Thu nhập khác',    N'INCOME',  N'plus-circle',   N'#74C0FC', 1);

-- ---- DANH MỤC USER 5 ----
INSERT INTO Categories (user_id, category_name, type, icon, color, is_default)
VALUES
    (5, N'Ăn uống',          N'EXPENSE', N'fork-knife',    N'#FF6B6B', 1),
    (5, N'Di chuyển',        N'EXPENSE', N'car',           N'#FF8E53', 1),
    (5, N'Mua sắm',          N'EXPENSE', N'shopping-bag',  N'#FFA94D', 1),
    (5, N'Hóa đơn & Tiện ích',N'EXPENSE',N'zap',           N'#FFD93D', 1),
    (5, N'Giải trí',         N'EXPENSE', N'gamepad',       N'#6BCB77', 1),
    (5, N'Y tế',             N'EXPENSE', N'heart-pulse',   N'#4D96FF', 1),
    (5, N'Giáo dục',         N'EXPENSE', N'book-open',     N'#C77DFF', 1),
    (5, N'Chi tiêu khác',    N'EXPENSE', N'more-horizontal',N'#ADB5BD',1),
    (5, N'Lương',            N'INCOME',  N'briefcase',     N'#51CF66', 1),
    (5, N'Thưởng',           N'INCOME',  N'gift',          N'#339AF0', 1),
    (5, N'Đầu tư',           N'INCOME',  N'trending-up',   N'#F06595', 1),
    (5, N'Thu nhập khác',    N'INCOME',  N'plus-circle',   N'#74C0FC', 1);

PRINT '>> Đã insert danh mục cho 5 users (12 danh mục/user = 60 danh mục).';
GO


-- ============================================================
-- BƯỚC 4: INSERT GIAO DỊCH (Transactions)
-- Dữ liệu tháng 5, 6, 7 năm 2026
--
-- Mapping category_id (theo thứ tự insert):
--   User 1: cat 1-12   | User 2: cat 13-24
--   User 3: cat 25-36  | User 4: cat 37-48
--   User 5: cat 49-60
--
-- EXPENSE categories per user (offset = (user-1)*12):
--   +1=Ăn uống, +2=Di chuyển, +3=Mua sắm, +4=Hóa đơn
--   +5=Giải trí, +6=Y tế, +7=Giáo dục, +8=Chi tiêu khác
-- INCOME categories per user (offset = (user-1)*12):
--   +9=Lương, +10=Thưởng, +11=Đầu tư, +12=Thu nhập khác
--
-- Mapping account_id:
--   User1: acc 1(Cash),2(Bank),3(MoMo)
--   User2: acc 4(Cash),5(Bank),6(Credit)
--   User3: acc 7(Cash),8(Bank),9(Savings)
--   User4: acc 10(Cash),11(Bank),12(ZaloPay)
--   User5: acc 13(Cash),14(Bank)
-- ============================================================

-- ---- GIAO DỊCH USER 1 (account_id: 1,2,3 | category_id: 1-12) ----
INSERT INTO Transactions (account_id, category_id, type, amount, transaction_date, description)
VALUES
    -- Tháng 5/2026 - Thu nhập
    (2, 9,  N'INCOME',  15000000.00, N'2026-05-01', N'Lương tháng 5/2026'),
    (2, 10, N'INCOME',   2000000.00, N'2026-05-15', N'Thưởng KPI tháng 4'),
    -- Tháng 5/2026 - Chi tiêu
    (1, 1,  N'EXPENSE',   800000.00, N'2026-05-03', N'Ăn sáng + trưa cả tuần'),
    (2, 4,  N'EXPENSE',  1200000.00, N'2026-05-05', N'Tiền điện + nước tháng 5'),
    (1, 2,  N'EXPENSE',   450000.00, N'2026-05-08', N'Đổ xăng xe máy'),
    (3, 3,  N'EXPENSE',   650000.00, N'2026-05-10', N'Mua áo phông Uniqlo'),
    (1, 1,  N'EXPENSE',  1200000.00, N'2026-05-12', N'Ăn tối gia đình'),
    (2, 5,  N'EXPENSE',   320000.00, N'2026-05-18', N'Xem phim cùng bạn bè'),
    (1, 2,  N'EXPENSE',   280000.00, N'2026-05-20', N'Grab đi làm cả tuần'),
    (2, 6,  N'EXPENSE',   500000.00, N'2026-05-22', N'Khám sức khỏe định kỳ'),
    -- Tháng 6/2026 - Thu nhập
    (2, 9,  N'INCOME',  15000000.00, N'2026-06-01', N'Lương tháng 6/2026'),
    (2, 11, N'INCOME',   1500000.00, N'2026-06-20', N'Lợi nhuận cổ tức'),
    -- Tháng 6/2026 - Chi tiêu
    (1, 1,  N'EXPENSE',   950000.00, N'2026-06-02', N'Ăn uống cả tuần'),
    (2, 4,  N'EXPENSE',  1100000.00, N'2026-06-05', N'Hóa đơn internet + điện'),
    (3, 3,  N'EXPENSE',  2500000.00, N'2026-06-08', N'Mua giày Nike sale 50%'),
    (1, 2,  N'EXPENSE',   500000.00, N'2026-06-15', N'Đổ xăng + phí gửi xe'),
    (1, 7,  N'EXPENSE',   800000.00, N'2026-06-18', N'Mua sách lập trình Java'),
    (2, 5,  N'EXPENSE',   450000.00, N'2026-06-25', N'Vé concert nhạc'),
    -- Tháng 7/2026 - Thu nhập
    (2, 9,  N'INCOME',  15000000.00, N'2026-07-01', N'Lương tháng 7/2026'),
    -- Tháng 7/2026 - Chi tiêu
    (1, 1,  N'EXPENSE',  1100000.00, N'2026-07-01', N'Ăn uống đầu tháng'),
    (2, 4,  N'EXPENSE',  1300000.00, N'2026-07-03', N'Tiền điện tháng 7 tăng mạnh');

-- ---- GIAO DỊCH USER 2 (account_id: 4,5,6 | category_id: 13-24) ----
INSERT INTO Transactions (account_id, category_id, type, amount, transaction_date, description)
VALUES
    (5, 21, N'INCOME',  12000000.00, N'2026-05-01', N'Lương tháng 5'),
    (5, 22, N'INCOME',    500000.00, N'2026-05-10', N'Freelance thiết kế logo'),
    (4, 13, N'EXPENSE',   700000.00, N'2026-05-04', N'Đi ăn ngoài'),
    (4, 14, N'EXPENSE',   200000.00, N'2026-05-06', N'Xe ôm công nghệ'),
    (5, 16, N'EXPENSE',  1500000.00, N'2026-05-08', N'Tiền điện nước'),
    (4, 15, N'EXPENSE',  1200000.00, N'2026-05-15', N'Mua mỹ phẩm The Ordinary'),
    (5, 21, N'INCOME',  12000000.00, N'2026-06-01', N'Lương tháng 6'),
    (4, 13, N'EXPENSE',   850000.00, N'2026-06-05', N'Ăn uống tuần 1'),
    (5, 19, N'EXPENSE',   300000.00, N'2026-06-12', N'Rạp chiếu phim'),
    (4, 14, N'EXPENSE',   350000.00, N'2026-06-18', N'Đổ xăng xe'),
    (5, 21, N'INCOME',  12000000.00, N'2026-07-01', N'Lương tháng 7'),
    (4, 13, N'EXPENSE',   600000.00, N'2026-07-02', N'Ăn sáng cả tuần');

-- ---- GIAO DỊCH USER 3 (account_id: 7,8,9 | category_id: 25-36) ----
INSERT INTO Transactions (account_id, category_id, type, amount, transaction_date, description)
VALUES
    (8, 33, N'INCOME',  20000000.00, N'2026-05-01', N'Lương tháng 5 - Senior Dev'),
    (8, 34, N'INCOME',   5000000.00, N'2026-05-01', N'Thưởng dự án hoàn thành'),
    (8, 35, N'INCOME',   3000000.00, N'2026-05-20', N'Cổ tức quý 1'),
    (7, 25, N'EXPENSE',  1500000.00, N'2026-05-05', N'Ăn uống cả tháng (tuần 1+2)'),
    (8, 28, N'EXPENSE',  2500000.00, N'2026-05-06', N'Tiền điện nước internet'),
    (7, 26, N'EXPENSE',   800000.00, N'2026-05-10', N'Xăng xe + phí cầu đường'),
    (7, 31, N'EXPENSE',  3000000.00, N'2026-05-15', N'Học khóa AWS Cloud online'),
    (8, 33, N'INCOME',  20000000.00, N'2026-06-01', N'Lương tháng 6'),
    (7, 25, N'EXPENSE',  1800000.00, N'2026-06-03', N'Ăn uống đầu tháng 6'),
    (8, 29, N'EXPENSE',   600000.00, N'2026-06-10', N'Vé xem bóng đá'),
    (8, 35, N'INCOME',   2500000.00, N'2026-06-20', N'Lợi nhuận đầu tư chứng khoán'),
    (8, 33, N'INCOME',  20000000.00, N'2026-07-01', N'Lương tháng 7'),
    (7, 25, N'EXPENSE',  2000000.00, N'2026-07-01', N'Ăn uống đầu tháng 7');

-- ---- GIAO DỊCH USER 4 (account_id: 10,11,12 | category_id: 37-48) ----
INSERT INTO Transactions (account_id, category_id, type, amount, transaction_date, description)
VALUES
    (11, 45, N'INCOME',   8000000.00, N'2026-05-01', N'Lương tháng 5'),
    (10, 37, N'EXPENSE',   600000.00, N'2026-05-03', N'Chợ đầu tuần'),
    (10, 38, N'EXPENSE',   150000.00, N'2026-05-05', N'Xe buýt tháng 5'),
    (11, 40, N'EXPENSE',  1000000.00, N'2026-05-06', N'Hóa đơn tiện ích'),
    (12, 39, N'EXPENSE',   500000.00, N'2026-05-20', N'Sale online Shopee'),
    (11, 45, N'INCOME',   8000000.00, N'2026-06-01', N'Lương tháng 6'),
    (11, 46, N'INCOME',    500000.00, N'2026-06-05', N'Thưởng chuyên cần'),
    (10, 37, N'EXPENSE',   700000.00, N'2026-06-02', N'Ăn uống gia đình'),
    (10, 41, N'EXPENSE',   200000.00, N'2026-06-15', N'Mua thuốc'),
    (11, 45, N'INCOME',   8000000.00, N'2026-07-01', N'Lương tháng 7'),
    (10, 37, N'EXPENSE',   650000.00, N'2026-07-01', N'Chợ đầu tháng 7');

-- ---- GIAO DỊCH USER 5 (account_id: 13,14 | category_id: 49-60) ----
INSERT INTO Transactions (account_id, category_id, type, amount, transaction_date, description)
VALUES
    (14, 57, N'INCOME',  18000000.00, N'2026-05-01', N'Lương tháng 5'),
    (14, 59, N'INCOME',   4000000.00, N'2026-05-15', N'Cho thuê xe máy'),
    (13, 49, N'EXPENSE',  1200000.00, N'2026-05-05', N'Ăn uống cả tuần'),
    (13, 50, N'EXPENSE',   600000.00, N'2026-05-08', N'Xăng + bảo dưỡng xe'),
    (14, 52, N'EXPENSE',  3000000.00, N'2026-05-10', N'Tiền điện tháng 5'),
    (13, 51, N'EXPENSE',  2000000.00, N'2026-05-18', N'Mua đồ gia dụng'),
    (14, 57, N'INCOME',  18000000.00, N'2026-06-01', N'Lương tháng 6'),
    (14, 58, N'INCOME',   3000000.00, N'2026-06-01', N'Thưởng hiệu suất Q2'),
    (13, 49, N'EXPENSE',  1500000.00, N'2026-06-03', N'Ăn uống đầu tháng 6'),
    (14, 56, N'EXPENSE',   800000.00, N'2026-06-10', N'Tiền thuốc cả nhà'),
    (14, 53, N'EXPENSE',   500000.00, N'2026-06-20', N'Karaoke với đồng nghiệp'),
    (14, 57, N'INCOME',  18000000.00, N'2026-07-01', N'Lương tháng 7'),
    (13, 49, N'EXPENSE',  1300000.00, N'2026-07-02', N'Đi ăn đầu tháng 7');

PRINT '>> Đã insert giao dịch cho 5 users.';
GO


-- ============================================================
-- BƯỚC 5: INSERT NGÂN SÁCH (Budgets) - Tháng 7/2026
-- ============================================================

INSERT INTO Budgets (user_id, category_id, budget_amount, spent_amount, month, year)
VALUES
    -- ---- Ngân sách User 1 (tháng 7/2026) ----
    (1,  1, 3000000.00, 1100000.00, 7, 2026),   -- Ăn uống: hạn mức 3tr, đã chi 1.1tr
    (1,  2, 1000000.00,       0.00, 7, 2026),   -- Di chuyển: hạn mức 1tr
    (1,  3, 2000000.00,       0.00, 7, 2026),   -- Mua sắm: hạn mức 2tr
    (1,  4, 1500000.00, 1300000.00, 7, 2026),   -- Hóa đơn: hạn mức 1.5tr, đã chi 1.3tr

    -- ---- Ngân sách User 2 (tháng 7/2026) ----
    (2, 13, 2000000.00,  600000.00, 7, 2026),   -- Ăn uống: hạn mức 2tr, đã chi 600k
    (2, 14,  500000.00,       0.00, 7, 2026),   -- Di chuyển: hạn mức 500k
    (2, 16, 1500000.00,       0.00, 7, 2026),   -- Hóa đơn: hạn mức 1.5tr

    -- ---- Ngân sách User 3 (tháng 7/2026) ----
    (3, 25, 4000000.00, 2000000.00, 7, 2026),   -- Ăn uống: hạn mức 4tr, đã chi 2tr
    (3, 26, 1500000.00,       0.00, 7, 2026),   -- Di chuyển: hạn mức 1.5tr
    (3, 28, 3000000.00,       0.00, 7, 2026),   -- Hóa đơn: hạn mức 3tr
    (3, 31, 5000000.00,       0.00, 7, 2026),   -- Giáo dục: hạn mức 5tr

    -- ---- Ngân sách User 4 (tháng 7/2026) ----
    (4, 37, 2000000.00,  650000.00, 7, 2026),   -- Ăn uống: hạn mức 2tr, đã chi 650k
    (4, 40, 1200000.00,       0.00, 7, 2026),   -- Hóa đơn: hạn mức 1.2tr

    -- ---- Ngân sách User 5 (tháng 7/2026) ----
    (5, 49, 4000000.00, 1300000.00, 7, 2026),   -- Ăn uống: hạn mức 4tr, đã chi 1.3tr
    (5, 50, 1500000.00,       0.00, 7, 2026),   -- Di chuyển: hạn mức 1.5tr
    (5, 52, 3500000.00,       0.00, 7, 2026),   -- Hóa đơn: hạn mức 3.5tr
    (5, 51, 3000000.00,       0.00, 7, 2026);   -- Mua sắm: hạn mức 3tr

PRINT '>> Đã insert ngân sách tháng 7/2026 cho 5 users.';
GO


-- ============================================================
-- KIỂM TRA DỮ LIỆU SAU KHI INSERT
-- ============================================================

PRINT '';
PRINT '---- KIỂM TRA SỐ LƯỢNG BẢN GHI ----';

SELECT 'Users'        AS [Bảng], COUNT(*) AS [Số bản ghi] FROM Users
UNION ALL
SELECT 'Accounts',      COUNT(*) FROM Accounts
UNION ALL
SELECT 'Categories',    COUNT(*) FROM Categories
UNION ALL
SELECT 'Transactions',  COUNT(*) FROM Transactions
UNION ALL
SELECT 'Budgets',       COUNT(*) FROM Budgets;

PRINT '';
PRINT '---- DANH SÁCH NGƯỜI DÙNG ----';
SELECT
    user_id       AS [ID],
    username      AS [Tên đăng nhập],
    full_name     AS [Họ tên],
    email         AS [Email],
    phone         AS [SĐT],
    FORMAT(created_at, N'dd/MM/yyyy') AS [Ngày tạo]
FROM Users
ORDER BY user_id;

PRINT '';
PRINT '---- SỐ DƯ TÀI KHOẢN THEO USER ----';
SELECT
    u.user_id        AS [ID],
    u.full_name      AS [Họ tên],
    a.account_name   AS [Tài khoản],
    a.account_type   AS [Loại],
    FORMAT(a.balance, N'N0') + N' ₫' AS [Số dư]
FROM Users u
INNER JOIN Accounts a ON u.user_id = a.user_id
ORDER BY u.user_id, a.account_id;
GO


-- ============================================================
-- HOÀN THÀNH
-- ============================================================

PRINT '';
PRINT '============================================================';
PRINT ' INSERT DỮ LIỆU MẪU HOÀN THÀNH THÀNH CÔNG!';
PRINT '------------------------------------------------------------';
PRINT ' Tài khoản đăng nhập mẫu:';
PRINT '   Username: nguyenvanA  | Password: Password123!';
PRINT '   Username: tranthiB    | Password: Password123!';
PRINT '   Username: levanC      | Password: Password123!';
PRINT '   Username: phamthiD    | Password: Password123!';
PRINT '   Username: hoangvanE   | Password: Password123!';
PRINT '';
PRINT ' LƯU Ý: Password trong DB là BCrypt hash.';
PRINT '   Khi đăng nhập, ứng dụng Java sẽ dùng BCrypt.checkpw()';
PRINT '   để so sánh mật khẩu nhập vào với hash trong DB.';
PRINT '============================================================';
GO
