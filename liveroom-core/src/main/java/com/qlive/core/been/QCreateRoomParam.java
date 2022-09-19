package com.qlive.core.been;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 创建房间参数
 */
public class QCreateRoomParam implements Serializable {

    /**
     * 房间标题
     */
    public String title;
    /**
     * 房间公告
     */
   @SerializedName(value =  "notice")
    public String notice;
    /**
     * 封面
     */
   @SerializedName(value =  "cover_url")
    public String coverURL;
    /**
     * 扩展字段
     */
   @SerializedName(value =  "extension")
    public HashMap<String, String> extension;
}
