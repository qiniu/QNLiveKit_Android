package com.qlive.pkservice;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

public class PKInfo implements Serializable {

   @SerializedName(value =  "init_room_id")
    public String initRoomId;
   @SerializedName(value =  "stop_at")
    public String stopAt;
   @SerializedName(value =  "start_at")
    public String startAt;
   @SerializedName(value =  "created_at")
    public long createdAt;
   @SerializedName(value =  "id")
    public String id;
   @SerializedName(value =  "init_user_id")
    public String initUserId;
   @SerializedName(value =  "recv_user_id")
    public String recvUserId;
   @SerializedName(value =  "extensions")
    public Map<String, String> extensions;
   @SerializedName(value =  "status")
    public Integer status;
   @SerializedName(value =  "recv_room_id")
    public String recvRoomId;
   
}
