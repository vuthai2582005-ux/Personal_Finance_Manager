-- ============================================================
-- FILE: create_database.sql
-- MÔ TẢ: Tạo toàn bộ cấu trúc Database cho ứng dụng
--         Quản Lý Tài Chính Cá Nhân (Personal Finance Manager)
-- TÁC GIẢ: PersonalFinanceManager Project
-- NGÀY TẠO: 2026-07-04
--
-- HƯỚNG DẪN: Copy toàn bộ nội dung file này vào SQL Server
--             Management Studio (SSMS) và nhấn F5 để thực thi.
-- ============================================================


-- ============================================================
-- BƯỚC 1: TẠO DATABASE
-- ============================================================

USE master;
GO

-- Xóa database cũ nếu đã tồn tại (dùng khi muốn tạo lại từ đầu)
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'PersonalFinanceDB')
BEGIN
    ALTER DATABASE PersonalFinanceDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE PersonalFinanceDB;
    PRINT '>> Database cũ đã được xóa.';
END
GO

CREATE DATABASE PersonalFinanceDB
    COLLATE Vietnamese_CI_AS;
GO

PRINT '>> Database PersonalFinanceDB đã được tạo thành công.';
GO

USE PersonalFinanceDB;
GO


-- ============================================================
-- BƯỚC 2: TẠO CÁC BẢNG
-- ============================================================

-- ----------------------------------------------------------
-- BẢNG: Users — Thông tin người dùng
-- ----------------------------------------------------------
CREATE TABLE Users (
    user_id       INT           IDENTITY(1,1)  NOT NULL,
    username      NVARCHAR(50)                 NOT NULL,
    password_hash NVARCHAR(255)                NOT NULL,  -- BCrypt hash
    full_name     NVARCHAR(100)                NOT NULL,
    email         NVARCHAR(100)                NULL,
    phone         NVARCHAR(15)                 NULL,
    created_at    DATETIME      DEFAULT GETDATE()         NOT NULL,
    updated_at    DATETIME      DEFAULT GETDATE()         NOT NULL,

    -- Khóa chính
    CONSTRAINT PK_Users PRIMARY KEY (user_id),

    -- Ràng buộc duy nhất
    CONSTRAINT UQ_Users_Username UNIQUE (username),
    CONSTRAINT UQ_Users_Email    UNIQUE (email)
);
GO

PRINT '>> Bảng Users đã được tạo.';
GO


-- ----------------------------------------------------------
-- BẢNG: Accounts — Tài khoản tài chính của người dùng
-- ----------------------------------------------------------
CREATE TABLE Accounts (
    account_id   INT           IDENTITY(1,1)  NOT NULL,
    user_id      INT                          NOT NULL,
    account_name NVARCHAR(100)                NOT NULL,
    account_type NVARCHAR(20)                 NOT NULL,  -- CASH, BANK, E_WALLET, CREDIT_CARD, SAVINGS
    balance      DECIMAL(18,2) DEFAULT 0      NOT NULL,
    currency     NVARCHAR(10)  DEFAULT N'VND' NOT NULL,
    is_active    BIT           DEFAULT 1      NOT NULL,
    created_at   DATETIME      DEFAULT GETDATE()         NOT NULL,

    -- Khóa chính
    CONSTRAINT PK_Accounts PRIMARY KEY (account_id),

    -- Khóa ngoại
    CONSTRAINT FK_Accounts_Users FOREIGN KEY (user_id)
        REFERENCES Users(user_id)
        ON DELETE CASCADE,

    -- Ràng buộc giá trị hợp lệ
    CONSTRAINT CK_Accounts_Type CHECK (
        account_type IN (N'CASH', N'BANK', N'E_WALLET', N'CREDIT_CARD', N'SAVINGS')
    ),

    CONSTRAINT CK_Accounts_Balance CHECK (balance >= 0)
);
GO

PRINT '>> Bảng Accounts đã được tạo.';
GO


-- ----------------------------------------------------------
-- BẢNG: Categories — Danh mục thu / chi
-- ----------------------------------------------------------
CREATE TABLE Categories (
    category_id   INT          IDENTITY(1,1)  NOT NULL,
    user_id       INT                         NOT NULL,
    category_name NVARCHAR(100)               NOT NULL,
    type          NVARCHAR(10)                NOT NULL,   -- INCOME hoặc EXPENSE
    icon          NVARCHAR(50)                NULL,
    color         NVARCHAR(7)                 NULL,       -- Mã hex, ví dụ: #FF5733
    is_default    BIT          DEFAULT 0      NOT NULL,

    -- Khóa chính
    CONSTRAINT PK_Categories PRIMARY KEY (category_id),

    -- Khóa ngoại
    CONSTRAINT FK_Categories_Users FOREIGN KEY (user_id)
        REFERENCES Users(user_id)
        ON DELETE CASCADE,

    -- Ràng buộc giá trị hợp lệ
    CONSTRAINT CK_Categories_Type CHECK (type IN (N'INCOME', N'EXPENSE'))
);
GO

PRINT '>> Bảng Categories đã được tạo.';
GO


-- ----------------------------------------------------------
-- BẢNG: Transactions — Giao dịch thu / chi
-- ----------------------------------------------------------
CREATE TABLE Transactions (
    transaction_id   INT           IDENTITY(1,1)  NOT NULL,
    account_id       INT                          NOT NULL,
    category_id      INT                          NOT NULL,
    type             NVARCHAR(10)                 NOT NULL,  -- INCOME hoặc EXPENSE
    amount           DECIMAL(18,2)                NOT NULL,
    transaction_date DATE                         NOT NULL,
    description      NVARCHAR(255)                NULL,
    created_at       DATETIME      DEFAULT GETDATE()         NOT NULL,

    -- Khóa chính
    CONSTRAINT PK_Transactions PRIMARY KEY (transaction_id),

    -- Khóa ngoại
    CONSTRAINT FK_Transactions_Accounts FOREIGN KEY (account_id)
        REFERENCES Accounts(account_id),

    CONSTRAINT FK_Transactions_Categories FOREIGN KEY (category_id)
        REFERENCES Categories(category_id),

    -- Ràng buộc giá trị hợp lệ
    CONSTRAINT CK_Transactions_Type   CHECK (type IN (N'INCOME', N'EXPENSE')),
    CONSTRAINT CK_Transactions_Amount CHECK (amount > 0)
);
GO

PRINT '>> Bảng Transactions đã được tạo.';
GO


-- ----------------------------------------------------------
-- BẢNG: Budgets — Ngân sách theo danh mục và tháng
-- ----------------------------------------------------------
CREATE TABLE Budgets (
    budget_id     INT           IDENTITY(1,1)  NOT NULL,
    user_id       INT                          NOT NULL,
    category_id   INT                          NOT NULL,
    budget_amount DECIMAL(18,2)                NOT NULL,
    spent_amount  DECIMAL(18,2) DEFAULT 0      NOT NULL,
    month         INT                          NOT NULL,
    year          INT                          NOT NULL,
    created_at    DATETIME      DEFAULT GETDATE()         NOT NULL,

    -- Khóa chính
    CONSTRAINT PK_Budgets PRIMARY KEY (budget_id),

    -- Khóa ngoại
    CONSTRAINT FK_Budgets_Users FOREIGN KEY (user_id)
        REFERENCES Users(user_id)
        ON DELETE CASCADE,

    CONSTRAINT FK_Budgets_Categories FOREIGN KEY (category_id)
        REFERENCES Categories(category_id),

    -- Ràng buộc giá trị hợp lệ
    CONSTRAINT CK_Budgets_Amount CHECK (budget_amount > 0),
    CONSTRAINT CK_Budgets_Spent  CHECK (spent_amount >= 0),
    CONSTRAINT CK_Budgets_Month  CHECK (month BETWEEN 1 AND 12),
    CONSTRAINT CK_Budgets_Year   CHECK (year > 2000),

    -- Mỗi danh mục chỉ có 1 ngân sách mỗi tháng
    CONSTRAINT UQ_Budgets_UserCategoryMonth UNIQUE (user_id, category_id, month, year)
);
GO

PRINT '>> Bảng Budgets đã được tạo.';
GO


-- ============================================================
-- BƯỚC 3: TẠO INDEXES ĐỂ TỐI ƯU HIỆU SUẤT TRUY VẤN
-- ============================================================

-- Index cho Accounts
CREATE NONCLUSTERED INDEX IX_Accounts_UserId
    ON Accounts(user_id);

-- Index cho Categories
CREATE NONCLUSTERED INDEX IX_Categories_UserId
    ON Categories(user_id);

CREATE NONCLUSTERED INDEX IX_Categories_Type
    ON Categories(type);

-- Index cho Transactions
CREATE NONCLUSTERED INDEX IX_Transactions_AccountId
    ON Transactions(account_id);

CREATE NONCLUSTERED INDEX IX_Transactions_CategoryId
    ON Transactions(category_id);

CREATE NONCLUSTERED INDEX IX_Transactions_Date
    ON Transactions(transaction_date DESC);

CREATE NONCLUSTERED INDEX IX_Transactions_Type
    ON Transactions(type);

-- Index cho Budgets
CREATE NONCLUSTERED INDEX IX_Budgets_UserId
    ON Budgets(user_id);

CREATE NONCLUSTERED INDEX IX_Budgets_MonthYear
    ON Budgets(year DESC, month DESC);

PRINT '>> Các Index đã được tạo.';
GO


-- ============================================================
-- BƯỚC 4: TẠO CÁC VIEW HỮU ÍCH
-- ============================================================

-- View: Thông tin giao dịch đầy đủ (kèm tên tài khoản & danh mục)
CREATE VIEW vw_TransactionDetails AS
SELECT
    t.transaction_id,
    t.type,
    t.amount,
    t.transaction_date,
    t.description,
    t.created_at,
    a.account_id,
    a.account_name,
    a.account_type,
    a.user_id,
    c.category_id,
    c.category_name,
    c.color   AS category_color,
    c.icon    AS category_icon
FROM Transactions t
INNER JOIN Accounts   a ON t.account_id  = a.account_id
INNER JOIN Categories c ON t.category_id = c.category_id;
GO

-- View: Tổng thu/chi mỗi tháng theo từng user
CREATE VIEW vw_MonthlyReport AS
SELECT
    a.user_id,
    t.type,
    YEAR(t.transaction_date)  AS year,
    MONTH(t.transaction_date) AS month,
    SUM(t.amount)             AS total_amount,
    COUNT(t.transaction_id)   AS transaction_count
FROM Transactions t
INNER JOIN Accounts a ON t.account_id = a.account_id
GROUP BY a.user_id, t.type, YEAR(t.transaction_date), MONTH(t.transaction_date);
GO

-- View: Tình trạng ngân sách (budget + spent + % sử dụng)
CREATE VIEW vw_BudgetStatus AS
SELECT
    b.budget_id,
    b.user_id,
    b.month,
    b.year,
    b.budget_amount,
    b.spent_amount,
    (b.budget_amount - b.spent_amount)              AS remaining_amount,
    CASE
        WHEN b.budget_amount = 0 THEN 0
        ELSE CAST(b.spent_amount * 100.0 / b.budget_amount AS DECIMAL(5,2))
    END                                             AS usage_percent,
    c.category_name,
    c.color  AS category_color,
    c.icon   AS category_icon
FROM Budgets b
INNER JOIN Categories c ON b.category_id = c.category_id;
GO

PRINT '>> Các View đã được tạo.';
GO


-- ============================================================
-- HOÀN THÀNH
-- ============================================================

PRINT '';
PRINT '============================================================';
PRINT ' CẤU TRÚC DATABASE PersonalFinanceDB ĐÃ ĐƯỢC TẠO THÀNH CÔNG';
PRINT '------------------------------------------------------------';
PRINT ' Các bảng đã tạo:';
PRINT '   - Users          (Người dùng)';
PRINT '   - Accounts       (Tài khoản tài chính)';
PRINT '   - Categories     (Danh mục thu/chi)';
PRINT '   - Transactions   (Giao dịch)';
PRINT '   - Budgets        (Ngân sách)';
PRINT ' Các View đã tạo:';
PRINT '   - vw_TransactionDetails';
PRINT '   - vw_MonthlyReport';
PRINT '   - vw_BudgetStatus';
PRINT '============================================================';
GO
