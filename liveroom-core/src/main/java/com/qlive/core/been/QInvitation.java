package com.qlive.core.been;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 邀请信息
 */
public class QInvitation implements Serializable {

    /**
     * 发起方
     */
    public QLiveUser initiator;
    /**
     * 接收方
     */
    public QLiveUser receiver;
    /**
     * 发起方所在房间ID
     */
    @SerializedName(value = "initiatorRoomId")
    public String initiatorRoomID;
    /**
     * 接收方所在房间ID
     */
    @SerializedName(value = "receiverRoomId")
    public String receiverRoomID;
    /**
     * 扩展字段
     */
    public HashMap<String, String> extension;
    /**
     * 邀请ID
     */
    @Expose(serialize = false, deserialize = false)
    public int invitationID;

}