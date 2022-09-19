package com.qlive.core.been;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户
 */
public class QLiveUser implements Serializable {

    /**
     * 用户ID
     */
   @SerializedName(value =  "user_id")
    public String userId;
    /**
     * 用户头像
     */
    public String avatar;
    /**
     * 名字
     */
    public String nick;
    /**
     * 扩展字段
     */
   @SerializedName(value =  "extends")
    public Map<String, String> extensions = new HashMap<>();
    /**
     * 用户im id
     */
   @SerializedName(value =  "im_userid")
    public String imUid;
    /**
     * 用户Im名称
     */
    public String im_username;
}
