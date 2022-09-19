package com.qlive.pkservice;

import com.qlive.avparam.QMixStreaming;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * pk混流适配器
 */
public interface QPKMixStreamAdapter {
    /**
     * 当pk开始 如何混流
     *
     * @param pkSession
     * @return 返回混流参数
     */
    List<QMixStreaming.MergeOption> onPKLinkerJoin(@NotNull QPKSession pkSession);

    /**
     * pk开始时候混流画布变成多大
     *返回null则原来主播有多大就有多大
     * @param pkSession
     * @return 混流背景参数
     */
    QMixStreaming.MixStreamParams onPKMixStreamStart(@NotNull QPKSession pkSession);

    /**
     * 当pk结束后如果还有其他普通连麦者 如何混流
     * 如果pk结束后没有其他连麦者 则不会回调
     *
     * @return 混流参数
     */
    default List<QMixStreaming.MergeOption> onPKLinkerLeft() {
        return new ArrayList<QMixStreaming.MergeOption>();

    }
    /**
     * 当pk结束后如果还有其他普通连麦者 如何混流 如果pk结束后没有其他连麦者 则不会回调 返回空则默认之前的不变化
     * @return 混流背景参数
     */
    default QMixStreaming.MixStreamParams onPKMixStreamStop() {
        return null;
    }

}
