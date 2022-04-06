import util.CalculateUtil;
import util.RandomUtil;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
public class TestMainApplication {
    public static void testRandom() {
        for (int i = 0; i < 1000; i++) {
            System.out.println(RandomUtil.generateFixLengthRandom(0, 9999));
        }
    }

    public static void testCalculateUtil() {
        System.out.println(CalculateUtil.divideRoundsUp(11,2));
    }

    public static void main(String[] args) {
        testCalculateUtil();

    }
}