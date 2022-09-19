package com.qlive.linkmicservice;

import com.qlive.avparam.QPushRenderView;
import com.qlive.core.QInvitationHandler;
import com.qlive.core.been.QExtension;
import com.qlive.core.QLiveCallBack;
import com.qlive.core.QLiveService;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 连麦服务
 */
public interface QLinkMicService extends QLiveService {

    /**
     * 获取当前房间所有连麦用户
     * @return 所有连麦者信息
     */
    List<QMicLinker> getAllLinker();

    /**
     * 设置某人的连麦视频预览
     * 麦上用户调用  上麦后才会使用切换成rtc连麦 下麦后使用拉流预览
     * @param uID 用户ID
     * @param preview 预览窗口
     */
    void setUserPreview(String uID, QPushRenderView preview);

    /**
     * 踢人
     * @param uID 用户ID
     * @param msg 附加消息
     * @param callBack  操作回调
     */
    void kickOutUser(String uID, String msg, QLiveCallBack<Void> callBack);

    /**
     * 跟新扩展字段
     * @param micLinker 麦位置
     * @param QExtension 扩展字段
     */
    void updateExtension(@NotNull QMicLinker micLinker, QExtension QExtension, QLiveCallBack<Void> callBack);

    /**
     * 添加麦位监听
     * @param listener  麦位监听
     */
    void addMicLinkerListener(QLinkMicServiceListener listener);

    /**
     * 移除麦位监听
     * @param listener 麦位监听
     */
    void removeMicLinkerListener(QLinkMicServiceListener listener);

    /**
     * 获得连麦邀请处理
     * @return QInvitationHandler 邀请处理
     */
    QInvitationHandler getInvitationHandler();

    /**
     * 观众向主播连麦处理器
     * @return QAudienceMicHandler
     */
    QAudienceMicHandler getAudienceMicHandler();

    /**
     * 主播处理自己被连麦处理器
     * @return QAnchorHostMicHandler
     */
    QAnchorHostMicHandler getAnchorHostMicHandler();
}
