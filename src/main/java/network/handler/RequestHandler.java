package network.handler;

import fi.iki.elonen.NanoHTTPD;

/**
 * notice: 实现新的RequestHandler后需要在RequestHandlerDispatch里面注入
 * 请求处理器 用于处理对应的url
 * @author Tangliyi (2238192070@qq.com)
 */
public interface RequestHandler {

    NanoHTTPD.Response handle(String jsonData);

    String canHandleUrl();
}