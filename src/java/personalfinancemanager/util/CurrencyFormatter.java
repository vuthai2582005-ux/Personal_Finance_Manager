package personalfinancemanager.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Tiện ích định dạng hiển thị tiền tệ VND.
 */
public class CurrencyFormatter {
    private static final DecimalFormat formatter;

    static {
        // Thiết lập định dạng kiểu Việt Nam: dùng dấu chấm "." phân cách hàng nghìn
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        symbols.setMonetaryDecimalSeparator(',');
        
        formatter = new DecimalFormat("#,##0", symbols);
    }

    /**
     * Định dạng số tiền thành chuỗi hiển thị kèm ký hiệu "₫".
     * Ví dụ: 1500000 -> "1.500.000 ₫"
     */
    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }
        return formatter.format(amount) + " ₫";
    }

    public static String format(double amount) {
        return format(BigDecimal.valueOf(amount));
    }

    /**
     * Chuyển đổi chuỗi nhập liệu (có thể chứa dấu chấm phân cách) thành BigDecimal.
     * Ví dụ: "1.500.000" -> 1500000
     */
    public static BigDecimal parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            // Loại bỏ ký tự "₫", dấu chấm "." phân cách hàng nghìn và khoảng trắng
            String cleanText = text.replace("₫", "")
                                   .replace(".", "")
                                   .replace(",", "")
                                   .trim();
            return new BigDecimal(cleanText);
        } catch (NumberFormatException e) {
            System.err.println("Lỗi parse tiền tệ: " + text);
            return BigDecimal.ZERO;
        }
    }
}
