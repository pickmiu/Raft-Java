package pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
@Data
@AllArgsConstructor
public class RequestVoteRPCResponse {
    private String clientId;
    private int term;
    private boolean voteGranted;
}