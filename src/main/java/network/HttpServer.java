package network;

import fi.iki.elonen.NanoHTTPD;
import lombok.extern.slf4j.Slf4j;
import network.handler.RequestHandler;

import java.io.IOException;

/**
 * Http服务器用于监听请求
 *
 * @author Tangliyi (2238192070@qq.com)
 */
@Slf4j
public class HttpServer extends NanoHTTPD {
    private final RequestHandlerDispatch requestHandlerDispatch = new RequestHandlerDispatch();

    public HttpServer(int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        log.info("[op:main] http server start in port {}", port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            RequestHandler requestHandler = requestHandlerDispatch.dispatch(session);
            if (requestHandler == null) {
                // 没有url match
                return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404");
            }
            return requestHandlerDispatch.dispatchAndHandle(session);
        } catch (Exception e) {
            log.error("[op:serve] catch-exception", e);
            return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "服务器异常");
        }
    }

}