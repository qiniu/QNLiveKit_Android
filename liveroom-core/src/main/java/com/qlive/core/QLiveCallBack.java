package com.qlive.core;

/**
 * 基础回调函数
 *
 * @param <T>
 */
public interface QLiveCallBack<T> {

    /**
     * 操作失败
     *
     * @param code 错误码
     * @param msg  消息
     */
    void onError(int code, String msg);

    /**
     * 操作成功
     *
     * @param data 数据
     */
    void onSuccess(T data);

}
