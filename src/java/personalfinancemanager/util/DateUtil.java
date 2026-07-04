package personalfinancemanager.util;

import personalfinancemanager.config.AppConstants;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Tiện ích xử lý và định dạng ngày tháng.
 */
public class DateUtil {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT);
    private static final SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.DATE_FORMAT);

    static {
        sdf.setLenient(false);
    }

    /**
     * Định dạng LocalDate thành chuỗi dạng dd/MM/yyyy.
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(dtf);
    }

    /**
     * Định dạng java.sql.Date thành chuỗi dạng dd/MM/yyyy.
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return sdf.format(date);
    }

    /**
     * Chuyển đổi chuỗi dd/MM/yyyy thành java.sql.Date.
     */
    public static Date parseDate(String text) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        java.util.Date parsed = sdf.parse(text.trim());
        return new Date(parsed.getTime());
    }

    /**
     * Chuyển đổi chuỗi dd/MM/yyyy thành LocalDate.
     */
    public static LocalDate parseLocalDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), dtf);
        } catch (DateTimeParseException e) {
            System.err.println("Lỗi parse LocalDate: " + text);
            return null;
        }
    }

    /**
     * Chuyển đổi java.sql.Date sang LocalDate.
     */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toLocalDate();
    }

    /**
     * Chuyển đổi LocalDate sang java.sql.Date.
     */
    public static Date toSqlDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return Date.valueOf(date);
    }

    /**
     * Lấy ngày đầu tiên của tháng hiện tại.
     */
    public static Date getStartOfMonth() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        return toSqlDate(start);
    }

    /**
     * Lấy ngày cuối cùng của tháng hiện tại.
     */
    public static Date getEndOfMonth() {
        LocalDate now = LocalDate.now();
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());
        return toSqlDate(end);
    }
}
