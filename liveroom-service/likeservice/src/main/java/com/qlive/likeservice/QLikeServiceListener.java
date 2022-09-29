package com.qlive.likeservice;

public interface QLikeServiceListener {
    /**
     * 有人点赞
     *
     * @param like
     */
    void onReceivedLikeMsg(QLike like);
}
