package com.qlive.shoppingservice;

import java.io.Serializable;

/**
 * 单个商品调节顺序
 */
public class QSingleOrderParam {
    /**
     * 商品ID
     */
    public String itemID;
    /**
     * 原来的顺序
     */
    public int from;
    /**
     * 调节后的顺序
     */
    public int to;
}
