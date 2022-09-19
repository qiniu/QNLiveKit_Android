package com.qlive.shoppingservice;

/**
 * 商品状态枚举
 */
public enum QItemStatus {

    /**
     * 已下架
     */
    PULLED(0),
    /**
     * 已上架售卖
     */
    ON_SALE(1),
    /**
     * 上架不能购买
     */
    ONLY_DISPLAY(2);
    private final int value;

    QItemStatus(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
