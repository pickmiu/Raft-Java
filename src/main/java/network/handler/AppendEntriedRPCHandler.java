package network.handler;

import com.alibaba.fastjson.JSON;
import fi.iki.elonen.NanoHTTPD;
import pojo.AppendEntriesRPCRequest;
import raft.RaftClient;
import raft.RaftClientProvider;

/**
 * 处理 AppendEntiesRpc 请求
 *
 * @author Tangliyi (2238192070@qq.com)
 */
public class AppendEntriedRPCHandler implements RequestHandler {

    @Override
    public NanoHTTPD.Response handle(String jsonData) {
        AppendEntriesRPCRequest appendEntriesRPCRequest = JSON.parseObject(jsonData, AppendEntriesRPCRequest.class);
        RaftClient raftClient = RaftClientProvider.getRaftClient();
        if (raftClient == null) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", "服务未启动");
        } else {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", JSON.toJSONString(raftClient.appendEntryRPCHandle(appendEntriesRPCRequest)));
        }
    }

    @Override
    public String canHandleUrl() {
        return "/appendEntriesRpc";
    }
}