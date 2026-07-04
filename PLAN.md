# Kế Hoạch Dự Án: Quản Lý Tài Chính Cá Nhân

Xây dựng ứng dụng **Quản Lý Tài Chính Cá Nhân** (Personal Finance Manager) sử dụng **Java Swing** (giao diện đồ họa), mô hình **MVC**, kết nối **SQL Server** qua JDBC. Build tool: **Apache Ant**. Giao diện hoàn toàn bằng **Tiếng Việt**.

---

## 1. Tổng Quan Chức Năng

| # | Module                          | Mô tả                                                         |
| - | ------------------------------- | --------------------------------------------------------------- |
| 1 | **Đăng nhập / Đăng ký**   | Xác thực người dùng, mã hóa mật khẩu (BCrypt)          |
| 2 | **Quản lý Danh mục**     | CRUD danh mục thu/chi (Ăn uống, Lương, Đầu tư…)        |
| 3 | **Quản lý Giao dịch**    | Thêm/sửa/xóa giao dịch thu nhập & chi tiêu                |
| 4 | **Quản lý Ngân sách**   | Đặt ngân sách theo danh mục, cảnh báo vượt ngân sách |
| 5 | **Báo cáo & Thống kê**  | Bảng thống kê thu/chi theo tháng, theo danh mục (dạng bảng) |
| 6 | **Quản lý Tài khoản**   | Nhiều tài khoản (Tiền mặt, Ngân hàng, Ví điện tử)    |
| 7 | **Hồ sơ người dùng**   | Cập nhật thông tin cá nhân, đổi mật khẩu               |

---

## 2. Thiết Kế Database (SQL Server)

> **📁 Script đã có sẵn trong thư mục `database/`:**
> - `database/create_database.sql` — Tạo DB, bảng, indexes, views
> - `database/insert_data.sql` — Seed data mặc định (danh mục, dữ liệu mẫu)
>
> **Không cần tạo lại script. Chạy 2 file trên theo thứ tự trong SSMS là xong.**

### 2.1. Sơ Đồ Quan Hệ Thực Thể (ERD)

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────────┐
│    Users     │       │    Accounts      │       │  Transactions    │
├──────────────┤       ├──────────────────┤       ├──────────────────┤
│ PK user_id   │──┐    │ PK account_id    │──┐    │ PK transaction_id│
│    username   │  │    │ FK user_id       │  │    │ FK account_id    │
│    password   │  ├───>│    account_name  │  ├───>│ FK category_id   │
│    full_name  │  │    │    account_type  │  │    │    type          │
│    email      │  │    │    balance       │  │    │    amount        │
│    phone      │  │    │    currency      │  │    │    transaction_  │
│    created_at │  │    │    is_active     │  │    │      date        │
│    updated_at │  │    │    created_at    │  │    │    description   │
└──────────────┘  │    └──────────────────┘  │    │    created_at    │
                  │                          │    └──────────────────┘
                  │    ┌──────────────────┐  │
                  │    │   Categories     │  │
                  │    ├──────────────────┤  │
                  │    │ PK category_id   │──┘
                  ├───>│ FK user_id       │
                  │    │    category_name │
                  │    │    type          │
                  │    │    icon          │
                  │    │    color         │
                  │    │    is_default    │
                  │    └──────────────────┘
                  │
                  │    ┌──────────────────┐
                  │    │    Budgets       │
                  │    ├──────────────────┤
                  │    │ PK budget_id     │
                  └───>│ FK user_id       │
                       │ FK category_id   │───> Categories.category_id
                       │    budget_amount │
                       │    spent_amount  │
                       │    month         │
                       │    year          │
                       │    created_at    │
                       └──────────────────┘
```

**Quan hệ:**

- `Users` 1 ──── N `Accounts`
- `Users` 1 ──── N `Categories`
- `Users` 1 ──── N `Budgets`
- `Accounts` 1 ──── N `Transactions`
- `Categories` 1 ──── N `Transactions`
- `Categories` 1 ──── N `Budgets`

**Views hữu ích (đã có trong script):**
- `vw_TransactionDetails` — Giao dịch kèm tên tài khoản & danh mục
- `vw_MonthlyReport` — Tổng thu/chi theo tháng, theo user
- `vw_BudgetStatus` — Tình trạng ngân sách (% sử dụng)

---

### 2.2. Chi Tiết Các Bảng

#### Bảng `Users` — Người dùng

| Cột              | Kiểu                 | Ràng buộc           | Mô tả                           |
| ----------------- | --------------------- | --------------------- | --------------------------------- |
| `user_id`       | `INT IDENTITY(1,1)` | `PRIMARY KEY`       | Mã người dùng                 |
| `username`      | `NVARCHAR(50)`      | `UNIQUE, NOT NULL`  | Tên đăng nhập                 |
| `password_hash` | `NVARCHAR(255)`     | `NOT NULL`          | Mật khẩu đã mã hóa (BCrypt) |
| `full_name`     | `NVARCHAR(100)`     | `NOT NULL`          | Họ tên đầy đủ               |
| `email`         | `NVARCHAR(100)`     | `UNIQUE`            | Email                             |
| `phone`         | `NVARCHAR(15)`      |                       | Số điện thoại                 |
| `created_at`    | `DATETIME`          | `DEFAULT GETDATE()` | Ngày tạo                        |
| `updated_at`    | `DATETIME`          | `DEFAULT GETDATE()` | Ngày cập nhật                  |

#### Bảng `Accounts` — Tài khoản tài chính

| Cột             | Kiểu                 | Ràng buộc                                                          | Mô tả             |
| ---------------- | --------------------- | -------------------------------------------------------------------- | ------------------- |
| `account_id`   | `INT IDENTITY(1,1)` | `PRIMARY KEY`                                                      | Mã tài khoản     |
| `user_id`      | `INT`               | `FOREIGN KEY → Users(user_id)`                                    | Chủ sở hữu       |
| `account_name` | `NVARCHAR(100)`     | `NOT NULL`                                                         | Tên tài khoản    |
| `account_type` | `NVARCHAR(20)`      | `CHECK IN ('CASH','BANK','E_WALLET','CREDIT_CARD','SAVINGS')`      | Loại tài khoản   |
| `balance`      | `DECIMAL(18,2)`     | `DEFAULT 0`                                                        | Số dư hiện tại  |
| `currency`     | `NVARCHAR(10)`      | `DEFAULT 'VND'`                                                    | Đơn vị tiền tệ |
| `is_active`    | `BIT`               | `DEFAULT 1`                                                        | Còn hoạt động?  |
| `created_at`   | `DATETIME`          | `DEFAULT GETDATE()`                                                | Ngày tạo          |

#### Bảng `Categories` — Danh mục thu/chi

| Cột              | Kiểu                 | Ràng buộc                       | Mô tả                |
| ----------------- | --------------------- | --------------------------------- | ---------------------- |
| `category_id`   | `INT IDENTITY(1,1)` | `PRIMARY KEY`                   | Mã danh mục          |
| `user_id`       | `INT`               | `FOREIGN KEY → Users(user_id)` | Người tạo           |
| `category_name` | `NVARCHAR(100)`     | `NOT NULL`                      | Tên danh mục         |
| `type`          | `NVARCHAR(10)`      | `CHECK IN ('INCOME','EXPENSE')` | Thu nhập / Chi tiêu  |
| `icon`          | `NVARCHAR(50)`      |                                   | Icon đại diện       |
| `color`         | `NVARCHAR(7)`       |                                   | Mã màu hex (#FF5733) |
| `is_default`    | `BIT`               | `DEFAULT 0`                     | Danh mục mặc định? |

#### Bảng `Transactions` — Giao dịch

| Cột                 | Kiểu                 | Ràng buộc                                | Mô tả          |
| -------------------- | --------------------- | ------------------------------------------ | ---------------- |
| `transaction_id`   | `INT IDENTITY(1,1)` | `PRIMARY KEY`                            | Mã giao dịch   |
| `account_id`       | `INT`               | `FOREIGN KEY → Accounts(account_id)`    | Tài khoản      |
| `category_id`      | `INT`               | `FOREIGN KEY → Categories(category_id)` | Danh mục        |
| `type`             | `NVARCHAR(10)`      | `CHECK IN ('INCOME','EXPENSE')`          | Loại giao dịch |
| `amount`           | `DECIMAL(18,2)`     | `NOT NULL, CHECK > 0`                    | Số tiền        |
| `transaction_date` | `DATE`              | `NOT NULL`                               | Ngày giao dịch |
| `description`      | `NVARCHAR(255)`     |                                            | Ghi chú         |
| `created_at`       | `DATETIME`          | `DEFAULT GETDATE()`                      | Ngày tạo       |

#### Bảng `Budgets` — Ngân sách

| Cột              | Kiểu                 | Ràng buộc                                | Mô tả             |
| ----------------- | --------------------- | ------------------------------------------ | ------------------- |
| `budget_id`     | `INT IDENTITY(1,1)` | `PRIMARY KEY`                            | Mã ngân sách     |
| `user_id`       | `INT`               | `FOREIGN KEY → Users(user_id)`          | Chủ sở hữu       |
| `category_id`   | `INT`               | `FOREIGN KEY → Categories(category_id)` | Danh mục áp dụng |
| `budget_amount` | `DECIMAL(18,2)`     | `NOT NULL, CHECK > 0`                    | Hạn mức           |
| `spent_amount`  | `DECIMAL(18,2)`     | `DEFAULT 0`                              | Đã chi tiêu      |
| `month`         | `INT`               | `CHECK (month BETWEEN 1 AND 12)`         | Tháng              |
| `year`          | `INT`               | `CHECK (year > 2000)`                    | Năm                |
| `created_at`    | `DATETIME`          | `DEFAULT GETDATE()`                      | Ngày tạo          |

> **Ràng buộc đặc biệt:** `UNIQUE(user_id, category_id, month, year)` — Mỗi danh mục chỉ có 1 ngân sách mỗi tháng.

---

## 3. Cấu Trúc Thư Mục Dự Án (Ant + NetBeans)

```
Personal_Finance_Manager/
├── build.xml                              ← Apache Ant build script
├── PLAN.md                                ← Tài liệu này
│
├── database/                              ← ✅ Script SQL đã có sẵn
│   ├── create_database.sql               ← Tạo DB + bảng + indexes + views
│   └── insert_data.sql                   ← Seed data mặc định
│
├── src/
│   └── personalfinancemanager/
│       ├── Main.java                      ← Entry point
│       │
│       ├── config/                        ← Cấu hình
│       │   ├── DatabaseConnection.java   ← Singleton kết nối SQL Server
│       │   └── AppConstants.java         ← Hằng số ứng dụng
│       │
│       ├── model/                         ← MODEL — POJO / Entity
│       │   ├── User.java
│       │   ├── Account.java
│       │   ├── Category.java
│       │   ├── Transaction.java
│       │   └── Budget.java
│       │
│       ├── dao/                           ← Data Access Object (truy vấn DB)
│       │   ├── BaseDAO.java              ← Abstract base class
│       │   ├── UserDAO.java
│       │   ├── AccountDAO.java
│       │   ├── CategoryDAO.java
│       │   ├── TransactionDAO.java
│       │   └── BudgetDAO.java
│       │
│       ├── controller/                    ← CONTROLLER — Xử lý logic nghiệp vụ
│       │   ├── AuthController.java       ← Đăng nhập / Đăng ký
│       │   ├── AccountController.java
│       │   ├── CategoryController.java
│       │   ├── TransactionController.java
│       │   ├── BudgetController.java
│       │   └── ReportController.java     ← Thống kê, báo cáo
│       │
│       ├── view/                          ← VIEW — Giao diện Swing (Tiếng Việt)
│       │   ├── auth/
│       │   │   ├── LoginView.java
│       │   │   └── RegisterView.java
│       │   ├── main/
│       │   │   ├── MainFrame.java        ← JFrame chính (navigation)
│       │   │   └── DashboardPanel.java   ← Trang tổng quan
│       │   ├── account/
│       │   │   ├── AccountListPanel.java
│       │   │   └── AccountFormDialog.java
│       │   ├── category/
│       │   │   ├── CategoryListPanel.java
│       │   │   └── CategoryFormDialog.java
│       │   ├── transaction/
│       │   │   ├── TransactionListPanel.java
│       │   │   ├── TransactionFormDialog.java
│       │   │   └── TransactionFilterPanel.java
│       │   ├── budget/
│       │   │   ├── BudgetListPanel.java
│       │   │   └── BudgetFormDialog.java
│       │   ├── report/
│       │   │   └── ReportPanel.java      ← Bảng thống kê (không dùng biểu đồ)
│       │   └── components/               ← Shared UI components
│       │       ├── RoundedButton.java
│       │       ├── PlaceholderTextField.java
│       │       ├── SidebarPanel.java
│       │       └── CardPanel.java
│       │
│       └── util/                          ← Tiện ích
│           ├── PasswordUtil.java          ← Mã hóa BCrypt
│           ├── CurrencyFormatter.java    ← Format tiền tệ VND
│           ├── DateUtil.java             ← Xử lý ngày tháng
│           ├── ValidationUtil.java       ← Validate input
│           └── SessionManager.java       ← Quản lý phiên đăng nhập
│
├── resources/
│   └── icons/                             ← Icon cho UI
│
└── lib/                                   ← Thư viện JAR
    ├── mssql-jdbc-12.x.x.jre17.jar       ← Microsoft JDBC Driver
    └── jbcrypt-0.4.jar                    ← BCrypt library
```

---

## 4. Chi Tiết Từng Layer

### 4.1. CONFIG — Kết Nối Database

#### `DatabaseConnection.java`

```java
/**
 * Singleton Pattern — Quản lý kết nối SQL Server
 *
 * Connection String:
 *   jdbc:sqlserver://localhost:1433;
 *   databaseName=PersonalFinanceDB;
 *   encrypt=false;
 *   trustServerCertificate=true
 *
 * Sử dụng try-with-resources cho mọi Connection
 */
```

- `getConnection()` → trả về `java.sql.Connection`
- Tạo mới connection mỗi lần gọi, đóng sau khi dùng (try-with-resources)
- Thông tin kết nối (server, user, password) lấy từ `AppConstants.java`

#### `AppConstants.java`

```java
/**
 * Hằng số toàn ứng dụng
 *
 * - DB_SERVER   = "localhost"         → SQL Server host
 * - DB_PORT     = 1433                → Port mặc định
 * - DB_NAME     = "PersonalFinanceDB" → Tên database
 * - DB_USER     = "..."               → SQL Auth username
 * - DB_PASSWORD = "..."               → SQL Auth password
 *
 * - WINDOW_WIDTH  = 1200              → Kích thước cửa sổ
 * - WINDOW_HEIGHT = 750
 * - FONT_FAMILY   = "Segoe UI"       → Font chữ
 * - CURRENCY_CODE = "VND"            → Đơn vị tiền tệ
 * - DATE_FORMAT   = "dd/MM/yyyy"     → Định dạng ngày
 */
```

---

### 4.2. MODEL — Các Entity (POJO)

Mỗi class Model bao gồm:

- Private fields tương ứng các cột trong bảng DB
- Constructor mặc định + constructor đầy đủ
- Getter / Setter cho tất cả fields
- `toString()` override

| File                 | Fields chính                                                                               |
| -------------------- | ------------------------------------------------------------------------------------------- |
| `User.java`        | userId, username, passwordHash, fullName, email, phone, createdAt, updatedAt                |
| `Account.java`     | accountId, userId, accountName, accountType, balance, currency, isActive, createdAt         |
| `Category.java`    | categoryId, userId, categoryName, type (INCOME/EXPENSE), icon, color, isDefault             |
| `Transaction.java` | transactionId, accountId, categoryId, type, amount, transactionDate, description, createdAt |
| `Budget.java`      | budgetId, userId, categoryId, budgetAmount, spentAmount, month, year, createdAt             |

---

### 4.3. DAO — Data Access Object

#### `BaseDAO.java` (Abstract)

```java
/**
 * Lớp trừu tượng cơ sở cho tất cả DAO
 *
 * Methods:
 *   - getConnection()                                       → Lấy Connection từ DatabaseConnection
 *   - closeResources(Connection, Statement, ResultSet)      → Đóng tài nguyên an toàn
 */
```

#### Các DAO cụ thể

| DAO                | Methods chính                                                                                                                                                                                                                                        |
| ------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `UserDAO`        | `findByUsername(String)`, `findByEmail(String)`, `findById(int)`, `insert(User)`, `update(User)`, `authenticate(String, String)`                                                                                                         |
| `AccountDAO`     | `findByUserId(int)`, `findById(int)`, `insert(Account)`, `update(Account)`, `delete(int)`, `updateBalance(int, BigDecimal)`                                                                                                              |
| `CategoryDAO`    | `findByUserId(int)`, `findByType(int, String)`, `findById(int)`, `insert(Category)`, `update(Category)`, `delete(int)`, `insertDefaults(int)`                                                                                           |
| `TransactionDAO` | `findByAccountId(int)`, `findByDateRange(int, Date, Date)`, `findByCategory(int)`, `insert(Transaction)`, `update(Transaction)`, `delete(int)`, `getSumByType(int, String, int, int)`                                                   |
| `BudgetDAO`      | `findByUserAndMonth(int, int, int)`, `findByCategoryAndMonth(int, int, int)`, `insert(Budget)`, `update(Budget)`, `delete(int)`, `updateSpentAmount(int, BigDecimal)`                                                                   |

> **Bắt buộc:** Tất cả DAO phải dùng `PreparedStatement` — tuyệt đối **không** dùng string concatenation SQL (chống SQL Injection).

---

### 4.4. CONTROLLER — Business Logic

| Controller                | Trách nhiệm                                                                                                                                   |
| ------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| `AuthController`        | Validate đầu vào đăng nhập/đăng ký, hash mật khẩu BCrypt, tạo tài khoản mới, tự động tạo danh mục mặc định cho user mới |
| `AccountController`     | CRUD tài khoản, tính tổng tài sản (sum balance), kiểm tra trùng tên tài khoản                                                        |
| `CategoryController`    | CRUD danh mục, lọc theo loại (INCOME/EXPENSE), ngăn xóa danh mục đang có giao dịch                                                     |
| `TransactionController` | CRUD giao dịch, **tự động cập nhật số dư Account**, **tự động cập nhật spent_amount Budget**                           |
| `BudgetController`      | CRUD ngân sách, kiểm tra vượt ngân sách, tính phần trăm sử dụng                                                                     |
| `ReportController`      | Tổng thu/chi theo tháng, top danh mục chi tiêu, chuẩn bị dữ liệu bảng thống kê                                                        |

#### ⚠️ Logic Quan Trọng Trong `TransactionController`

```
Khi THÊM giao dịch:
  - EXPENSE → Account.balance -= amount  VÀ  Budget.spent_amount += amount
  - INCOME  → Account.balance += amount

Khi XÓA giao dịch:
  - EXPENSE → Account.balance += amount  VÀ  Budget.spent_amount -= amount
  - INCOME  → Account.balance -= amount

Khi SỬA giao dịch:
  - Rollback balance/spent cũ → Apply balance/spent mới

⚡ Tất cả thao tác trong MỘT database transaction (BEGIN TRAN...COMMIT)
   để đảm bảo tính nhất quán dữ liệu (ACID)
```

---

### 4.5. VIEW — Giao Diện Swing (Tiếng Việt)

> **Quy tắc UI:** Toàn bộ nhãn (label), nút bấm (button), tiêu đề cột bảng, thông báo (JOptionPane), placeholder text đều phải bằng **Tiếng Việt**.
> Không dùng FlatLaf hay Look & Feel bên ngoài — chỉ dùng Swing thuần với `Nimbus` L&F hoặc tùy chỉnh thủ công.

#### Luồng Điều Hướng

```
MànHình Đăng Nhập ──(thành công)──> MainFrame
    │                                     │
    │                                     ├── Tổng Quan (DashboardPanel)
    │                                     ├── Giao Dịch (TransactionListPanel)
    └──(chưa có tài khoản)──>             ├── Tài Khoản (AccountListPanel)
         Màn Hình Đăng Ký                ├── Danh Mục  (CategoryListPanel)
               │                          ├── Ngân Sách (BudgetListPanel)
               └──(thành công)──> Login  └── Báo Cáo   (ReportPanel)

Mỗi ListPanel có Dialog tương ứng cho Thêm/Sửa:
  - TransactionListPanel ←→ TransactionFormDialog
  - AccountListPanel     ←→ AccountFormDialog
  - CategoryListPanel    ←→ CategoryFormDialog
  - BudgetListPanel      ←→ BudgetFormDialog
```

#### Mô Tả Các View Chính

| View                           | Thành phần UI                                                                                | Nhãn Tiếng Việt tiêu biểu                                 |
| ------------------------------ | ---------------------------------------------------------------------------------------------- | ----------------------------------------------------------- |
| **LoginView**            | JTextField username, JPasswordField, JButton Đăng Nhập / Đăng Ký                       | "Tên đăng nhập", "Mật khẩu", "Đăng nhập"         |
| **RegisterView**         | Form: username, password, xác nhận mật khẩu, họ tên, email, điện thoại             | "Xác nhận mật khẩu", "Họ và tên"                   |
| **MainFrame**            | `SidebarPanel` (menu trái) + `JPanel` content area (CardLayout)                          | "Tổng Quan", "Giao Dịch", "Tài Khoản", …          |
| **DashboardPanel**       | 3 CardPanel: Tổng Thu / Tổng Chi / Số Dư + JTable 5 giao dịch gần nhất               | "Tổng thu tháng này", "Giao dịch gần đây"          |
| **TransactionListPanel** | JTable + TransactionFilterPanel (lọc ngày, loại, danh mục) + nút Thêm / Sửa / Xóa   | "Ngày", "Loại", "Danh mục", "Số tiền", "Ghi chú" |
| **AccountListPanel**     | JTable hoặc card hiển thị từng tài khoản + nút Thêm / Sửa / Xóa                       | "Tên tài khoản", "Loại", "Số dư"                   |
| **CategoryListPanel**    | JTable + tab/filter Thu Nhập / Chi Tiêu + nút Thêm / Sửa / Xóa                         | "Tên danh mục", "Loại", "Mặc định"                 |
| **BudgetListPanel**      | JTable + JProgressBar hiển thị % sử dụng ngân sách                                     | "Danh mục", "Hạn mức", "Đã chi", "Còn lại"        |
| **ReportPanel**          | JTable thống kê thu/chi theo tháng + JTable top danh mục chi tiêu (không dùng biểu đồ) | "Tháng", "Tổng thu", "Tổng chi", "Chênh lệch"     |

#### Shared Components

| Component                | Mô tả                                                               |
| ------------------------ | --------------------------------------------------------------------- |
| `RoundedButton`        | JButton tùy chỉnh với bo góc, hiệu ứng hover                    |
| `PlaceholderTextField` | JTextField với placeholder text màu xám                            |
| `SidebarPanel`         | Menu điều hướng bên trái với icon + label Tiếng Việt           |
| `CardPanel`            | Panel hiển thị số liệu tổng quan (icon + tiêu đề + giá trị) |

---

### 4.6. UTIL — Tiện Ích

| Util Class            | Methods                                                                                                     | Chức năng                                |
| --------------------- | ----------------------------------------------------------------------------------------------------------- | ------------------------------------------ |
| `PasswordUtil`      | `hash(String)` → String, `verify(String, String)` → boolean                                           | Mã hóa & xác thực mật khẩu BCrypt    |
| `CurrencyFormatter` | `format(BigDecimal)` → "1.500.000 ₫", `parse(String)` → BigDecimal                                   | Format/parse tiền tệ VND                 |
| `DateUtil`          | `formatDate(LocalDate)`, `parseDate(String)`, `getStartOfMonth()`, `getEndOfMonth()`                | Xử lý ngày tháng                       |
| `ValidationUtil`    | `isNullOrEmpty(String)`, `isValidEmail(String)`, `isPositiveNumber(String)`, `isValidPhone(String)` | Validate dữ liệu đầu vào              |
| `SessionManager`    | `setCurrentUser(User)`, `getCurrentUser()`, `logout()`, `isLoggedIn()`                              | Singleton — quản lý phiên đăng nhập |

---

## 5. Thư Viện Cần Thiết

| Thư viện           | Phiên bản      | Mục đích                          | Bắt buộc? |
| -------------------- | ---------------- | ------------------------------------ | ----------- |
| **mssql-jdbc** | 12.8.1.jre17   | JDBC Driver kết nối SQL Server   | ✅ Có      |
| **jBCrypt**    | 0.4            | Mã hóa mật khẩu an toàn       | ✅ Có      |

> **Lưu ý:** Không dùng FlatLaf, JFreeChart hay bất kỳ thư viện UI/chart bên ngoài. Chỉ dùng Java Swing thuần (JDK 17 built-in).

---

## 6. Cấu Hình Apache Ant (`build.xml`)

Build tool sử dụng **Apache Ant** tích hợp sẵn trong **NetBeans**.

### Các target chính trong `build.xml`:

| Target      | Mô tả                                             |
| ----------- | --------------------------------------------------- |
| `clean`   | Xóa thư mục `build/` và `dist/`               |
| `compile` | Compile toàn bộ source, bao gồm JARs trong `lib/` |
| `jar`     | Đóng gói thành file `.jar` có thể chạy          |
| `run`     | Compile + chạy ứng dụng                           |

### Classpath & lib:

- Tất cả file `.jar` trong thư mục `lib/` phải được thêm vào classpath khi compile và khi chạy.
- Manifest của JAR phải khai báo `Main-Class: personalfinancemanager.Main`.

---

## 7. Script Database

> **📁 Đã có sẵn, không cần tạo mới:**
> 1. Chạy `database/create_database.sql` trong SSMS → Tạo DB `PersonalFinanceDB` (Collation: `Vietnamese_CI_AS`), 5 bảng, indexes, 3 views.
> 2. Chạy `database/insert_data.sql` trong SSMS → Seed danh mục mặc định và dữ liệu mẫu.
> 3. Cập nhật thông tin kết nối trong `AppConstants.java` cho khớp với SQL Server instance.

---

## 8. Thứ Tự Triển Khai (5 Phases)

### Phase 1 — Nền Tảng (Foundation)

| # | Task                                          | Output                                             |
| - | --------------------------------------------- | -------------------------------------------------- |
| 1 | Cấu hình `build.xml` (Ant)               | Build script hoàn chỉnh                          |
| 2 | Đặt JAR vào `lib/`, khai báo classpath   | Thư viện sẵn sàng sử dụng                     |
| 3 | Tạo `DatabaseConnection.java`            | Singleton kết nối SQL Server                     |
| 4 | Tạo `AppConstants.java`                  | Hằng số ứng dụng (DB config, UI config)         |
| 5 | Tạo tất cả **Model** classes (5 file)    | POJO: User, Account, Category, Transaction, Budget |
| 6 | Tạo `BaseDAO.java` + `UserDAO.java`      | Lớp DAO cơ sở + DAO người dùng               |
| 7 | Test kết nối DB                             | Xác nhận kết nối thành công                  |

### Phase 2 — Xác Thực (Authentication)

| #  | Task                                           | Output                             |
| -- | ---------------------------------------------- | ---------------------------------- |
| 8  | Tạo `PasswordUtil.java`                     | Mã hóa/xác thực BCrypt         |
| 9  | Tạo `SessionManager.java`                   | Quản lý phiên đăng nhập      |
| 10 | Tạo `ValidationUtil.java`                   | Validate đầu vào                |
| 11 | Tạo `AuthController.java`                   | Logic đăng nhập/đăng ký      |
| 12 | Tạo `LoginView.java` + `RegisterView.java` | Giao diện đăng nhập/đăng ký |
| 13 | Tạo `Main.java` — entry point              | Khởi chạy ứng dụng             |

### Phase 3 — Khung Chính & Dashboard

| #  | Task                                                      | Output                                   |
| -- | --------------------------------------------------------- | ---------------------------------------- |
| 14 | Tạo shared components (RoundedButton, SidebarPanel, ...) | UI components tái sử dụng             |
| 15 | Tạo `MainFrame.java`                                   | Khung chính với sidebar + content area |
| 16 | Tạo `DashboardPanel.java`                              | Trang tổng quan                         |
| 17 | Tạo `AccountDAO` + `AccountController`               | Logic quản lý tài khoản              |
| 18 | Tạo `AccountListPanel` + `AccountFormDialog`         | Giao diện quản lý tài khoản         |

### Phase 4 — Danh Mục, Giao Dịch & Ngân Sách

| #  | Task                                                     | Output                                     |
| -- | -------------------------------------------------------- | ------------------------------------------ |
| 19 | Tạo `CategoryDAO` + `CategoryController`            | Logic quản lý danh mục                  |
| 20 | Tạo `CategoryListPanel` + `CategoryFormDialog`      | Giao diện quản lý danh mục             |
| 21 | Tạo `TransactionDAO` + `TransactionController`      | Logic quản lý giao dịch                 |
| 22 | Tạo `TransactionListPanel` + `TransactionFormDialog`| Giao diện quản lý giao dịch            |
| 23 | Implement logic cập nhật balance khi thêm/sửa/xóa  | Đồng bộ số dư tài khoản             |
| 24 | Tạo `CurrencyFormatter.java` + `DateUtil.java`      | Tiện ích format tiền tệ & ngày tháng  |
| 25 | Tạo `BudgetDAO` + `BudgetController`                | Logic quản lý ngân sách               |
| 26 | Tạo `BudgetListPanel` + `BudgetFormDialog`          | Giao diện ngân sách + JProgressBar    |

### Phase 5 — Báo Cáo & Hoàn Thiện

| #  | Task                                  | Output                                       |
| -- | ------------------------------------- | -------------------------------------------- |
| 27 | Tạo `ReportController`             | Logic thống kê (dùng view vw_MonthlyReport) |
| 28 | Tạo `ReportPanel`                  | Bảng thống kê thu/chi theo tháng           |
| 29 | Tạo `TransactionFilterPanel`       | Lọc giao dịch theo ngày/loại/danh mục     |
| 30 | Thêm cảnh báo vượt ngân sách    | JOptionPane thông báo tiếng Việt           |
| 31 | Test toàn diện + Fix bugs           | Ứng dụng hoàn chỉnh                       |
| 32 | Tối ưu hiệu suất & code cleanup    | Code sạch, chạy mượt                      |

---

## 9. Sơ Đồ Kiến Trúc MVC

```
┌─────────────────────────────────────────────────────────────────┐
│                         VIEW (Swing — Tiếng Việt)               │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │  Đăng   │ │  Tổng    │ │  Giao    │ │  Báo     │  ...      │
│  │  Nhập   │ │  Quan    │ │  Dịch    │ │  Cáo     │           │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘           │
└───────┼─────────────┼────────────┼─────────────┼────────────────┘
        │  Sự kiện   │  Sự kiện  │  Sự kiện   │  Sự kiện
        ▼             ▼            ▼             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      CONTROLLER                                 │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐ ┌──────────┐       │
│  │  Auth    │ │ Account  │ │ Transaction  │ │  Report  │  ...  │
│  │Controller│ │Controller│ │  Controller  │ │Controller│       │
│  └────┬─────┘ └────┬─────┘ └──────┬───────┘ └────┬─────┘       │
└───────┼─────────────┼──────────────┼───────────────┼────────────┘
        │  CRUD       │  CRUD        │  CRUD         │  Query
        ▼             ▼              ▼               ▼
┌─────────────────────────────────────────────────────────────────┐
│                         DAO                                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐ ┌──────────┐       │
│  │ UserDAO  │ │AccountDAO│ │TransactionDAO│ │BudgetDAO │  ...  │
│  └────┬─────┘ └────┬─────┘ └──────┬───────┘ └────┬─────┘       │
└───────┼─────────────┼──────────────┼───────────────┼────────────┘
        │  SQL        │  SQL         │  SQL          │  SQL
        ▼             ▼              ▼               ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DATABASE (SQL Server)                         │
│  ┌──────┐ ┌────────┐ ┌──────────┐ ┌────────────┐ ┌───────┐     │
│  │Users │ │Accounts│ │Categories│ │Transactions│ │Budgets│     │
│  └──────┘ └────────┘ └──────────┘ └────────────┘ └───────┘     │
└─────────────────────────────────────────────────────────────────┘

MODEL (POJO) được sử dụng xuyên suốt để truyền dữ liệu giữa các layer
```

---

## 10. Ghi Chú Kỹ Thuật

### 10.1. Quy Tắc Đặt Tên

| Loại     | Convention  | Ví dụ                          |
| --------- | ----------- | -------------------------------- |
| Package   | lowercase   | `personalfinancemanager.model` |
| Class     | PascalCase  | `TransactionController`        |
| Method    | camelCase   | `findByUserId()`               |
| Biến     | camelCase   | `accountName`                  |
| Hằng số | UPPER_SNAKE | `DB_URL`, `PRIMARY_COLOR`    |
| Bảng DB  | PascalCase  | `Transactions`                 |
| Cột DB   | snake_case  | `transaction_date`             |

### 10.2. Xử Lý Lỗi

- DAO: Throw `SQLException` → Controller xử lý
- Controller: Catch exception → trả về kết quả cho View
- View: Hiển thị thông báo lỗi qua `JOptionPane` bằng **Tiếng Việt**
- Log lỗi ra console: `System.err.println()` hoặc `java.util.logging`

### 10.3. Bảo Mật

- Mật khẩu: Hash bằng BCrypt (không lưu plain text)
- SQL Injection: Sử dụng `PreparedStatement` (KHÔNG dùng string concatenation)
- Session: Lưu trong memory (SessionManager), không persist

### 10.4. Ngôn Ngữ Giao Diện

Danh sách các chuỗi UI quan trọng cần dùng Tiếng Việt:

| Ngữ cảnh          | Chuỗi Tiếng Việt                                                |
| ------------------- | ----------------------------------------------------------------- |
| Sidebar menu       | Tổng Quan, Giao Dịch, Tài Khoản, Danh Mục, Ngân Sách, Báo Cáo |
| Nút hành động    | Thêm, Sửa, Xóa, Lưu, Hủy, Đóng, Tìm kiếm, Làm mới     |
| Cột bảng          | Ngày, Loại, Danh mục, Số tiền, Tài khoản, Ghi chú        |
| Loại giao dịch   | Thu nhập, Chi tiêu                                               |
| Loại tài khoản  | Tiền mặt, Ngân hàng, Ví điện tử, Thẻ tín dụng, Tiết kiệm |
| Thông báo lỗi    | "Tên đăng nhập không được để trống!", "Mật khẩu không đúng!" |
| Xác nhận xóa    | "Bạn có chắc muốn xóa?", "Xác nhận", "Không"             |

---

*Tài liệu cập nhật ngày: 04/07/2026*
*Phiên bản: 2.0*
*Build Tool: Apache Ant | UI Language: Tiếng Việt | DB: SQL Server (scripts trong database/)*
