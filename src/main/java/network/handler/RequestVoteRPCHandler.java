package network.handler;

import com.alibaba.fastjson.JSON;
import fi.iki.elonen.NanoHTTPD;
import pojo.RequestVoteRPCRequest;
import raft.RaftClient;
import raft.RaftClientProvider;

/**
 * 处理 RequestVoteRpc
 *
 * @author Tangliyi (2238192070@qq.com)
 */
public class RequestVoteRPCHandler implements RequestHandler {
    @Override
    public NanoHTTPD.Response handle(String jsonData) {
        RequestVoteRPCRequest requestVoteRPCRequest = JSON.parseObject(jsonData, RequestVoteRPCRequest.class);
        RaftClient raftClient = RaftClientProvider.getRaftClient();
        if (raftClient == null) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", "服务未启动");
        } else {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", JSON.toJSONString(raftClient.requestVoteRPCHandle(requestVoteRPCRequest)));
        }
    }

    @Override
    public String canHandleUrl() {
        return "/requestVoteRpc";
    }
}