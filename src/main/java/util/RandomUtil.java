package util;

import java.util.Random;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
public class RandomUtil {

    public static int generateRandom(int min, int max) {
        Random random = new Random();
        return min + random.nextInt(max - min + 1);
    }

    public static String generateFixLengthRandom(int min, int max) {
        int num = generateRandom(min, max);
        String numString = Integer.toString(num);
        int length = Integer.toString(max).length();
        StringBuilder numStringStringBuilder = new StringBuilder(numString);
        for (int i = 0; i < length - numString.length(); i++) {
            numStringStringBuilder = new StringBuilder("0").append(numStringStringBuilder);
        }
        return numStringStringBuilder.toString();
    }
}