package com.qlive.uikit;

import com.qlive.sdk.QPage;

/**
 * 房间列表页面
 */
public class RoomListPage implements QPage {

    public final int getCustomLayoutID() {
        return RoomListActivity.Companion.getReplaceLayoutID();
    }

    /**
     * 设置房间列表页面的自定义布局
     * @param layoutID  拷贝kit_activity_room_list.xml 修改后的自定义布局
     */
    public final void setCustomLayoutID(int layoutID) {
        RoomListActivity.Companion.setReplaceLayoutID(layoutID);
    }
}
