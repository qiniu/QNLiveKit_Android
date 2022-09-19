package com.qlive.core.been;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class QLiveStatistics implements Serializable {

    //1.live 2. Item 3. comment   4. PK连麦  5. 观众连麦
    /**
     * 统计类型 - 直播浏览
     */
    public static int TYPE_LIVE_WATCHER_COUNT=1;
    /**
     * 统计类型 - 商品点击
     */
    public static int TYPE_QItem_CLICK_COUNT=2;
    /**
     * 统计类型 - 聊天弹幕
     */
    public static int TYPE_PUBCHAT_COUNT=3;
    /**
     * 统计类型 - pk
     */
    public static int TYPE_PK_COUNT=4;
    /**
     * 统计类型 - 连麦
     */
    public static int TYPE_LINK_MIC_COUNT=5;

    public String flow;
    public List<Info> info;

    public static class Info implements Serializable {
        public int type;
       @SerializedName(value =  "type_description")
        public String typeDescription;
       @SerializedName(value =  "page_view")
        public int pageView;
       @SerializedName(value =  "unique_visitor")
        public int uniqueVisitor;
    }
}
