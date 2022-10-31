package com.qlive.likeservice;

/**
 * 点赞监听
 */
public interface QLikeServiceListener {
    /**
     * 有人点赞
     *
     * @param like
     */
    void onReceivedLikeMsg(QLike like);
}
