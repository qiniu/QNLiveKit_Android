package com.qlive.linkmicservice;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 主播端连麦器
 */
public interface QAnchorHostMicHandler {
    /**
     * 设置混流适配器
     *
     * @param QLinkMicMixStreamAdapter  混流适配器
     */
    public void setMixStreamAdapter( QLinkMicMixStreamAdapter QLinkMicMixStreamAdapter);
}
