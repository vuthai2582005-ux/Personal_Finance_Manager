// State toàn cục của ứng dụng
const AppState = {
    user: null,
    activeSection: 'section-dashboard',
    accounts: [],
    categories: [],
    transactions: [],
    budgets: [],
    charts: {
        pie: null,
        bar: null
    }
};

// ==================== KHỞI CHẠY KHI TẢI TRANG ====================
document.addEventListener('DOMContentLoaded', () => {
    initEventListeners();
    checkAuthSession();
});

// ==================== XÁC THỰC PHIÊN ĐĂNG NHẬP ====================
async function checkAuthSession() {
    try {
        const res = await fetch('api/auth/me');
        if (!res.ok) throw new Error("Unauthorized");
        const data = await res.json();
        
        if (data.loggedIn) {
            AppState.user = data.user;
            showAppView();
        } else {
            showAuthView();
        }
    } catch (err) {
        showAuthView();
    }
}

function showAuthView() {
    document.getElementById('auth-section').style.display = 'flex';
    document.getElementById('app-section').style.display = 'none';
}

function showAppView() {
    document.getElementById('auth-section').style.display = 'none';
    document.getElementById('app-section').style.display = 'flex';
    
    // Thiết lập giao diện theo user
    const fullName = AppState.user.fullName || AppState.user.username;
    document.getElementById('display-user-fullname').innerText = fullName;
    document.getElementById('avatar-char').innerText = fullName.trim().charAt(0).toUpperCase();

    // Reset date input của trans form về hôm nay
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('trans-date').value = today;

    // Load dữ liệu tab mặc định (Dashboard)
    switchTab('section-dashboard');
}

// ==================== BỘ LẮNG NGHE SỰ KIỆN TỔNG QUAN ====================
function initEventListeners() {
    // Điều hướng Login <=> Register
    document.getElementById('to-register').addEventListener('click', (e) => {
        e.preventDefault();
        document.getElementById('login-card').style.display = 'none';
        document.getElementById('register-card').style.display = 'block';
    });
    
    document.getElementById('to-login').addEventListener('click', (e) => {
        e.preventDefault();
        document.getElementById('login-card').style.display = 'block';
        document.getElementById('register-card').style.display = 'none';
    });

    // Form Đăng nhập & Đăng ký
    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('register-form').addEventListener('submit', handleRegister);
    document.getElementById('btn-logout-act').addEventListener('click', handleLogout);

    // Sự kiện điều hướng Sidebar Tabs
    document.querySelectorAll('.sidebar .nav-item').forEach(item => {
        item.addEventListener('click', () => {
            const targetSection = item.getAttribute('data-target');
            switchTab(targetSection);
            
            // Cập nhật class active trong sidebar
            document.querySelectorAll('.sidebar .nav-item').forEach(nav => nav.classList.remove('active'));
            item.classList.add('active');
        });
    });

    // Định dạng số khi gõ tiền tệ (Form Account, Transaction, Budget) - Đã sửa lỗi xung đột bộ gõ tiếng Việt
    ['acc-balance', 'trans-amount', 'budget-limit'].forEach(id => {
        const input = document.getElementById(id);
        if (!input) return;

        // 1. Khi người dùng click vào ô nhập liệu: Bỏ định dạng dấu chấm để nhập/sửa số thuần túy
        input.addEventListener('focus', () => {
            const cleanVal = input.value.replace(/\D/g, "");
            input.value = cleanVal;
        });

        // 2. Chỉ cho phép nhập số thuần túy khi đang gõ
        input.addEventListener('keypress', (e) => {
            if (!/\d/.test(e.key)) {
                e.preventDefault();
            }
        });

        // 3. Khi bấm ra ngoài: Định dạng lại số tiền thành định dạng có dấu chấm phân cách hàng nghìn
        input.addEventListener('blur', () => {
            const cleanVal = input.value.replace(/\D/g, "");
            if (cleanVal !== "") {
                input.value = new Intl.NumberFormat('vi-VN').format(cleanVal);
            }
        });
    });


    // Link Thêm nhanh Giao dịch ở Dashboard
    document.getElementById('dash-add-trans-btn').addEventListener('click', () => {
        openTransactionModal();
    });

    // Modal Trực quan: Tự động lọc danh mục tương ứng khi đổi Loại giao dịch (Thu/Chi)
    document.getElementById('trans-type').addEventListener('change', () => {
        populateTransactionCategories();
    });

    // Submit Forms CRUD
    document.getElementById('account-form').addEventListener('submit', submitAccountForm);
    document.getElementById('category-form').addEventListener('submit', submitCategoryForm);
    document.getElementById('transaction-form').addEventListener('submit', submitTransactionForm);
    document.getElementById('budget-form').addEventListener('submit', submitBudgetForm);

    // Lọc giao dịch
    document.getElementById('transaction-filter-form').addEventListener('submit', (e) => {
        e.preventDefault();
        loadTransactionsTab();
    });
    document.getElementById('reset-filter-btn').addEventListener('click', () => {
        document.getElementById('transaction-filter-form').reset();
        loadTransactionsTab();
    });

    // Bút bấm Thêm mới Modal
    document.getElementById('add-account-btn').addEventListener('click', () => {
        openAccountModal();
    });
    document.getElementById('add-category-btn').addEventListener('click', () => {
        openCategoryModal();
    });
    document.getElementById('add-transaction-btn').addEventListener('click', () => {
        openTransactionModal();
    });
    document.getElementById('add-budget-btn').addEventListener('click', () => {
        openBudgetModal();
    });

    // Xem Báo cáo
    document.getElementById('update-report-btn').addEventListener('click', () => {
        loadReportsTab();
    });

    // Thay đổi màu sắc hiển thị mã hex
    document.getElementById('cat-color').addEventListener('input', (e) => {
        document.getElementById('color-hex').innerText = e.target.value.toUpperCase();
    });
}

// ==================== CÁC CHỨC NĂNG XÁC THỰC ====================
async function handleLogin(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<span class="loading-spinner"></span>';
    btn.disabled = true;

    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    try {
        const body = new URLSearchParams();
        body.append('username', username);
        body.append('password', password);

        const res = await fetch('api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body: body
        });
        const data = await res.json();

        if (data.success) {
            showToast(data.message, 'success');
            AppState.user = data.user;
            showAppView();
            e.target.reset();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Có lỗi kết nối mạng xảy ra!', 'error');
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

async function handleRegister(e) {
    e.preventDefault();
    
    const password = document.getElementById('reg-password').value;
    const confirm = document.getElementById('reg-confirm').value;

    if (password !== confirm) {
        showToast('Mật khẩu xác nhận không trùng khớp!', 'error');
        return;
    }

    const btn = e.target.querySelector('button');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<span class="loading-spinner"></span>';
    btn.disabled = true;

    try {
        const body = new URLSearchParams();
        body.append('username', document.getElementById('reg-username').value);
        body.append('password', password);
        body.append('confirmPassword', confirm);
        body.append('fullName', document.getElementById('reg-fullname').value);
        body.append('email', document.getElementById('reg-email').value);
        body.append('phone', document.getElementById('reg-phone').value);

        const res = await fetch('api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body: body
        });
        const data = await res.json();

        if (data.success) {
            showToast(data.message, 'success');
            document.getElementById('register-card').style.display = 'none';
            document.getElementById('login-card').style.display = 'block';
            e.target.reset();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Có lỗi kết nối mạng xảy ra!', 'error');
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

async function handleLogout() {
    try {
        const res = await fetch('api/auth/logout', { method: 'POST' });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            AppState.user = null;
            showAuthView();
        }
    } catch (err) {
        showToast('Không thể kết nối với server để đăng xuất!', 'error');
    }
}

// ==================== ĐIỀU HƯỚNG TABS ====================
function switchTab(sectionId) {
    // Ẩn tất cả các tab
    document.querySelectorAll('.page-section').forEach(sec => sec.classList.remove('active'));
    
    // Hiển thị tab đích
    const target = document.getElementById(sectionId);
    if (target) {
        target.classList.add('active');
        AppState.activeSection = sectionId;
    }

    // Tải dữ liệu tương ứng cho tab
    if (sectionId === 'section-dashboard') {
        loadDashboardTab();
    } else if (sectionId === 'section-accounts') {
        loadAccountsTab();
    } else if (sectionId === 'section-categories') {
        loadCategoriesTab();
    } else if (sectionId === 'section-transactions') {
        loadTransactionsTab(true); // Lần đầu vào thì load danh mục/tài khoản cho bộ lọc luôn
    } else if (sectionId === 'section-budgets') {
        loadBudgetsTab();
    } else if (sectionId === 'section-reports') {
        // Thiết lập tháng và năm mặc định trong bộ lọc
        const d = new Date();
        document.getElementById('report-month').value = d.getMonth() + 1;
        document.getElementById('report-year').value = d.getFullYear();
        loadReportsTab();
    }
}

// ==================== TIỆN ÍCH CHUNG ====================
function formatCurrency(number) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(number);
}

function openModal(id) {
    document.getElementById(id).classList.add('active');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('active');
}

function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let iconClass = 'fa-circle-check';
    if (type === 'error') iconClass = 'fa-circle-xmark';
    if (type === 'warning') iconClass = 'fa-triangle-exclamation';

    toast.innerHTML = `
        <i class="fa-solid ${iconClass}"></i>
        <span>${message}</span>
    `;

    container.appendChild(toast);

    // Slide out và xóa sau 4 giây
    setTimeout(() => {
        toast.style.animation = 'slideInRight 0.3s reverse forwards';
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 3700);
}

// ==================== TẢI DỮ LIỆU TAB 1: DASHBOARD ====================
async function loadDashboardTab() {
    try {
        const d = new Date();
        document.getElementById('dashboard-month-label').innerText = `Tháng ${d.getMonth() + 1}/${d.getFullYear()}`;

        // 1. Tính số dư từ tổng số dư tài khoản
        const resAcc = await fetch('api/accounts');
        const accounts = await resAcc.json();
        AppState.accounts = accounts;
        const totalBalance = accounts.reduce((sum, a) => sum + (a.active ? a.balance : 0), 0);
        document.getElementById('dash-balance').innerText = formatCurrency(totalBalance);

        // 2. Lấy thu nhập/chi tiêu tháng này từ API thống kê giao dịch
        // Sử dụng API báo cáo cơ cấu để cộng dồn hoặc tính gián tiếp
        const resInc = await fetch(`api/reports/category?type=INCOME&month=${d.getMonth() + 1}&year=${d.getFullYear()}`);
        const incomes = await resInc.json();
        const totalIncome = incomes.reduce((sum, i) => sum + i.totalAmount, 0);
        document.getElementById('dash-income').innerText = formatCurrency(totalIncome);

        const resExp = await fetch(`api/reports/category?type=EXPENSE&month=${d.getMonth() + 1}&year=${d.getFullYear()}`);
        const expenses = await resExp.json();
        const totalExpense = expenses.reduce((sum, e) => sum + e.totalAmount, 0);
        document.getElementById('dash-expense').innerText = formatCurrency(totalExpense);

        // 3. Tải 5 giao dịch gần đây
        const resTrans = await fetch('api/transactions?limit=5');
        const recentTrans = await resTrans.json();

        const tbody = document.getElementById('dashboard-recent-table-body');
        tbody.innerHTML = '';

        if (recentTrans.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted);">Không có giao dịch nào gần đây.</td></tr>';
            return;
        }

        recentTrans.forEach(t => {
            const isIncome = t.type === 'INCOME';
            const badgeClass = isIncome ? 'badge-income' : 'badge-expense';
            const badgeText = isIncome ? 'Thu nhập' : 'Chi tiêu';
            const amountColor = isIncome ? 'color: var(--success); font-weight: 700;' : 'color: var(--danger); font-weight: 700;';
            const prefix = isIncome ? '+' : '-';

            tbody.innerHTML += `
                <tr>
                    <td>${t.transactionDate}</td>
                    <td><span class="badge ${badgeClass}">${badgeText}</span></td>
                    <td>${t.categoryName}</td>
                    <td style="text-align: right; ${amountColor}">${prefix} ${formatCurrency(t.amount)}</td>
                    <td>${t.accountName}</td>
                    <td>${t.description || ''}</td>
                </tr>
            `;
        });

    } catch (err) {
        showToast('Không tải được số liệu tổng quan!', 'error');
    }
}

// ==================== TẢI DỮ LIỆU TAB 2: TÀI KHOẢN ====================
async function loadAccountsTab() {
    try {
        const res = await fetch('api/accounts');
        const accounts = await res.json();
        AppState.accounts = accounts;

        const tbody = document.getElementById('accounts-table-body');
        tbody.innerHTML = '';

        if (accounts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">Chưa có tài khoản nào được tạo.</td></tr>';
            return;
        }

        accounts.forEach(a => {
            const statusBadge = a.active 
                ? '<span class="badge badge-income">Đang hoạt động</span>'
                : '<span class="badge" style="background: rgba(255,255,255,0.05); color: var(--text-muted);">Đã khóa</span>';

            tbody.innerHTML += `
                <tr>
                    <td><strong>${a.accountName}</strong></td>
                    <td>${a.accountType}</td>
                    <td style="text-align: right; font-weight: 600;">${formatCurrency(a.balance)}</td>
                    <td>${statusBadge}</td>
                    <td style="text-align: right;">
                        <div class="action-buttons" style="justify-content: flex-end;">
                            <button class="btn-icon edit" onclick="editAccount(${a.accountId})" title="Sửa"><i class="fa-solid fa-pen"></i></button>
                            <button class="btn-icon delete" onclick="deleteAccount(${a.accountId})" title="Xóa"><i class="fa-solid fa-trash"></i></button>
                        </div>
                    </td>
                </tr>
            `;
        });
    } catch (err) {
        showToast('Không thể tải danh sách tài khoản!', 'error');
    }
}

// ==================== TẢI DỮ LIỆU TAB 3: DANH MỤC ====================
async function loadCategoriesTab() {
    try {
        const res = await fetch('api/categories');
        const categories = await res.json();
        AppState.categories = categories;

        const tbody = document.getElementById('categories-table-body');
        tbody.innerHTML = '';

        if (categories.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: var(--text-muted);">Chưa có danh mục nào được tạo.</td></tr>';
            return;
        }

        categories.forEach(c => {
            const isSystem = c.isDefault 
                ? '<span class="badge" style="background: rgba(56, 189, 248, 0.15); color: #38bdf8;">Hệ thống</span>'
                : '<span class="badge" style="background: rgba(255,255,255,0.05); color: var(--text-muted);">Tự tạo</span>';

            const typeBadge = c.type === 'INCOME'
                ? '<span class="badge badge-income">Thu nhập</span>'
                : '<span class="badge badge-expense">Chi tiêu</span>';

            tbody.innerHTML += `
                <tr>
                    <td>
                        <span class="color-circle" style="background: ${c.color || '#ADB5BD'};"></span>
                        <strong>${c.categoryName}</strong>
                    </td>
                    <td>${typeBadge}</td>
                    <td>${isSystem}</td>
                    <td style="text-align: right;">
                        <div class="action-buttons" style="justify-content: flex-end;">
                            ${c.isDefault ? '<span style="color: var(--text-muted); font-size:12px; font-style:italic;">Không thể sửa/xóa</span>' : `
                                <button class="btn-icon edit" onclick="editCategory(${c.categoryId})" title="Sửa"><i class="fa-solid fa-pen"></i></button>
                                <button class="btn-icon delete" onclick="deleteCategory(${c.categoryId})" title="Xóa"><i class="fa-solid fa-trash"></i></button>
                            `}
                        </div>
                    </td>
                </tr>
            `;
        });
    } catch (err) {
        showToast('Không thể tải danh sách danh mục!', 'error');
    }
}

// ==================== TẢI DỮ LIỆU TAB 4: GIAO DỊCH ====================
async function loadTransactionsTab(isFirstLoad = false) {
    try {
        if (isFirstLoad) {
            // Tải danh sách tài khoản & danh mục vào form filter
            const resAcc = await fetch('api/accounts');
            const accounts = await resAcc.json();
            const filterAcc = document.getElementById('filter-account');
            filterAcc.innerHTML = '<option value="">Tất cả tài khoản</option>';
            accounts.forEach(a => {
                if (a.active) filterAcc.innerHTML += `<option value="${a.accountId}">${a.accountName}</option>`;
            });

            const resCat = await fetch('api/categories');
            const categories = await resCat.json();
            const filterCat = document.getElementById('filter-category');
            filterCat.innerHTML = '<option value="">Tất cả danh mục</option>';
            categories.forEach(c => {
                filterCat.innerHTML += `<option value="${c.categoryId}">${c.categoryName} (${c.type === 'INCOME' ? 'Thu' : 'Chi'})</option>`;
            });
        }

        // Lấy các tham số lọc từ form
        const accountId = document.getElementById('filter-account').value;
        const categoryId = document.getElementById('filter-category').value;
        const type = document.getElementById('filter-type').value;
        const fromDate = document.getElementById('filter-from-date').value;
        const toDate = document.getElementById('filter-to-date').value;

        // Xây dựng url gọi API
        const queryParams = new URLSearchParams();
        if (accountId) queryParams.append('accountId', accountId);
        if (categoryId) queryParams.append('categoryId', categoryId);
        if (type) queryParams.append('type', type);
        if (fromDate) queryParams.append('fromDate', fromDate);
        if (toDate) queryParams.append('toDate', toDate);

        const res = await fetch(`api/transactions?${queryParams.toString()}`);
        const transactions = await res.json();
        AppState.transactions = transactions;

        const tbody = document.getElementById('transactions-table-body');
        tbody.innerHTML = '';

        if (transactions.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">Không tìm thấy giao dịch nào trùng khớp.</td></tr>';
            return;
        }

        transactions.forEach(t => {
            const isIncome = t.type === 'INCOME';
            const badgeClass = isIncome ? 'badge-income' : 'badge-expense';
            const badgeText = isIncome ? 'Thu nhập' : 'Chi tiêu';
            const amountColor = isIncome ? 'color: var(--success); font-weight: 700;' : 'color: var(--danger); font-weight: 700;';
            const prefix = isIncome ? '+' : '-';

            tbody.innerHTML += `
                <tr>
                    <td>${t.transactionDate}</td>
                    <td><span class="badge ${badgeClass}">${badgeText}</span></td>
                    <td>${t.categoryName}</td>
                    <td style="text-align: right; ${amountColor}">${prefix} ${formatCurrency(t.amount)}</td>
                    <td>${t.accountName}</td>
                    <td>${t.description || ''}</td>
                    <td style="text-align: right;">
                        <div class="action-buttons" style="justify-content: flex-end;">
                            <button class="btn-icon edit" onclick="editTransaction(${t.transactionId})" title="Sửa"><i class="fa-solid fa-pen"></i></button>
                            <button class="btn-icon delete" onclick="deleteTransaction(${t.transactionId})" title="Xóa"><i class="fa-solid fa-trash"></i></button>
                        </div>
                    </td>
                </tr>
            `;
        });
    } catch (err) {
        showToast('Không tải được lịch sử giao dịch!', 'error');
    }
}

// ==================== TẢI DỮ LIỆU TAB 5: NGÂN SÁCH ====================
async function loadBudgetsTab() {
    try {
        const res = await fetch('api/budgets');
        const budgets = await res.json();
        AppState.budgets = budgets;

        const container = document.getElementById('budgets-container');
        container.innerHTML = '';

        if (budgets.length === 0) {
            container.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: var(--text-muted); padding: 40px 0;">Chưa thiết lập ngân sách chi tiêu nào. Hãy tạo hạn mức ngân sách cho tháng này!</div>';
            return;
        }

        budgets.forEach(b => {
            const pct = b.budgetAmount > 0 ? (b.spentAmount / b.budgetAmount) * 100 : 0;
            const pctText = pct.toFixed(1);
            
            // Xác định màu sắc thanh progress
            let fillColor = 'var(--success)';
            let warningText = '';
            if (pct >= 100) {
                fillColor = 'var(--danger)';
                warningText = '<span style="color: #f87171; font-weight:700;"><i class="fa-solid fa-triangle-exclamation"></i> Vượt hạn mức!</span>';
            } else if (pct >= 80) {
                fillColor = 'var(--warning)';
                warningText = '<span style="color: #fbbf24;"><i class="fa-solid fa-bell"></i> Sắp chạm hạn mức!</span>';
            }

            container.innerHTML += `
                <div class="stats-card" style="flex-direction: column; align-items: stretch; gap: 10px; padding: 20px;">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span style="font-size: 15px; font-weight:700; color: white;">${b.categoryName}</span>
                        <span style="font-size: 12px; color: var(--text-muted); font-weight:600;">Tháng ${b.month}/${b.year}</span>
                    </div>
                    <div style="display: flex; justify-content: space-between; align-items: baseline; margin: 4px 0;">
                        <div>
                            <span style="font-size: 18px; font-weight:800; color: white;">${formatCurrency(b.spentAmount)}</span>
                            <span style="font-size: 12px; color: var(--text-muted);">/ ${formatCurrency(b.budgetAmount)}</span>
                        </div>
                        <span style="font-size: 14px; font-weight:800; color: ${fillColor}">${pctText}%</span>
                    </div>
                    
                    <div class="budget-progress-container">
                        <div class="budget-progress-bar-bg">
                            <div class="budget-progress-bar-fill" style="width: ${Math.min(pct, 100)}%; background-color: ${fillColor};"></div>
                        </div>
                    </div>

                    <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px; border-top: 1px solid rgba(255,255,255,0.05); padding-top: 10px;">
                        <div>${warningText}</div>
                        <div class="action-buttons">
                            <button class="btn-icon edit" onclick="editBudget(${b.budgetId})" title="Sửa hạn mức" style="width:28px; height:28px;"><i class="fa-solid fa-pen" style="font-size:11px;"></i></button>
                            <button class="btn-icon delete" onclick="deleteBudget(${b.budgetId})" title="Xóa" style="width:28px; height:28px;"><i class="fa-solid fa-trash" style="font-size:11px;"></i></button>
                        </div>
                    </div>
                </div>
            `;
        });
    } catch (err) {
        showToast('Không thể tải thông tin ngân sách!', 'error');
    }
}

// ==================== TẢI DỮ LIỆU TAB 6: BÁO CÁO (CHART.JS) ====================
async function loadReportsTab() {
    const month = document.getElementById('report-month').value;
    const year = document.getElementById('report-year').value;
    const type = document.getElementById('report-type').value;

    const typeText = type === 'EXPENSE' ? 'chi tiêu' : 'thu nhập';
    document.getElementById('pie-chart-title').innerText = `Cơ cấu ${typeText} tháng ${month}/${year}`;
    document.getElementById('bar-chart-title').innerText = `Xu hướng thu chi cả năm ${year}`;

    try {
        // 1. Tải báo cáo cơ cấu danh mục (Pie Chart)
        const resCat = await fetch(`api/reports/category?type=${type}&month=${month}&year=${year}`);
        const categoryData = await resCat.json();

        // 2. Tải báo cáo xu hướng năm (Bar Chart)
        const resTrend = await fetch(`api/reports/trend?year=${year}`);
        const trendData = await resTrend.json();

        renderPieChart(categoryData);
        renderBarChart(trendData);

    } catch (err) {
        showToast('Không tải được biểu đồ thống kê!', 'error');
    }
}

function renderPieChart(dataList) {
    const ctx = document.getElementById('pieChart').getContext('2d');
    
    // Hủy biểu đồ cũ nếu đã tồn tại để tránh xung đột render
    if (AppState.charts.pie) {
        AppState.charts.pie.destroy();
    }

    if (dataList.length === 0) {
        // Hiển thị thông báo không có dữ liệu bằng text trên canvas
        AppState.charts.pie = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Không có dữ liệu'],
                datasets: [{
                    data: [1],
                    backgroundColor: ['rgba(255,255,255,0.05)'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { labels: { color: '#94a3b8' } }
                }
            }
        });
        return;
    }

    const labels = dataList.map(item => item.categoryName);
    const data = dataList.map(item => item.totalAmount);
    
    // Tạo mảng màu đẹp
    const colors = [
        '#ef4444', '#f97316', '#f59e0b', '#10b981', '#06b6d4', 
        '#3b82f6', '#6366f1', '#8b5cf6', '#d946ef', '#ec4899'
    ];

    AppState.charts.pie = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: colors.slice(0, dataList.length),
                borderColor: '#1e293b',
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        color: '#f8fafc',
                        font: { family: 'Plus Jakarta Sans', size: 12 }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const val = context.raw;
                            return ` ${context.label}: ${formatCurrency(val)}`;
                        }
                    }
                }
            },
            cutout: '65%'
        }
    });
}

function renderBarChart(trendList) {
    const ctx = document.getElementById('barChart').getContext('2d');

    if (AppState.charts.bar) {
        AppState.charts.bar.destroy();
    }

    const labels = trendList.map(t => `Tháng ${t.month}`);
    const incomes = trendList.map(t => t.totalIncome);
    const expenses = trendList.map(t => t.totalExpense);

    AppState.charts.bar = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Thu nhập',
                    data: incomes,
                    backgroundColor: '#10b981',
                    borderRadius: 4
                },
                {
                    label: 'Chi tiêu',
                    data: expenses,
                    backgroundColor: '#ef4444',
                    borderRadius: 4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    grid: { display: false },
                    ticks: { color: '#94a3b8', font: { family: 'Plus Jakarta Sans' } }
                },
                y: {
                    grid: { color: 'rgba(255,255,255,0.05)' },
                    ticks: {
                        color: '#94a3b8',
                        font: { family: 'Plus Jakarta Sans' },
                        callback: function(value) {
                            if (value >= 1000000) return (value / 1000000) + ' Tr';
                            if (value >= 1000) return (value / 1000) + ' K';
                            return value;
                        }
                    }
                }
            },
            plugins: {
                legend: {
                    labels: { color: '#f8fafc', font: { family: 'Plus Jakarta Sans' } }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return ` ${context.dataset.label}: ${formatCurrency(context.raw)}`;
                        }
                    }
                }
            }
        }
    });
}

// ==================== CÁC CHỨC NĂNG DỰNG MODAL FORM ====================

// --- MODAL TÀI KHOẢN ---
function openAccountModal(acc = null) {
    const title = document.getElementById('account-modal-title');
    const form = document.getElementById('account-form');
    form.reset();

    if (acc) {
        title.innerText = 'Chỉnh sửa tài khoản';
        document.getElementById('acc-id').value = acc.accountId;
        document.getElementById('acc-name').value = acc.accountName;
        document.getElementById('acc-type').value = acc.accountType;
        document.getElementById('acc-balance').value = new Intl.NumberFormat('vi-VN').format(acc.balance);
        document.getElementById('acc-active-group').style.display = 'flex';
        document.getElementById('acc-active').checked = acc.active;
        document.getElementById('acc-balance-group').querySelector('label').innerText = 'Số dư (VND) *';
    } else {
        title.innerText = 'Thêm tài khoản mới';
        document.getElementById('acc-id').value = '';
        document.getElementById('acc-active-group').style.display = 'none';
        document.getElementById('acc-balance-group').querySelector('label').innerText = 'Số dư ban đầu (VND) *';
    }

    openModal('modal-account');
}

async function submitAccountForm(e) {
    e.preventDefault();
    const id = document.getElementById('acc-id').value;
    const name = document.getElementById('acc-name').value;
    const type = document.getElementById('acc-type').value;
    const balance = document.getElementById('acc-balance').value;

    const action = id ? 'update' : 'add';

    try {
        const body = new URLSearchParams();
        body.append('action', action);
        if (id) {
            body.append('accountId', id);
            body.append('active', document.getElementById('acc-active').checked);
            body.append('balance', balance);
        } else {
            body.append('initialBalance', balance);
        }
        body.append('accountName', name);
        body.append('accountType', type);

        const res = await fetch('api/accounts', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body: body
        });
        const data = await res.json();

        if (data.success) {
            showToast(data.message, 'success');
            closeModal('modal-account');
            loadAccountsTab();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Lỗi khi gửi yêu cầu!', 'error');
    }
}

async function editAccount(id) {
    const acc = AppState.accounts.find(a => a.accountId === id);
    if (acc) {
        openAccountModal(acc);
    }
}

async function deleteAccount(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa tài khoản này không?')) return;

    try {
        const body = new URLSearchParams();
        body.append('action', 'delete');
        body.append('accountId', id);

        const res = await fetch('api/accounts', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body: body
        });
        const data = await res.json();

        if (data.success) {
            showToast(data.message, 'success');
            loadAccountsTab();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Lỗi khi xóa tài khoản!', 'error');
    }
}

// --- MODAL DANH MỤC ---
function openCategoryModal(cat = null) {
    const title = document.getElementById('category-modal-title');
    const form = document.getElementById('category-form');
    form.reset();

    if (cat) {
        title.innerText = 'Chỉnh sửa danh mục';
        document.getElementById('cat-id').value = cat.categoryId;
        document.getElementById('cat-name').value = cat.categoryName;
        document.getElementById('cat-type').value = cat.type;
        document.getElementById('cat-color').value = cat.color || '#2980b9';
        document.getElementById('color-hex').innerText = (cat.color || '#2980b9').toUpperCase();
    } else {
        title.innerText = 'Thêm danh mục mới';
        document.getElementById('cat-id').value = '';
        document.getElementById('cat-color').value = '#2980b9';
        document.getElementById('color-hex').innerText = '#2980B9';
    }

    openModal('modal-category');
}

async function submitCategoryForm(e) {
    e.preventDefault();
    const id = document.getElementById('cat-id').value;
    const name = document.getElementById('cat-name').value;
    const type = document.getElementById('cat-type').value;
    const color = document.getElementById('cat-color').value;

    const action = id ? 'update' : 'add';

    try {
        const body = new URLSearchParams();
        body.append('action', action);
        if (id) body.append('categoryId', id);
        body.append('categoryName', name);
        body.append('type', type);
        body.append('color', color);

        const res = await fetch('api/categories', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body: body
        });
        const data = await res.json();

        if (data.success) {
            showToast(data.message, 'success');
            closeModal('modal-category');
            loadCategoriesTab();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Lỗi khi gửi yêu cầu!', 'error');
    }
}

async function editCategory(id) {
    const cat = AppState.categories.find(c => c.categoryId === id);
    if (cat) openCategoryModal(cat);
}

async function deleteCategory(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa danh mục này?')) return;

    try {
        const body = new URLSearchParams();
        body.append('action', 'delete');
        body.append('categoryId', id);

        const res = await fetch('api/categories', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body: body
        });
        const data = await res.json();

        if (data.success) {
            showToast(data.message, 'success');
            loadCategoriesTab();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Lỗi khi xóa danh mục!', 'error');
    }
}

// --- MODAL GIAO DỊCH ---
async function openTransactionModal(trans = null) {
    const title = document.getElementById('transaction-modal-title');
    const form = document.getElementById('transaction-form');
    form.reset();

    // 1. Tải danh sách tài khoản hợp lệ
    const resAcc = await fetch('api/accounts');
    const accounts = await resAcc.json();
    const selectAcc = document.getElementById('trans-account');
    selectAcc.innerHTML = '';
    accounts.forEach(a => {
        if (a.active) selectAcc.innerHTML += `<option value="${a.accountId}">${a.accountName} (${formatCurrency(a.balance)})</option>`;
    });

    // 2. Tải toàn bộ danh mục của user về AppState để lọc
    const resCat = await fetch('api/categories');
    AppState.categories = await resCat.json();

    if (trans) {
        title.innerText = 'Chỉnh sửa giao dịch';
        document.getElementById('trans-id').value = trans.transactionId;
        document.getElementById('trans-type').value = trans.type;
        
        // Load lại categories tương thích với type cũ
        populateTransactionCategories();
        
        document.getElementById('trans-account').value = trans.accountId;
        document.getElementById('trans-category').value = trans.categoryId;
        document.getElementById('trans-amount').value = new Intl.NumberFormat('vi-VN').format(trans.amount);
        document.getElementById('trans-date').value = trans.rawDate;
        document.getElementById('trans-desc').value = trans.description || '';
    } else {
        title.innerText = 'Thêm giao dịch mới';
        document.getElementById('trans-id').value = '';
        document.getElementById('trans-type').value = 'EXPENSE';
        
        // Mặc định load categories của chi tiêu
        populateTransactionCategories();
        
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('trans-date').value = today;
    }

    openModal('modal-transaction');
}

function populateTransactionCategories() {
    const type = document.getElementById('trans-type').value;
    const selectCat = document.getElementById('trans-category');
    selectCat.innerHTML = '';
    
    const filtered = AppState.categories.filter(c => c.type === type);
    filtered.forEach(c => {
        selectCat.innerHTML += `<option value="${c.categoryId}">${c.categoryName}</option>`;
    });
}

async function submitTransactionForm(e) {
    e.preventDefault();
    const id = document.getElementById('trans-id').value;
    const type = document.getElementById('trans-type').value;
    const accountId = document.getElementById('trans-account').value;
    const categoryId = document.getElementById('trans-category').value;
    const amount = document.getElementById('trans-amount').value;
    const date = document.getElementById('trans-date').value;
    const description = document.getElementById('trans-desc').value;

    if (!accountId || !categoryId) {
        showToast('Vui lòng tạo tài khoản và danh mục trước!', 'warning');
        return;
    }

    const action = id ? 'update' : 'add';

    try {
        const body = new URLSearchParams();
        body.append('action', action);
        if (id) body.append('transactionId', id);
        body.append('accountId', accountId);
        body.append('categoryId', categoryId);
        body.append('type', type);
        body.append('amount', amount);
        body.append('date', date);
        body.append('description', description);

        const res = await fetch('api/transactions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body: body
        });
        const data = await res.json();

        if (data.success) {
            showToast(data.message, 'success');
            closeModal('modal-transaction');
            
            // Cập nhật lại giao diện tab hiện tại
            if (AppState.activeSection === 'section-dashboard') {
                loadDashboardTab();
            } else {
                loadTransactionsTab();
            }
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Lỗi khi lưu giao dịch!', 'error');
    }
}

async function editTransaction(id) {
    const trans = AppState.transactions.find(t => t.transactionId === id);
    if (trans) {
        openTransactionModal(trans);
    }
}

async function deleteTransaction(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa giao dịch này?')) return;

    try {
        const body = new URLSearchParams();
        body.append('action', 'delete');
        body.append('transactionId', id);

        const res = await fetch('api/transactions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body: body
        });
        const data = await res.json();

        if (data.success) {
            showToast(data.message, 'success');
            if (AppState.activeSection === 'section-dashboard') {
                loadDashboardTab();
            } else {
                loadTransactionsTab();
            }
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Lỗi khi xóa giao dịch!', 'error');
    }
}

// --- MODAL NGÂN SÁCH ---
async function openBudgetModal(bud = null) {
    const title = document.getElementById('budget-modal-title');
    const form = document.getElementById('budget-form');
    form.reset();

    // 1. Tải các danh mục chi tiêu (Expense)
    const resCat = await fetch('api/categories?type=EXPENSE');
    const categories = await resCat.json();
    const selectCat = document.getElementById('budget-category');
    selectCat.innerHTML = '';
    categories.forEach(c => {
        selectCat.innerHTML += `<option value="${c.categoryId}">${c.categoryName}</option>`;
    });

    if (bud) {
        title.innerText = 'Sửa hạn mức ngân sách';
        document.getElementById('budget-id').value = bud.budgetId;
        
        // Ẩn chọn danh mục & thời gian khi sửa (đúng theo nghiệp vụ của controller chỉ cho sửa hạn mức)
        document.getElementById('budget-cat-group').style.display = 'none';
        document.getElementById('budget-date-group').style.display = 'none';
        
        document.getElementById('budget-limit').value = new Intl.NumberFormat('vi-VN').format(bud.budgetAmount);
    } else {
        title.innerText = 'Thiết lập ngân sách chi tiêu';
        document.getElementById('budget-id').value = '';
        
        document.getElementById('budget-cat-group').style.display = 'block';
        document.getElementById('budget-date-group').style.display = 'grid';

        const d = new Date();
        document.getElementById('budget-month').value = d.getMonth() + 1;
        document.getElementById('budget-year').value = d.getFullYear();
    }

    openModal('modal-budget');
}

async function submitBudgetForm(e) {
    e.preventDefault();
    const id = document.getElementById('budget-id').value;
    const limit = document.getElementById('budget-limit').value;

    const action = id ? 'update' : 'add';

    try {
        const body = new URLSearchParams();
        body.append('action', action);
        body.append('limit', limit);
        
        if (id) {
            body.append('budgetId', id);
        } else {
            const categoryId = document.getElementById('budget-category').value;
            const month = document.getElementById('budget-month').value;
            const year = document.getElementById('budget-year').value;
            
            if (!categoryId) {
                showToast('Chưa có danh mục chi tiêu nào được chọn!', 'warning');
                return;
            }
            body.append('categoryId', categoryId);
            body.append('month', month);
            body.append('year', year);
        }

        const res = await fetch('api/budgets', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body: body
        });
        const data = await res.json();

        if (data.success) {
            showToast(data.message, 'success');
            closeModal('modal-budget');
            loadBudgetsTab();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Lỗi khi thiết lập ngân sách!', 'error');
    }
}

async function editBudget(id) {
    const bud = AppState.budgets.find(b => b.budgetId === id);
    if (bud) openBudgetModal(bud);
}

async function deleteBudget(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa thiết lập ngân sách này?')) return;

    try {
        const body = new URLSearchParams();
        body.append('action', 'delete');
        body.append('budgetId', id);

        const res = await fetch('api/budgets', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body: body
        });
        const data = await res.json();

        if (data.success) {
            showToast(data.message, 'success');
            loadBudgetsTab();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Lỗi khi xóa ngân sách!', 'error');
    }
}
