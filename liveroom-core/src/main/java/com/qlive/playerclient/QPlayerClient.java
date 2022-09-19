package com.qlive.playerclient;


import com.qlive.avparam.QPlayerEventListener;
import com.qlive.avparam.QPlayerRenderView;
import com.qlive.core.QClientType;
import com.qlive.core.QLiveCallBack;
import com.qlive.core.QLiveClient;
import com.qlive.core.QLiveService;
import com.qlive.core.QLiveStatusListener;
import com.qlive.core.been.QLiveRoomInfo;

import org.jetbrains.annotations.NotNull;

/**
 * 拉流客户端
 */
public interface QPlayerClient extends QLiveClient {


    /**
     * 获取插件服务实例
     *
     * @param serviceClass 插件的类
     * @param <T>
     * @return 返回 服务插件对象
     */
    @Override
    <T extends QLiveService> T getService(Class<T> serviceClass);

    /**
     * 设置直播状态回调
     *
     * @param liveStatusListener 直播事件监听
     */
    @Override
    void addLiveStatusListener(QLiveStatusListener liveStatusListener);

    @Override
    void removeLiveStatusListener(QLiveStatusListener liveStatusListener);

    /**
     * 当前客户端类型
     * QClientType.PUSHER 代表推流端
     * QClientType.PLAYER 代表拉流端
     *
     * @return QClientType
     */
    @Override
    QClientType getClientType();

    /**
     * 加入房间
     *
     * @param roomID   房间ID
     * @param callBack 回调
     */
    void joinRoom(String roomID, QLiveCallBack<QLiveRoomInfo> callBack);        //加入房间

    /**
     * 离开房间
     * 离开后可继续加入其他房间 如上下滑动切换房间
     *
     * @param callBack 回调
     */
    void leaveRoom(QLiveCallBack<Void> callBack);                               //关闭房间

    /**
     * 销毁释放资源
     * 离开房间后退出页面不再使用需要释放
     */
    @Override
    void destroy();                                                              //销毁

    /**
     * 设置预览窗口
     * 内置 QPlayerTextureRenderView(推荐)/ QSurfaceRenderView
     *
     * @param renderView 预览窗口
     */
    void play(@NotNull QPlayerRenderView renderView);                            //绑定播放器渲染视图

    /**
     * 暂停
     */
    void pause();

    /**
     * 恢复
     */
    void resume();

    /**
     * 添加播放器事件监听
     *
     * @param playerEventListener 播放器事件监听
     */
    void addPlayerEventListener(QPlayerEventListener playerEventListener);       //设置拉流端事件回调

    /**
     * 移除播放器事件监听
     *
     * @param playerEventListener 播放器事件监听
     */
    void removePlayerEventListener(QPlayerEventListener playerEventListener);       //设置拉流端事件回调

}


