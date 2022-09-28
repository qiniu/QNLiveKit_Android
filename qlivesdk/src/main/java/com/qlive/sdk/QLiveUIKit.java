package com.qlive.sdk;

import android.content.Context;

import com.qlive.docannotations.QDOC;

/**
 * UIkit客户端
 */
public interface QLiveUIKit {
    /**
     * 获取内置页面
     * 每个页面有相应的UI配置
     * @param pageClass 页面的类 目前子类为 RoomListPage-> 房间列表页面 RoomPage->直播间页面
     * @param <T>
     * @return T  <T extends QPage>
     */
    <T extends QPage> T getPage(Class<T> pageClass);

    /**
     * 跳转到直播列表页面
     * @param context 安卓上下文
     */
    void launch(Context context);
}
