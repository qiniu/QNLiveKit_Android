package com.softsugar.library.sdk.net;

import androidx.annotation.NonNull;

import com.softsugar.library.sdk.utils.ReadAssetsUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FakeApkInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        return mockModifyName(chain);
    }

    private Response mockModifyName(Chain chain) {
        String json = ReadAssetsUtils.getJson("mock/data.json");
        return new Response.Builder()
                .code(200)
                .addHeader("Content-Type", "application/json")
                .body(ResponseBody.create(MediaType.parse("application/json"), json))
                .message(json)
                .request(chain.request())
                .protocol(Protocol.HTTP_2)
                .build();
    }
}
