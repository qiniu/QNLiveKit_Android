package com.softsugar.library.sdk.net;

import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.softsugar.library.sdk.Constants;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HeaderInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        // 如果请求头中要加token，切记一定要指定默认值为 "",空字符串
        String token = SPUtils.getInstance().getString(Constants.KEY_TOKEN, "");
        Log.d("lugq", "intercept token:" + token);
        //token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlZTFjNzgxYTVmMmI0YmU2ODA1MDYyNGNmODIyYzQwMCIsImlzcyI6InNkayIsImlhdCI6MTY1MTIwMzA2MiwiZXhwIjoxNjUxMjMxODYyfQ.rTSzhgVAsncVnUx_bxNNqZlPHF65pNWjinlL3g4vMgM";
        Request original = chain.request();
        Request request = original.newBuilder()
                .header("Authorization", token)
                .method(original.method(), original.body())
                .build();
        return chain.proceed(request);
    }
}
