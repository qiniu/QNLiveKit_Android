package com.qlive.core;

/**
 * token获取回调
 * 当token过期后自动调用getTokenInfo
 */
public interface QTokenGetter{
    /**
     * 如何获取token
     * @param callback 业务（同步/异步）获取后把结果通知给sdk
     */
    void getTokenInfo( QLiveCallBack<String> callback);
}