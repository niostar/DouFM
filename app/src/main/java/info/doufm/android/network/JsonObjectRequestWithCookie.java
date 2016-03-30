package info.doufm.android.network;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import info.doufm.android.constans.Constants;

/**
 * Created by lsc on 2014/12/24.
 */
public class JsonObjectRequestWithCookie extends JsonObjectRequest {
    private Map<String, String> mHeaders = new HashMap<>();

    public JsonObjectRequestWithCookie(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, Map<String, String> map) {
        super(Method.POST, url, jsonRequest, listener, errorListener);

    }

    public JsonObjectRequestWithCookie(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
    }


    //拿到客户端发起的http请求的Header
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders;
    }

    //发送请求时，往Header中添加cookie，可以一并发送
    public void setCookie(String cookie) throws AuthFailureError {
        mHeaders.put("User-Agent", Constants.User_Agent);
        mHeaders.put("Cookie", cookie);
    }
}
