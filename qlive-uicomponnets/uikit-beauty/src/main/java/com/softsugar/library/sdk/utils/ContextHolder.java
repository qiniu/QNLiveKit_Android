package com.softsugar.library.sdk.utils;

import android.content.Context;

public class ContextHolder {

    private static String appId;
    private static String appKey;

    private static Context ApplicationContext;

    public static void initial(Context context) {
        ApplicationContext = context;
    }

    public static Context getContext() {
        return ApplicationContext;
    }

    public static String getAppId() {
        return appId;
    }

    public static void setAppId(String appId) {
        ContextHolder.appId = appId;
    }

    public static String getAppKey() {
        return appKey;
    }

    public static void setAppKey(String appKey) {
        ContextHolder.appKey = appKey;
    }
}
