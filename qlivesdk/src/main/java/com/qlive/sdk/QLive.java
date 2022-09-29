package com.qlive.sdk;

import android.content.Context;

import com.qlive.core.QLiveConfig;
import com.qlive.core.been.QLiveUser;
import com.qlive.sdk.internal.QLiveCoreDelegate;
import com.qlive.core.QTokenGetter;
import com.qlive.core.QLiveCallBack;
import com.qlive.playerclient.QPlayerClient;
import com.qlive.pushclient.QPusherClient;

import org.jetbrains.annotations.NotNull;

/**
 * 低代码直播客户端
 */
public class QLive {

    private static final QLiveCoreDelegate delegate = new QLiveCoreDelegate();
    /**
     * 初始化
     *
     * @param context     安卓上下文
     * @param config      sdk配置
     * @param tokenGetter token获取
     */
    public static void init(Context context, QLiveConfig config, QTokenGetter tokenGetter) {
       delegate.init(context, config, tokenGetter);
        getLiveUIKit();
    }

    /**
     * 登陆 认证成功后才能使用qlive的功能
     *
     * @param callBack 操作回调
     * @apiNote 认证成功后才能使用qlive的功能
     */
    public static void auth(@NotNull QLiveCallBack<Void> callBack) {
       delegate.login(callBack);
    }

    /**
     * 跟新用户信息
     *
     * @param userInfo 用户参数
     * @param callBack 回调函数
     */
    public static void setUser(@NotNull QUserInfo userInfo, @NotNull QLiveCallBack<Void> callBack) {
        QLiveUser user = new QLiveUser();
        user.avatar = userInfo.avatar;
        user.nick = userInfo.nick;
        user.extensions = userInfo.extension;
       delegate.setUser(user, callBack);
    }

    /**
     * 获取当前登陆用户资料
     *
     * @return QLiveUser
     */
    public static QLiveUser getLoginUser() {
        return delegate.getLiveUser();
    }

    /**
     * 创建推流客户端
     *
     * @return QPusherClient
     */
    public static QPusherClient createPusherClient() {
        return delegate.createPusherClient();
    }

    /**
     * 创建拉流客户端
     *
     * @return QPlayerClient
     */
    public static QPlayerClient createPlayerClient() {
        return delegate.createPlayerClient();
    }

    /**
     * 获取房间管理接口
     *
     * @return QRooms
     */
    public static QRooms getRooms() {
        return delegate.getQRooms();
    }

    /**
     * 获得UIkit
     *
     * @return QLiveUIKit
     */
    public static QLiveUIKit getLiveUIKit() {
        return delegate.getUIKIT();
    }
}
