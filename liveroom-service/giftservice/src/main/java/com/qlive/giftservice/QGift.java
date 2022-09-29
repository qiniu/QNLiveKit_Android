package com.qlive.giftservice;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

/**
 * 礼物模型
 */
public class QGift implements Serializable {
    @SerializedName(value = "gift_id")
    public int giftID;
    /**
     * 礼物类型
     */
    public int type;
    /**
     * 礼物名称
     */
    public String name;
    /**
     * 礼物金额，0 表示自定义金额
     */
    public int amount;
    /**
     * 礼物图片
     */
    public String img;
    /**
     * 动态效果类型
     */
    @SerializedName(value = "animation_type")
    public String animationType;
    /**
     * 动态效果图片
     */
    @SerializedName(value = "animation_img")
    public String animationImg;
    /**
     * 排序，从小到大排序，相同order 根据创建时间排序',
     */
    public int order;
    /**
     * 创建时间
     */
    @SerializedName(value = "created_at")
    public long createdAt;
    /**
     * 更新时间
     */
    @SerializedName(value = "updated_at")
    public long updatedAt;
    /**
     * 扩展字段
     */
    public Map<String, String> extension;
}
