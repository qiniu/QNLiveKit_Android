

#### 事件处理组件  内置的处理事件

```xml
    <!-- 用户申请连麦监听 -->
    <com.qlive.uikitlinkmic.FuncCPTLinkMicApplyMonitor
        android:layout_width="0dp"
        android:layout_height="0dp" />
    
    <!-- pk连麦监听  -->
    <com.qlive.uikitpk.FuncCPTPKApplyMonitor
        android:layout_width="0dp"
        android:layout_height="0dp" />
    
    <!-- 房间状态变化监听  功能组件-->
    <com.qlive.uikit.component.FuncCPTRoomStatusMonitor
        android:layout_width="0dp"
        android:layout_height="0dp" />
    
    <!-- 房主掉线结束页面功能组件-->
    <com.qlive.uikit.component.FuncCPTAnchorStatusMonitor
        android:layout_width="0dp"
        android:layout_height="0dp" />
    
    <!-- 手机虚拟按键事件拦截 -->
    <com.qlive.uikit.component.FuncCPTDefaultKeyDownMonitor
        android:layout_width="0dp"
        android:layout_height="0dp" />
    
    <!-- 被主播主动邀请监听  -->
    <com.qlive.uikitlinkmic.FuncCPTBeInvitedLinkMicMonitor
        android:layout_width="0dp"
        android:layout_height="0dp" />
```
####  UI组件
##### 底层预览类

```xml
    <!-- 房间背景图-->
    <com.qlive.uikitcore.QKitImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/kit_dafault_room_bg" />

    <!-- 主播自己的播放器预览-->
    <com.qlive.rtclive.QPushTextureView
        android:id="@+id/preTextureView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <!-- pk两个主播预览-->
    <com.qlive.uikitpk.PKAnchorPreview
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <!--  连麦者预览-->
    <com.qlive.uikitlinkmic.MicLinkersView
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:layout_gravity="end"
        tools:layout_width="100dp" />

    <!--  关闭房间按钮-->
    <com.qlive.uikit.component.CloseRoomView
        android:layout_width="32dp"
        android:layout_height="32dp"
        android:layout_gravity="top|end"
        android:layout_marginTop="40dp"
        android:layout_marginEnd="8dp"
        android:orientation="vertical"
        android:padding="10dp"
        android:src="@mipmap/kit_ic_close_white"
        tools:ignore="MissingDefaultResource"
        tools:visibility="visible" />
```

##### 房间信息展示类

```xml

    <!-- 房主信息-->
    <com.qlive.uikituser.RoomHostView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp" />

    <!--  房主开始预告中的房间按钮-->
    <com.qlive.uikit.component.AnchorStartTrailerLiveView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginEnd="12dp" />

    <!-- 开播数据统计-->
    <com.qlive.uikit.component.LiveStatisticsView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp"
        android:textColor="@color/white"
        android:textSize="9sp" />
    
    <!--  在线用户-->
    <com.qlive.uikituser.OnlineUserView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        tools:background="#ee8811" />
    
    <!-- 在线人数-->
    <com.qlive.uikituser.RoomMemberCountView
        android:layout_width="wrap_content"
        android:layout_height="32dp"
        android:layout_marginStart="2dp"
        android:layout_marginEnd="44dp"
        android:background="@drawable/kit_shape_40000000_52"
        android:gravity="center"
        android:minWidth="32dp"
        android:orientation="vertical"
        android:paddingStart="6dp"
        android:paddingEnd="6dp"
        android:text="1"
        android:textColor="#ffffff"
        android:textSize="14sp" />
    
    <!--  房间ID展示 -->
    <com.qlive.uikituser.RoomIdView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="end"
        android:layout_marginTop="3dp"
        android:layout_marginEnd="8dp"
        android:gravity="end"
        android:orientation="vertical"
        android:textColor="#ffffff"
        android:textSize="6sp"
        tools:text="aaaa" />

    <!-- 主播讲解中商品卡片-->
    <com.qlive.uikitshopping.ExplainingQItemCardView
        android:layout_width="113dp"
        android:layout_height="171dp"
        android:layout_gravity="bottom|end"
        android:layout_marginEnd="8dp"
        android:layout_marginBottom="80dp" />

    <!-- 房间公告-->
    <com.qlive.uikitpublicchat.RoomNoticeView
        android:layout_width="238dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp"
        android:layout_marginBottom="2dp"
        android:background="@drawable/kit_shape_40000000_6"
        android:orientation="vertical"
        android:paddingStart="8dp"
        android:paddingTop="16dp"
        android:paddingEnd="8dp"
        android:paddingBottom="16dp"
        android:textColor="#ffffff"
        android:textSize="13sp" />

    <!--  公屏聊天列表-->
    <com.qlive.uikitpublicchat.PublicChatView
        android:layout_width="238dp"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        tools:background="#44000000"
        tools:layout_height="150dp" />

    <!-- 弹幕轨道展示-->
    <com.qlive.uikitdanmaku.DanmakuTrackManagerView
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <!--  礼物轨道展示-->
    <com.qlive.uikitgift.GiftTrackManagerView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_gravity="center"
        android:gravity="center_vertical" />
```    
    

##### 功能栏目类
    
```xml
    <!--  聊天输入框-->
    <com.qlive.uikitpublicchat.InputView
        android:layout_width="0dp"
        android:layout_height="32dp"
        android:layout_marginStart="8dp"
        android:layout_marginEnd="8dp"
        android:layout_weight="1"
        android:background="@drawable/kit_shape_40000000_16">
        <ImageView
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:layout_gravity="center_vertical"
            android:layout_marginStart="8dp"
            android:src="@mipmap/kit_ic_show_input"
            tools:ignore="ContentDescription" />
    </com.qlive.uikitpublicchat.InputView>

    <!-- 发弹幕-->
    <com.qlive.uikitdanmaku.SendDanmakuView
        android:layout_width="32dp"
        android:layout_height="32dp"
        android:layout_marginEnd="4dp"
        android:orientation="vertical"
        android:src="@mipmap/kit_ic_go_danmaku"
        tools:ignore="MissingDefaultResource" />

    <!--  开始pk-->
    <com.qlive.uikitpk.StartPKView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="4dp"
        android:layout_marginEnd="4dp" />

    <!-- 购物车小黄车-->
    <com.qlive.uikitshopping.GoShoppingImgView
        android:layout_width="32dp"
        android:layout_height="32dp"
        android:layout_marginStart="4dp"
        android:layout_marginEnd="4dp"
        android:orientation="vertical"
        android:src="@mipmap/kit_ic_go_shopping" />

    <!--  点赞 -->
    <com.qlive.uikitlike.LikeView
        android:layout_width="32dp"
        android:layout_height="37dp"
        android:layout_marginStart="4dp"
        android:layout_marginEnd="4dp"
        android:orientation="vertical" />
    
    <!--  更多工具栏 -->
    <com.qlive.uikit.component.BottomMoreFuncButton
        android:layout_width="32dp"
        android:layout_height="32dp"
        android:layout_marginStart="4dp"
        android:layout_marginEnd="8dp"
        android:src="@mipmap/kit_ic_more" />

    <!--  美颜贴纸 -->
   <com.qlive.uikit.component.ShowStickerBeautyView
       android:layout_width="wrap_content"
       android:layout_height="wrap_content"/>
    
    <!--  美颜特效 -->
   <com.qlive.uikit.component.ShowBeautyView
       android:layout_width="wrap_content"
       android:layout_height="wrap_content"/>
```

##### ktv
    
```xml    
    <!-- 歌词显示 -->
    <com.qlive.uikitktv.QLrcView
        app:lrcCurrentTextColor="#EF4149"
        app:lrcDividerHeight="20dp"
        app:lrcLabel=" "
        app:lrcNormalTextColor="#99FFFFFF"
        app:lrcNormalTextSize="16sp"
        app:lrcTextGravity="center"
        app:lrcTextSize="26sp"
        android:layout_width="match_parent"
        android:layout_height="100dp"
        android:layout_marginTop="124dp"
        android:background="#00000000"
        android:padding="12dp"
        android:paddingStart="10dp"
        android:paddingEnd="10dp"
        android:paddingBottom="20dp" />

    <!-- 点歌-->
    <com.qlive.uikitktv.KTVControlView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="end"
        android:layout_marginTop="250dp"
        android:layout_marginEnd="16dp" />
```
  
```xml    
    <!-- 开播预览 -->
    <com.qlive.uikit.component.LivePreView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:alpha="0.1">
        <!-- 预览页面的返回-->
        <com.qlive.uikit.component.QBackRoomNavigationImg
            android:layout_width="40dp"
            android:layout_height="20dp"
            android:layout_gravity="end"
            android:layout_marginTop="62dp"
            android:paddingStart="10dp"
            android:paddingEnd="10dp"
            android:src="@mipmap/kit_close_room"
            tools:ignore="TouchTargetSizeCheck,SpeakableTextPresentCheck" />

        <!-- 预览页面美颜-->
        <com.qlive.uikit.component.ShowBeautyPreview
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="bottom"
            android:layout_marginStart="24dp"
            android:layout_marginBottom="54dp"
            android:drawableTop="@mipmap/kit_preview_ic_beauty"
            android:gravity="center_horizontal"
            android:orientation="vertical"
            android:text="@string/beauty"
            android:textColor="@color/white"
            tools:ignore="TouchTargetSizeCheck" />

        <!--  切换摄像头-->
        <com.qlive.uikit.component.SwitchCameraView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="bottom|end"
            android:layout_marginEnd="24dp"
            android:layout_marginBottom="54dp"
            android:drawableTop="@mipmap/kit_preview_ic_swith"
            android:gravity="center_horizontal"
            android:orientation="vertical"
            android:text="@string/camera_switch"
            android:textColor="@color/white"
            tools:ignore="TouchTargetSizeCheck" />
    </com.qlive.uikit.component.LivePreView>
```
