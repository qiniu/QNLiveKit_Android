# 歌词LrcView
主要负责歌词的显示，支持上下拖动调整进度。

- com.qlive.uikitktv.QLrcView : 已经实现QLiveComponent和QKTVServiceListener的歌词业务组件，能自动同步房间歌曲和歌词进度
- com.qlive.uikitktv.LrcView.LrcView ：无业务实现的格式UI组件
## xml中使用
```
<com.qlive.uikitktv.QLrcView
 android:id="@+id/lrcView"
 android:layout_width="match_parent"
 android:layout_height="match_parent"
 android:paddingStart="10dp"
 android:paddingTop="20dp"
 android:paddingEnd="10dp"
 android:paddingBottom="20dp"
 app:lrcCurrentTextColor="@color/ktv_lrc_highligh"
 app:lrcDividerHeight="20dp"
 app:lrcLabel=" "
 app:lrcNormalTextColor="@color/ktv_lrc_nomal"
 app:lrcNormalTextSize="16sp"
 app:lrcTextGravity="center"
 app:lrcTextSize="26sp" />
```

|xml参数|说明|代码|
|----|----|----|
|lrcCurrentTextColor|歌词高亮下颜色|setCurrentColor|
|lrcNormalTextColor|歌词正常下颜色|setNormalColor|
|lrcTextSize|文字高亮下大小|setCurrentTextSize|
|lrcNormalTextSize|文字正常下大小|setNormalTextSize|
|lrcLabel|没有歌词下默认文字|setLabel|
|lrcTextGravity|歌词对齐方式|无|
|lrcDividerHeight|歌词上下之间间距|无|

## 主要API
|API|说明|
|----|----|
|setActionListener|绑定事件回调，用于接收运行中的事件|
|setTotalDuration|设置音乐总长度，单位毫秒|
|loadLrc|加载本地歌词文件|
|setEnableDrag|设置是否允许上下滑动|
|updateTime|更新进度，单位毫秒|
|hasLrc|是否有歌词文件|
|reset|重置内部状态，清空已经加载的歌词|


