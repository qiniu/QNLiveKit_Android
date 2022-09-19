package com.qlive.linkmicservice;

import com.qlive.avparam.QMixStreaming;

import java.util.List;

/**
 * 混流适配器
 */
public  interface QLinkMicMixStreamAdapter {

    /**
     * 连麦开始如果要自定义混流画布和背景
     * 返回空则主播推流分辨率有多大就多大默认实现
     *
     * @return QMixStreamParams
     */
    QMixStreaming.MixStreamParams onMixStreamStart();

    /**
     * 混流布局适配
     *
     * @param micLinkers 变化后所有连麦者
     * @param target     当前变化的连麦者
     * @param isJoin     当前变化的连麦者是新加还是离开
     * @return 返回重设后的每个连麦者的混流布局
     */
    List<QMixStreaming.MergeOption> onResetMixParam(List<QMicLinker> micLinkers, QMicLinker target, boolean isJoin);
}