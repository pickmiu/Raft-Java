package enums;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
public enum RoleEnum implements IntegerEnum<RoleEnum>{
    FOLLOWER(1),
    CANDIDATE(2),
    LEADER(3);

    private int value;

    RoleEnum(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public RoleEnum genEnumByIntValue(int intValue) {
        for (RoleEnum val: RoleEnum.values()) {
            if (val.value == intValue) {
                return val;
            }
        }
        return null;
    }
}