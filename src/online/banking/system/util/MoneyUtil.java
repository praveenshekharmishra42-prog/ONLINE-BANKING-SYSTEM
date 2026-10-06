package online.banking.system.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/** Formats money as "Rs. 12,345.00". */
public final class MoneyUtil {

    private MoneyUtil() {
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "Rs. 0.00";
        }
        DecimalFormat df = new DecimalFormat("#,##0.00");   // new object each time: DecimalFormat is not thread-safe
        return "Rs. " + df.format(amount.setScale(2, RoundingMode.HALF_UP));
    }
}
