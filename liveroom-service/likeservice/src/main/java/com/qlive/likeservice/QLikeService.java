package com.qlive.likeservice;

import com.qlive.core.QLiveCallBack;

/**
 * 点赞服务
 */
public interface QLikeService {

    /**
     * 点赞
     *
     * @param count
     * @param callback
     */
    void like(int count, QLiveCallBack<QLikeResponse> callback);

    /**
     * 添加点赞监听
     *
     * @param listener
     */
    void addLikeServiceListener(QLikeServiceListener listener);

    void removeLikeServiceListener(QLikeServiceListener listener);
}

