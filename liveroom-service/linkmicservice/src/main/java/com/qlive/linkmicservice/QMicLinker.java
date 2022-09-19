package com.qlive.linkmicservice;
import com.google.gson.annotations.SerializedName;
import com.qlive.core.been.QLiveUser;
import java.io.Serializable;
import java.util.HashMap;

/**
 * 连麦用户
 */
public class QMicLinker implements Serializable {

    /**
     * 麦上用户资料
     */
    public QLiveUser user;
   @SerializedName(value =  "live_id")
    /**
     * 连麦用户所在房间ID
     */
    public String userRoomID;
    /**
     * 扩展字段
     */
   @SerializedName(value =  "extends")
    public HashMap<String, String> extension;

    /**
     * 是否开麦克风
     */
   @SerializedName(value =  "mic")
    public boolean isOpenMicrophone;
    /**
     * 是否开摄像头
     */
   @SerializedName(value =  "camera")
    public boolean isOpenCamera;
}

