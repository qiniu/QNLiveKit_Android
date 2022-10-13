package com.qlive.uikit;

import android.content.Context;

import com.qlive.sdk.QPage;

public class LiveRecordPage implements QPage {

    public final int getCustomLayoutID() {
        return LiveRecordActivity.Companion.getReplaceLayoutID();
    }

    /**
     * 设置房间列表页面的自定义布局
     *
     * @param layoutID 拷贝kit_activity_room_list.xml 修改后的自定义布局
     */
    public final void setCustomLayoutID(int layoutID) {
        LiveRecordActivity.Companion.setReplaceLayoutID(layoutID);
    }

    public void start(Context context) {
        LiveRecordActivity.Companion.start(context);
    }

}
