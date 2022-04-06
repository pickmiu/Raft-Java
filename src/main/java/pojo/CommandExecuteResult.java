package pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
@Data
@AllArgsConstructor
public class CommandExecuteResult {
    private boolean success;
    private String errMsg;
    private String value;
}