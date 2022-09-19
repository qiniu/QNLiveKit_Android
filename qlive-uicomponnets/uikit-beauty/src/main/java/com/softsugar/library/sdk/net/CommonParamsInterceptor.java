package com.softsugar.library.sdk.net;

import android.util.Log;
import android.webkit.URLUtil;

import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.SpanUtils;
import com.google.gson.JsonSyntaxException;
import com.softsugar.library.sdk.Constants;
import com.softsugar.library.sdk.utils.ContextHolder;
import com.softsugar.library.sdk.utils.SignUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommonParamsInterceptor implements Interceptor {
    private static final String TAG = "CommonParamsInterceptor";

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        // 1. 获取request
        Request request = chain.request();

        // 2.获取到method
        String method = request.method();

        try {
            if (method.equals("POST")) {
                RequestBody requestBody = request.body();
                if (requestBody instanceof FormBody) {
                    request = toPostForm(request);
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        //最后通过chain.proceed(request)进行返回
        return chain.proceed(request);
    }

    private Request toPostForm(Request request) {
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        FormBody formBody = (FormBody) request.body();
        if (null == formBody)
            return request;
        //把原来的参数添加到新的构造器，（因为没找到直接添加，所以就new新的）
        String appId = "";
        String data = "";
        for (int i = 0; i < formBody.size(); i++) {
            bodyBuilder.addEncoded(formBody.encodedName(i), formBody.encodedValue(i));
            if (formBody.encodedName(i).equals("appId")) {
                appId = formBody.encodedValue(i);
                Log.d(TAG, "toPostForm: appId:" + appId);
            }
            if (formBody.encodedName(i).equals("data")) {
                byte[] ss = formBody.encodedValue(i).getBytes();
                data = new String(URLUtil.decode(ss));
                Log.d(TAG, "加密后的data值:" + data);
            }
        }
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = DeviceUtils.getUniqueDeviceId();
        String sign = "";
        try {
            sign = getSignValueForAuth(timestamp, uuid, request, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        formBody = bodyBuilder
                .add("timestamp", timestamp)
                .add("sdkVersion", Constants.SDK_VERSION)
                .add("appVersion", Constants.APP_VERSION)
                .add("uuid", uuid)
                .add("sign", sign)
                .build();

        return request.newBuilder()
                .post(formBody)
                .build();
    }

    private String getSignValueForAuth(String timestamp, String uuid, Request request, String data) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("appId", ContextHolder.getAppId());
        params.put("timestamp", timestamp);
        params.put("sdkVersion", Constants.SDK_VERSION);
        params.put("appVersion", Constants.APP_VERSION);
        params.put("uuid", uuid);
        if (request.url().toString().contains("materials/list")) {
            params.put("data", data);
        }

        // 进行升序排序
        params = sortMapByKey(params);
        // 对参数进行拼接 a=1&b=2
        // 把生成签名的key追加在最后
        String sdkKey = ContextHolder.getAppKey();
        Log.d(TAG, "getSignValueForAuth: " + request.url().toString());
        if (request.url().toString().contains("materials/list")) {
            sdkKey = SPUtils.getInstance().getString(Constants.KEY_SDK_KEY);
        }
        Log.d(TAG, "getSignValueForAuth: appkey:" + sdkKey);
        String str = SignUtils.prepareParams(params, false) + sdkKey;
        Log.d(TAG, "sign加密前拼接:" + str);
        // SHA1
        return SignUtils.encryptToSHA(str);
    }

    public static Map<String, Object> sortMapByKey(Map<String, Object> map) {
        Map<String, Object> sortMap = new TreeMap<String, Object>();
        Set<String> keySet = map.keySet();
        Iterator<String> iter = keySet.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            sortMap.put(key, map.get(key));
        }
        return sortMap;
    }
}
