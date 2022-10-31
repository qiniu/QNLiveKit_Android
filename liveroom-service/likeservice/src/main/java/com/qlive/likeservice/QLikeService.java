package com.qlive.likeservice;

import com.qlive.core.QLiveCallBack;
import com.qlive.core.QLiveService;

/**
 * 点赞服务
 */
public interface QLikeService extends QLiveService {

    /**
     * 点赞
     *
     * @param count 单次点赞数量
     * @param callback
     */
    void like(int count, QLiveCallBack<QLikeResponse> callback);

    /**
     * 添加点赞监听
     *
     * @param listener
     */
    void addLikeServiceListener(QLikeServiceListener listener);

    /**
     * 移除点赞监听
     * @param listener
     */
    void removeLikeServiceListener(QLikeServiceListener listener);
}

