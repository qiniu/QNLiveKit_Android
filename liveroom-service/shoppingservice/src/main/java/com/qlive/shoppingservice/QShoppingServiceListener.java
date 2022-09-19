package com.qlive.shoppingservice;

import com.qlive.core.been.QExtension;

/**
 * 购物车服务监听
 */
public interface QShoppingServiceListener {
    /**
     * 正在展示的商品切换通知
     *
     * @param item 商品
     */
    void onExplainingUpdate(QItem item);

    /**
     * 商品扩展字段跟新通知
     *
     * @param item      商品
     * @param extension 扩展字段
     */
    void onExtensionUpdate(QItem item, QExtension extension);

    /**
     * 主播操作了商品列表 商品列表变化
     */
    void onItemListUpdate();
}