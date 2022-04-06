package pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestVoteRPCRequest {
    private int term;
    private String candidateId;
    private int lastLogIndex;
    private int lastLogTerm;
}