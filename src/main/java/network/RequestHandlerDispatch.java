package network;

import network.handler.*;
import fi.iki.elonen.NanoHTTPD;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 分发器 用于给接收到的请求分发对应的处理器
 *
 * @author Tangliyi (2238192070@qq.com)
 */
@Slf4j
public class RequestHandlerDispatch {

    private Map<String, RequestHandler> handlerMap = new HashMap(2);

    public RequestHandlerDispatch() {
        // 注入handler
        injection(new AppendEntriedRPCHandler());
        injection(new RequestVoteRPCHandler());
        injection(new ExecuteCommandHandler());
        injection(new TestHandler());
    }

    private void injection(RequestHandler requestHandler) {
        handlerMap.put(requestHandler.canHandleUrl(), requestHandler);
    }

    public RequestHandler dispatch(NanoHTTPD.IHTTPSession session) {
        return handlerMap.get(session.getUri());
    }

    public NanoHTTPD.Response dispatchAndHandle(NanoHTTPD.IHTTPSession session) {
        String jsonData = null;
        if (session.getMethod().equals(NanoHTTPD.Method.POST)) {
            Map<String, String> files = new HashMap<>();
            try {
                session.parseBody(files);
            } catch (Exception e) {
                log.error("[op:dispatchAndHandle] catch-exception", e);
            }
            jsonData = files.get("postData");
        }
        return handlerMap.get(session.getUri()).handle(jsonData);
    }
}