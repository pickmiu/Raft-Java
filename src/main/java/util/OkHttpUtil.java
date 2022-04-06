package util;

import com.alibaba.fastjson.JSONObject;
import config.Config;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;

import static config.Config.JSON;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
public class OkHttpUtil {
    public static RequestBody buildRequestBody(Object object) {
        return RequestBody.create(JSONObject.toJSONString(object), JSON);
    }

    public static Call getCall(String url, RequestBody requestBody) {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return Config.OkHttpClient.newCall(request);
    }
}