package com.qlive.sdk;

import com.qlive.core.QLiveCallBack;
import com.qlive.core.been.QCreateRoomParam;
import com.qlive.core.been.QLiveRoomInfo;
import com.qlive.core.been.QLiveStatistics;
import com.qlive.giftservice.QGift;
import com.qlive.giftservice.QGiftStatistics;

import java.util.List;

/**
 * 房间管理接口
 */
public interface QRooms {

    /**
     * 创建房间
     *
     * @param param    创建房间参数
     * @param callBack
     */
    void createRoom(QCreateRoomParam param, QLiveCallBack<QLiveRoomInfo> callBack);

    /**
     * 删除房间
     *
     * @param roomID   房间ID
     * @param callBack
     */
    void deleteRoom(String roomID, QLiveCallBack<Void> callBack);

    /**
     * 根据ID获取房间信息
     *
     * @param roomID   房间ID
     * @param callBack
     */
    void getRoomInfo(String roomID, QLiveCallBack<QLiveRoomInfo> callBack);

    /**
     * 房间列表
     *
     * @param pageNumber
     * @param pageSize
     * @param callBack
     */
    void listRoom(
            int pageNumber,
            int pageSize,
            QLiveCallBack<List<QLiveRoomInfo>> callBack
    );

    /**
     * 我的直播记录
     *
     * @param pageNumber
     * @param pageSize
     * @param callBack
     */
    void liveRecord(int pageNumber,
                    int pageSize,
                    QLiveCallBack<List<QLiveRoomInfo>> callBack);

    /**
     * 获取直播间数据统计
     *
     * @param roomID
     * @param callBack
     */
    void getLiveStatistics(String roomID, QLiveCallBack<QLiveStatistics> callBack);


    /**
     * 获取礼物配置
     */
    public void getGiftConfig(int type, QLiveCallBack<List<QGift>> callback);

    /**
     * 获取直播间礼物统计
     */
    public void getLiveGiftStatistics(String roomID, int pageNumber, int pageSize, QLiveCallBack<List<QGiftStatistics>> callback);

    /**
     * 获取主播礼物统计
     */
    public void getAnchorGiftStatistics(int pageNumber, int pageSize, QLiveCallBack<List<QGiftStatistics>> callback);

    /**
     * 获取用户礼物统计
     */
    public void getUserGiftStatistics(int pageNumber, int pageSize, QLiveCallBack<List<QGiftStatistics>> callback);

}
