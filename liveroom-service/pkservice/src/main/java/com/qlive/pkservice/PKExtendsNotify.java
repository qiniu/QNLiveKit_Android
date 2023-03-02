package com.qlive.pkservice;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

class PKExtendsNotify implements Serializable {

    /**
     * sid : 1001
     * init_room_id : 1001
     * recv_room_id : 1001
     * extends : {"age":33}
     */
    public String sid;
    public String init_room_id;
    public String recv_room_id;
    @SerializedName("extends")
    public HashMap<String,String> extendsX;

}
