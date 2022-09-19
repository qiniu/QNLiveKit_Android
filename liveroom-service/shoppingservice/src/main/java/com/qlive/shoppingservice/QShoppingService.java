package com.qlive.shoppingservice;

import com.qlive.core.QLiveCallBack;
import com.qlive.core.QLiveService;
import com.qlive.core.been.QExtension;

import java.util.HashMap;
import java.util.List;

/**
 * 购物服务
 */
public interface QShoppingService extends QLiveService {

    /**
     * 获取直播间所有商品
     *
     * @param callBack 回调
     */
    void getItemList(QLiveCallBack<List<QItem>> callBack);

    /**
     * 跟新商品状态
     *
     * @param itemID   商品ID
     * @param status   商品状态
     * @param callBack 回调
     */
    void updateItemStatus(String itemID, QItemStatus status, QLiveCallBack<Void> callBack);

    void updateItemStatus(HashMap<String, QItemStatus> newStatus, QLiveCallBack<Void> callBack);

    /**
     * 跟新商品扩展字段 并通知房间所有人
     *
     * @param item      商品
     * @param extension 扩展字段
     * @param callBack  回调
     */
    void updateItemExtension(QItem item, QExtension extension, QLiveCallBack<Void> callBack);

    /**
     * 设置讲解中的商品 并通知房间所有人
     *
     * @param item     商品
     * @param callBack 回调
     */
    void setExplaining(QItem item, QLiveCallBack<Void> callBack);

    /**
     * 取消设置讲解中的商品 并通知房间所有人
     *
     * @param callBack 回调
     */
    void cancelExplaining(QLiveCallBack<Void> callBack);

    /**
     * 获取 当前讲解中的
     *
     * @return 当前讲解中的
     */
    QItem getExplaining();

    /**
     * 跟新单个商品顺序
     *
     * @param param    调节顺序
     * @param callBack 回调
     */
    void changeSingleOrder(QSingleOrderParam param, QLiveCallBack<Void> callBack);

    /**
     * 跟新单个商品顺序
     *
     * @param params   所有商品 调节后的顺序
     * @param callBack 回调
     */
    void changeOrder(List<QOrderParam> params, QLiveCallBack<Void> callBack);

    /**
     * 删除商品
     *
     * @param itemIDS
     * @param callBack
     */
    void deleteItems(List<String> itemIDS, QLiveCallBack<Void> callBack);

    /**
     * 添加购物服务监听
     *
     * @param listener 监听
     */
    void addServiceListener(QShoppingServiceListener listener);

    /**
     * 移除商品监听
     *
     * @param listener 监听
     */
    void removeServiceListener(QShoppingServiceListener listener);

    /**
     * 开始录制正在讲解的商品
     * @param callBack 回调
     */
    void startRecord(QLiveCallBack<Void> callBack);

    /**
     * 删除讲解中的商品
     *
     * @param recordIds 商品ID列表
     * @param callBack   回调
     */
    void deleteRecord(List<Integer> recordIds, QLiveCallBack<Void> callBack);

    void statsQItemClick(QItem item);
}




