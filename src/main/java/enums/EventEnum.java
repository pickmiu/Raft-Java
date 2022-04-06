package enums;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
public enum EventEnum implements IntegerEnum<EventEnum> {
    ElectionTimeOut(0),
    WinElection(1),
    CandidateReceivedValidAppendEntriesRPC(2),
    LeaderReceivedValidAppendEntriesRPC(3),
    LeaderReceivedResponseClaimTermBigger(4);

    private int value;

    EventEnum(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public EventEnum genEnumByIntValue(int intValue) {
        for (EventEnum val : EventEnum.values()) {
            if (val.value == intValue) {
                return val;
            }
        }
        return null;
    }
}