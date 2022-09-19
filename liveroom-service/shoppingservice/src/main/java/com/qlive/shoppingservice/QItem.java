package com.qlive.shoppingservice;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 商品信息
 */
public class QItem implements Serializable {
    /**
     * 所在房间ID
     */
   @SerializedName(value =  "live_id")
    public String liveID;
    /**
     * 商品ID
     */
   @SerializedName(value =  "item_id")
    public String itemID;
    /**
     * 商品号
     */
    public int order;
    /**
     * 标题
     */
    public String title;
    /**
     * 商品标签 多个以,分割
     */
    public String tags;
    /**
     * 缩略图
     */
    public String thumbnail;
    /**
     * 链接
     */
    public String link;
    /**
     * 当前价格
     */
   @SerializedName(value =  "current_price")
    public String currentPrice;
    /**
     * 原价
     */
   @SerializedName(value =  "origin_price")
    public String originPrice;
    /**
     * 上架状态
     * 已下架
     * PULLED(0),
     * 已上架售卖
     * ON_SALE(1),
     * 上架不能购买
     * ONLY_DISPLAY(2);
     */
    public int status;
    /**
     * 商品扩展字段
     */
   @SerializedName(value =  "extends")
    public Map<String, String> extensions;

    /**
     * 商品讲解录制信息
     */
    public RecordInfo record;

    /**
     * 商品讲解录制信息
     */
    public static class RecordInfo implements Serializable{

        /**
         * 录制完成
         */
        public static int RECORD_STATUS_FINISHED = 0;
        /**
         * 等待处理
         */
        public static int RECORD_STATUS_WAITING = 1;
        /**
         * 正在生成视频
         */
        public static int RECORD_STATUS_GENERATING = 2;
        /**
         * 失败
         */
        public static int RECORD_STATUS_ERROR = 3;
        /**
         * 正在录制
         */
        public static int RECORD_STATUS_RECORDING = 4;

        /**
         * 录制ID
          */
        public int id;
        /**
         * 播放路径
         */
       @SerializedName(value =  "record_url")
        public String recordURL;
        /**
         * 开始时间戳
         */
        public long start;
        /**
         * 结束时间戳
         */
        public long end;
        /**
         * 状态
         */
        public int status;

        /**
         * 所在直播间ID
         */
       @SerializedName(value =  "live_id")
        public String liveID;
        /**
         * 所在商品ID
         */
       @SerializedName(value =  "item_id")
        public String itemID;
    }
}
