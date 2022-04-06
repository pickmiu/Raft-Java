package util;

import java.math.BigDecimal;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
public class CalculateUtil {
    /**
     * 除法 结果向上取整
     *
     * @param a
     * @param b
     * @return
     */
    public static int divideRoundsUp(int a, int b) {
        BigDecimal aBig = new BigDecimal(a);
        BigDecimal bBig = new BigDecimal(b);
        return (int) Math.ceil(aBig.divide(bBig).doubleValue());
    }
}