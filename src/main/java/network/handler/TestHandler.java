package network.handler;

import fi.iki.elonen.NanoHTTPD;

/**
 * 测试
 *
 * @author Tangliyi (2238192070@qq.com)
 */
public class TestHandler implements RequestHandler {

    @Override
    public NanoHTTPD.Response handle(String jsonData) {
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", "hello too");
    }

    @Override
    public String canHandleUrl() {
        return "/hello";
    }
}