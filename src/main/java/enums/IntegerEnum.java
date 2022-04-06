package enums;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
public interface IntegerEnum<T> {
    /**
     * 获得当前枚举类的int值
     *
     * @return
     */
    int getValue();

    /**
     * 根据intValue生成对应的Enum对象
     *
     * @param intValue
     * @return
     */
    T genEnumByIntValue(int intValue);
}