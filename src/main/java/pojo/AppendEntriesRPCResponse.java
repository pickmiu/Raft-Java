package pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
@Data
@AllArgsConstructor
public class AppendEntriesRPCResponse {
    public AppendEntriesRPCResponse(String clientId, int term, boolean success) {
        this.clientId = clientId;
        this.term = term;
        this.success = success;
    }

    private String clientId;
    private int term;
    private boolean success;
    /**
     * require
     * 1.缺失日志 返回缺失的第一个index
     * 2.日志冲突 返回follower在冲突term下第一个entry的index
     */
    private int conflictTerm;
    private int nextIndexSuggest;
}