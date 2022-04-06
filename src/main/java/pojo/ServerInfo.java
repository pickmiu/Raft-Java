package pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
@Data
@AllArgsConstructor
public class ServerInfo {
    /**
     * 下一次发送给follower的Index
     */
    private int nextIndex;
    /**
     * leader更新commitIndex的依据
     * notice: matchIndex更新后需要check commitIndex
     */
    private int matchIndex;
}