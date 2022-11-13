package com.qlive.uikit;

import android.content.Context;

import com.qlive.avparam.QCameraParam;
import com.qlive.avparam.QMicrophoneParam;
import com.qlive.core.QLiveCallBack;
import com.qlive.core.been.QLiveRoomInfo;
import com.qlive.sdk.QLive;
import com.qlive.sdk.QPage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.jvm.internal.Intrinsics;

public class RoomPage implements QPage {

    public QCameraParam cameraParam = new QCameraParam();
    public QMicrophoneParam microphoneParam = new QMicrophoneParam();

    public int getAnchorCustomLayoutID() {
        return RoomPushActivity.Companion.getReplaceLayoutId();
    }

    /**
     * 自定义布局 如果需要替换自定义布局
     * 自定义主播端布局 如果需要替换自定义布局
     *
     * @param anchorCustomLayoutID 自定义布局ID
     */
    public void setAnchorCustomLayoutID(int anchorCustomLayoutID) {
        RoomPushActivity.Companion.setReplaceLayoutId(anchorCustomLayoutID);
    }

    public int getPlayerCustomLayoutID() {
        return RoomPlayerActivity.Companion.getReplaceLayoutId();
    }

    /**
     * 自定义布局 如果需要替换自定义布局
     * 自定义主播端布局 如果需要替换自定义布局
     *
     * @param playerCustomLayoutID 自定义布局ID
     */
    public void setPlayerCustomLayoutID(int playerCustomLayoutID) {
        RoomPlayerActivity.Companion.setReplaceLayoutId(playerCustomLayoutID);
    }

    /**
     * 根据房间信息自动跳转主播页直播间或观众直播间
     *
     * @param context  安卓上下文
     * @param roomInfo 房间信息
     * @param callBack 回调
     */
    public final void startRoomActivity(@NotNull Context context, @NotNull QLiveRoomInfo roomInfo, @Nullable QLiveCallBack<QLiveRoomInfo> callBack) {
        startRoomActivity(context, roomInfo, null, callBack);
    }

    /**
     * 根据房间信息自动跳转主播页直播间或观众直播间 并且带有自定义 Intent
     *
     * @param context
     * @param roomInfo
     * @param extSetter
     * @param callBack
     */
    public final void startRoomActivity(@NotNull Context context, @NotNull QLiveRoomInfo roomInfo, @Nullable StartRoomActivityExtSetter extSetter, @Nullable QLiveCallBack<QLiveRoomInfo> callBack) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(roomInfo, "roomInfo");
        String var10002;
        if (Intrinsics.areEqual(roomInfo.anchor.userId, QLive.getLoginUser().userId)) {
            var10002 = roomInfo.liveID;
            Intrinsics.checkNotNullExpressionValue(var10002, "roomInfo.liveID");
            RoomPushActivity.Companion.start(context, var10002, extSetter, callBack);
        } else {
            var10002 = roomInfo.liveID;
            Intrinsics.checkNotNullExpressionValue(var10002, "roomInfo.liveID");
            RoomPlayerActivity.Companion.start(context, var10002, extSetter, callBack);
        }
    }

    /**
     * 跳转观众直播间
     *
     * @param context    安卓上下文
     * @param liveRoomId 房间ID
     * @param callBack   回调
     */
    public final void startPlayerRoomActivity(@NotNull Context context, @NotNull String liveRoomId, @Nullable QLiveCallBack<QLiveRoomInfo> callBack) {
        startPlayerRoomActivity(context, liveRoomId, null, callBack);
    }

    /**
     * 跳转观众直播间 并且带有自定义 Intent
     *
     * @param context
     * @param liveRoomId
     * @param extSetter
     * @param callBack
     */
    public final void startPlayerRoomActivity(@NotNull Context context, @NotNull String liveRoomId, @Nullable StartRoomActivityExtSetter extSetter, @Nullable QLiveCallBack<QLiveRoomInfo> callBack) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(liveRoomId, "liveRoomId");
        RoomPlayerActivity.Companion.start(context, liveRoomId, extSetter, callBack);
    }

    /**
     * 跳转已经存在的主播直播间
     *
     * @param context    安卓上下文
     * @param liveRoomId 直播间ID
     * @param callBack   回调
     */
    public final void startAnchorRoomActivity(@NotNull Context context, @NotNull String liveRoomId, @Nullable QLiveCallBack<QLiveRoomInfo> callBack) {
        startAnchorRoomActivity(context, liveRoomId, null, callBack);
    }

    /**
     * 跳转已经存在的主播直播间 并且带有自定义 Intent
     *
     * @param context
     * @param liveRoomId
     * @param extSetter
     * @param callBack
     */
    public final void startAnchorRoomActivity(@NotNull Context context, @NotNull String liveRoomId, @Nullable StartRoomActivityExtSetter extSetter, @Nullable QLiveCallBack<QLiveRoomInfo> callBack) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(liveRoomId, "liveRoomId");
        RoomPushActivity.Companion.start(context, liveRoomId, extSetter, callBack);
    }

    /**
     * 跳转到创建直播间开播页面
     *
     * @param context  安卓上下文
     * @param callBack 回调
     */
    public final void startAnchorRoomWithPreview(@NotNull Context context, @Nullable QLiveCallBack<QLiveRoomInfo> callBack) {
        startAnchorRoomWithPreview(context, null, callBack);
    }

    /**
     * 跳转到创建直播间开播页面 并且带有自定义 Intent
     *
     * @param context
     * @param extSetter 自定义参数
     * @param callBack
     */
    public final void startAnchorRoomWithPreview(@NotNull Context context, @Nullable StartRoomActivityExtSetter extSetter, @Nullable QLiveCallBack<QLiveRoomInfo> callBack) {
        Intrinsics.checkNotNullParameter(context, "context");
        RoomPushActivity.Companion.start(context, extSetter, callBack);
    }

}
