package pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppendEntriesRPCRequest {
    private int term;
    private String leaderId;
    /**
     * nextIndex及之后的所有entry
     */
    private List<Entry> entries;
    private int prevLogTerm;
    private int prevLogIndex;
    private int leaderCommit;
}