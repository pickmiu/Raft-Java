package network.handler;

import com.alibaba.fastjson.JSON;
import config.Config;
import fi.iki.elonen.NanoHTTPD;
import pojo.Command;
import pojo.CommandExecuteResult;
import pojo.Entry;
import raft.Follower;
import raft.Leader;
import raft.RaftClientProvider;

/**
 * 处理远程命令
 *
 * @author Tangliyi (2238192070@qq.com)
 */
public class ExecuteCommandHandler implements RequestHandler {

    @Override
    public NanoHTTPD.Response handle(String jsonData) {
        Command command = JSON.parseObject(jsonData, Command.class);
        // if command operation is reading. past
        // else 1. append the command to its log as a new entry.
        //      2. issues AppendEntries RPCs in parallel to each of the other servers to replicate the entry.
        //          when the majority of the servers have been replicated. leader commits all preceding entries in its log

        if ("get".equals(command.getOperate())) {
            // 直接从数据库中读取值
            return null;
        } else {
            if (RaftClientProvider.getRaftClient() instanceof Leader) {
                Config.logEntryManager.addEntry(command);
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", JSON.toJSONString(new CommandExecuteResult(true, null, null)));
            } else {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", JSON.toJSONString(new CommandExecuteResult(false, "当前节点不是leader节点，只支持get命令", null)));
            }
        }
    }

    @Override
    public String canHandleUrl() {
        return "/executeCommand";
    }
}