package com.qlive.roomservice;

import com.qlive.core.been.QExtension;
import com.qlive.core.QLiveCallBack;
import com.qlive.core.been.QLiveRoomInfo;
import com.qlive.core.been.QLiveUser;
import com.qlive.core.QLiveService;

import java.util.List;

/**
 * 房间服务
 */
public interface QRoomService extends QLiveService {

    /**
     * 添加监听
     * @param listener
     */
    public void addRoomServiceListener(QRoomServiceListener listener);

    /**
     * 移除监听
     * @param listener
     */
    public void removeRoomServiceListener(QRoomServiceListener listener);

    /**
     * 获取当前房间
     * @return 当前房间信息
     */
    public QLiveRoomInfo getRoomInfo();

    /**
     * 刷新房间信息
     */
    public void getRoomInfo(QLiveCallBack<QLiveRoomInfo> callBack);

    /**
     * 跟新直播扩展信息
     * @param extension 扩展字段
     * @param callBack  操作回调
     */
    public void updateExtension(QExtension extension, QLiveCallBack<Void> callBack);


    /**
     * 当前房间在线用户
     * @param pageNum  页号 1开始
     * @param pageSize 每页大小
     * @param callBack 操作回调
     */
    public void getOnlineUser(int pageNum, int pageSize, QLiveCallBack<List<QLiveUser>> callBack);

    /**
     * 某个房间在线用户
     * @param pageNum  页号 1开始
     * @param pageSize 每页大小
     * @param callBack 操作回调
     * @param roomId  房间ID
     */
    public void getOnlineUser(int pageNum, int pageSize, String roomId, QLiveCallBack<List<QLiveUser>> callBack);

    /**
     * 使用用户ID搜索房间用户
     *
     * @param uid 用户ID
     * @param callBack 操作回调
     */
    public void searchUserByUserId(String uid, QLiveCallBack<QLiveUser> callBack);

    /**
     * 使用用户im uid 搜索用户
     *
     * @param imUid 用户im 用户ID
     * @param callBack 操作回调
     */
    public void searchUserByIMUid(String imUid, QLiveCallBack<QLiveUser> callBack);


}
