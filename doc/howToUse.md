
```                  
                                                   
                              +---------------+     +---> RoomListPage //房间列表UI实现页面
                              |               |     |
                          +---+   QLiveUIKIT  +--- -+
                          |   |               |     |
                          |   +---------------+     +---> RoomPage    //直播间页面UI实现
                          |       uikit sdk   
                          | 
                          |                        
                          |                         
                          |                         +---> createRoom  //创建房间接口
                          |   +---------------+     |
+----------------------+  |   |               |     +---> listRoom    //房间列表接口
|                      |  +---+     QRooms    +-----+
|      QLive           |  |   |               |     +---> deleteRoom  //删除房间接口
|                      |  |   +---------------+     |
+----------------------+  |       房间管理接口        +---> getRoomInfo //获取房间信息接口
                          |
                          | 
                          | 
                          |                         +--->  QChatRoomService //聊天室服务 
                          |   +----------------+    |
                          |   |                |    +--->  QLinkMicService  //连麦业务服务
                          +---+   QLiveClient   +---+
                              |                |    +--->  QPKService       //pk业务服务
                              +----------------+    |
                                 推拉流房间客户端      +--->  QPublicChatService //房间里公屏消息服务       
                                   无UI版本sdk       |
                                                    +--->  QRoomService     //房间频道业务 
                                                    |    
                                                    +--->  QDanmakuService  //弹幕服务 
                                                    | 
                                                    +--->  QShoppingService //电商购物服务 
                                                    |
                                                    +--->  QGiftService //礼物服务  
                                                    |
                                                    +--->  QLikeService //点赞服务                                                                                                         

```    

## sdk接入

1 下载sdk
[下载地址](https://github.com/qiniu/QNLiveKit_Android/tree/main/app-sdk)

2 参考dome工程的build.gradle文件 配置aar


```
//七牛imsdk 必选  
implementation project(':app-sdk:depends_sdk_qnim')  
//七牛rtc 主播推流必选  观众要连麦必选
implementation project(':app-sdk:depends_sdk_qrtc') 
//七牛播放器  观众拉流端必选 
implementation project(':app-sdk:depends_sdk_piliplayer') 
//低代码无ui sdk 必选
implementation project(':app-sdk:qlive-sdk') 
//okhttp 3版本以上
implementation 'com.squareup.okhttp3:okhttp:4.2.2' 
//json
implementation 'com.google.code.gson:gson:2.8.9' 
```

```
 //UIkit包 不使用UI则不需要依赖
implementation project(':app-sdk:qlive-sdk-uikit') 
//谷歌官方UI组件
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.3.0'
implementation 'androidx.recyclerview:recyclerview:1.1.0'
implementation 'androidx.cardview:cardview:1.0.0'
//图片加载
implementation 'com.github.bumptech.glide:glide:4.11.0' 
//如果没有开启 viewbinding 选项 务必依赖这个库
implementation "androidx.databinding:viewbinding:7.1.2"
```

> UIkit也可以直接使用源码模块-可直接修改代码
> #### implementation project(':qlive-uikit')

3 混淆配置
如果你的项目需要混淆 [qlivesdk混淆配置参考](https://github.com/qiniu/QNLiveKit_Android/blob/main/app/proguard-rules.pro
)

## UIKIT使用说明
### 初始化
```kotlin
//初始化
QLive.init(this, QLiveConfig()) { callback ->
  //业务方如何获取token  在token过期和登陆时候会回调该方法
  getLoginToken(callback)
}
//登陆 
QLive.auth(object : QLiveCallBack<Void> {
  override fun onError(code: Int, msg: String?) {}
  override fun onSuccess(data: Void) {}
})
//可选 绑定用户资料 第一次绑定后没有跟新个人资料可不用每次都绑定
//也可以在服务端绑定用户 服务端也可以调用
val ext = HashMap<String, String>()
// 可选参数，接入用户希望在直播间的在线用户列表/资料卡等UI中显示自定义字段如vip等级等等接入方的业务字段
ext.put("vip", "1")
ext.put("xxx", "xxx")
//跟新/绑定 业务端的用户信息
QLive.setUser(QUserInfo("your avatar", "your nick", ext), object : QLiveCallBack<Void> {
  override fun onError(code: Int, msg: String?) {}
  override fun onSuccess(data: Void?) {}
})
val liveUIKit = QLive.getLiveUIKit()
//跳转到直播列表页面
liveUIKit.launch(context)
```
> 登陆成功后才能使用sdk

### 主动跳转到直播间
```kotlin
//主动跳转观众直播间
QLive.getLiveUIKit().getPage(RoomPage::class.java).startPlayerRoomActivity(...)
//主播主动跳转已经存在主播直播间
QLive.getLiveUIKit().getPage(RoomPage::class.java).startAnchorRoomActivity(...)
//自定义开播跳转预览创建直播间页面
QLive.getLiveUIKit().getPage(RoomPage::class.java).startAnchorRoomWithPreview(...)
```

### 定制UI
#### 修改现有的UI组件

![alt ](http://qrnlrydxa.hn-bkt.clouddn.com/qlive5.png)

**直接修改开源代码**
uikit使用源码依赖，直接修改源码
> 优点：快捷
> 缺点：官方更新UI逻辑代码后不方便同步

**无侵入式自定义UI**
uikit使用sdk依赖


拷贝布局文件--只需拷贝需要修改的页面，拷贝至接入的工程并且重新命名
- kit_activity_room_player.xml //[观众直播间布局 ](https://github.com/qiniu/QNLiveKit_Android/blob/main/liveroom-uikit/src/main/res/layout/kit_activity_room_player.xml)
- kit_activity_room_pusher.xm  //[主播直播间布局](https://github.com/qiniu/QNLiveKit_Android/blob/main/liveroom-uikit/src/main/res/layout/kit_activity_room_pusher.xml)
- kit_activity_room_list.xml   //[房间列表页布局](https://github.com/qiniu/QNLiveKit_Android/blob/main/liveroom-uikit/src/main/res/layout/kit_activity_room_list.xml)

clear重新编译编译-->androidStudio预览看到如上效果图

- 1修改拷贝文件的布局任意属性，比如边距，文本颜色，样式等等
- 2调用替换布局文件
```kotlin
val roomPage = QLive.getLiveUIKit().getPage(RoomPage::class.java)
//自定义房间页面观众房间的布局
roomPage.playerCustomLayoutID = R.layout.customXXXlayout
//自定义房间页面主播房间的布局
roomPage.anchorCustomLayoutID = R.layout.customXXlayout

val roomListPage = QLive.getLiveUIKit().getPage(RoomListPage::class.java)
//自定义房间列表页面布局
roomListPage.customLayoutID = R.layout.customXlayout
```

修改拷贝的布局文件或者源布局文件

案列：
```xml
<!--   找到房间背景图-->
<com.qlive.uikitcore.QKitImageView
    android:layout_width="match_parent"
    android:layout_height="match_parent"

    android:src="@drawable/kit_dafault_room_bg" />

    <!--替换-->
<com.qlive.uikitcore.QKitImageView
    android:layout_width="match_parent"
    android:layout_height="match_parent"

    android:src="@drawable/my_room_bg" />
```
> tip: 所有的安卓自带基础UI都可以修改属性 如边距,父容器排列，文本颜色等等

如果要替换UI里面的逻辑代码
创建自定义UI组件 继承QLiveComponent
案列：
```kotlin
//自定义一个公告UI组件--参考原来的公告实现
class CustomNoticeView :FrameLayout, com.qlive.uikit.QLiveComponent {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        //自己的公告布局
        LayoutInflater.from(context).inflate(R.layout.customnoticeview,this,true)
    }

    //绑定UI组件上下文 context中包涵UI实现安卓平台功能的字段如activity fragmentManager
    override fun attachKitContext(context: com.qlive.uikit.QLiveUIKitContext) {}
    //绑定房间客户端 通过client可以获取业务实现
    override fun attachLiveClient(client: QLiveClient) {}
    //进入回调 在这个阶段可以提前根据liveId提前初始化一些UI
    override fun onEntering(liveID: String, user: QLiveUser) { }

    //加入回调 房间加入成功阶段 已经拿到了QLiveRoomInfo
    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeFromFloating: Boolean) {
        //设置房间公告文本 公告文本roomInfo中字段取出
        tvNotice.setText("房间公告："+roomInfo.notice)
    }
    //离开回调
    override fun onLeft() {
        //房间切换/离开 清空UI
        tvNotice.setText("")
    }
    // 销毁
    override fun onDestroyed() { }
    //安卓activity生命周期 安卓页面 onresume onpause等等
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {}
}
//自定义UI可以参考原来的实现修改成自定义实现
//提示：所有的UI组件不需要在activity绑定操作 只需要继承QLiveComponent就能完成所有工作
```
然后在拷贝布局文件里或者源码布局里替换原来内置的UI组件
```xml
    <!--   原来的UI组件-->
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
    android:textSize="13sp"
    tools:text="官方公告" />

    <!--   改成你自己的-->
<CustomNoticeView
    android:layout_width="238dp"
    android:layout_height="wrap_content"/>
```
> 提示不需要修改activity

#### 删除内置UI组件
直接在拷贝的布局文件或者源码布局里里删除

案列：去掉pk功能
在xml里直接删除开始pk按钮

```xml
   <!--    去掉开始pk按钮-->
<com.qlive.uikitpk.StartPKView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />

    <!--    去掉pk预览-->
<com.qlive.uikitpk.PKAnchorPreview
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```
> UI插件的增删改不需要修改activity

#### 添加UI组件

```kotlin
class CustomView :FrameLayout, com.qlive.uikit.QLiveComponent {
    //  实现自己额外的多个UI布局
}
//在拷贝的布局文件里或者源码布局里你想要的位置添加即可

```

#### 自定义房间列表页面
如果需要将房间列表页面添加到你想要的页面比如app首页的viewpager切换，否则直接替换修改内在roomList布局就可以了

方式1 使用RoomListView

在想要添加的布局xml里面添加RoomListView
```xml
<!--  empty_placeholder_tips   没有数据的占位提示-->
<!--  empty_placeholder_no_net_icon     没有网络的占位图片-->
<!--  empty_placeholder_icon    没有数据的占位图片-->
<com.qlive.uikit.component.RoomListView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:empty_placeholder_icon="@drawable/kit_pic_empty"
    app:empty_placeholder_no_net_icon="@drawable/kit_pic_empty_network"
    app:empty_placeholder_tips="空空如也" />

```
在你的activivity代码里配置样式
```kotlin
//可选 替换列表item适配
roomListView.setRoomAdapter(CustomAdapter())
//必选 启动 
roomListView.attachKitContext(QUIKitContext(this,supportFragmentManager,this,this))
```

方式2 使用 qlive数据api
```kotlin
//使用 qlive数据api 获取房间等数据自定义UI
QLive.getRooms().listRoom(..）
QLive.getRooms().createRoom(..）
```
方式3 修改UIkit源码

### UI插件通信事件
如选择不修改源码的形式接入uikit，如何在不同模块间UI插件实现通信

```kotlin
//定义一个UI事件 继承UIEvent
public class TestUIEvent extends UIEvent{
    //自定义多个字段
    public int testInt = 2;
}
```

在某个UI组件里注册事件
```
//注册TestUIEvent
registerEventAction(TestUIEvent::class.java) { 
    //事件数据
    event:TestUIEvent ->
    //收到测试事件1的回调
    Log.d("UIEvent",event.getAction()+" "+event.testInt)
    }
 //注册其他更多UIEvent       
registerEventAction(xxxUIEvent::class.java) {event:xxxUIEvent ->
   //收到测试事件xxx的回调
   Log.d("UIEvent",event.getAction())
  }
```
在另外一个UI组件里发送事件
```
sendUIEvent(TestUIEvent().apply {testInt = 1000;})
```


### 使用美颜插件（可选）
```
拷贝源码模块uikit-beauty并且添加依赖
implementation project(":qlive-uicomponnets:uikit-beauty")
```
- (必选) 联系七牛商务获取美颜认证lic文件 重命名SenseME.lic放在assets文件下->运行sdk已经带了美颜滤镜贴纸功能
- (可选 -自定义调节UI面板) 修改美颜调节面板UI及拷贝购买的额外的素材文件/或删除原有用不着的素材至uikit-beauty/assets
- (可选 -自定义显示美颜弹窗按钮)uikit中调用   kitContext?.getLiveFuncComponent(FuncCPTBeautyDialogShower::class.java)?.showBeautyEffectDialog() 即可显示美颜特效弹窗
- (可选 -自定义显示贴纸弹窗按钮)uikit中调用  kitContext?.getLiveFuncComponent(FuncCPTBeautyDialogShower::class.java)
  ?.showBeautyStickDialog()  即可显示美颜贴纸弹窗

#### 外接其他美颜
```kotlin
client.setVideoFrameListener(object:QVideoFrameListener{
    //拿到帧回调即可以使用其他美颜sdk处理
})
```

### 小窗播放
配置权限
```xml
<!-- 系统悬浮窗权限 -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<!-- 从桌面返回 -->
<uses-permission android:name="android.permission.REORDER_TASKS" />
```
在UIkit中 启动小窗播放
```kotlin
 //FloatingModel.GO_DESKTOP (开始悬浮并返回桌面)
 //FloatingModel.BACK_LAST_PAGE (开始悬浮返回上一个页面 -- 当前activity会销毁-可再次进入或进入新的直播间)
 //FloatingModel.GO_NEXT_PAGE (开始悬浮去下一页面)
 kitContext.getLiveFuncComponent(FuncCPTPlayerFloatingHandler::class.java)?.create(FloatingModel) { succeed: Boolean, msg: String ->
     //小窗后要跳转的页面
}
```
小窗组件功能概览 如需自定义参考配置
```kotlin
//内置悬浮窗播放功能组件 
class FuncCPTPlayerFloatingHandler{
       //静态配置
      companion object {
        //当前正在展示的悬浮窗  
        fun currentFloatingPlayerView(): FloatingPlayerView? 
        /**
         * 自定义权限申请提示
          默认权限申请时候提示用户开始权限，，自定义可以替换
         * Permission request tip call
         * @param afterTipCall 自定义权限申请提示
         * afterTipCall.invoke(true) == 已经提示完成可以去申请
         * afterTipCall.invoke(false) == 不去申请，取消操作
         */
        var permissionRequestTipCall: (kitConText: com.qlive.uikit.QLiveUIKitContext, afterTipCall: (Boolean) -> Unit) -> Unit ={}
     }
  /**
   * 开始悬浮播放
   * @param floatingModel 悬浮窗模式 
   * @param view 自定义小窗UI 默认DefaultFloatingPlayerView
   * @param config  小窗的位置等配置参数
   * @param createCall 成功失败回调
   */
  fun create(
    floatingModel: FloatingModel,
    view: FloatingPlayerView = DefaultFloatingPlayerView(),
    config: FloatConfig = createDefaultFloatConfig(kitContext!!.androidContext),
    createCall: (Boolean, String) -> Unit
  ){}
}
```
###  跳转直播间携带本地参数
如果需要跳转直播携带一些本地的参数。
调用带StartRoomActivityExtSetter参数的启动直播间方法
```kotlin
QLive.getLiveUIKit()
        .getPage(RoomPage::class.java)
        .startXXXXRoomActivity(this,
        "id",
       //自定义本地参数
        object : StartRoomActivityExtSetter {
            override fun setExtParams(startIntent: Intent) {
                //安卓平台Intent的存放参数
                startIntent.putExtra("xxx", "xxx")
            }
        }, null
    )

```
在直播间任意UI插件中通过上下文获取
```kotlin
 val xxx = kitContext.currentActivity.intent.getStringExtra("xxx")
```

### 定制内置UI组件点击事件和样式
每个内置的UI组件都有静态的样式配置和事件回调
如设置直播间在线用户的样式
```
//设置点击事件
OnlineUserView.onItemUserClickListener={
   kitContext, //上下文
   client, //客户端
   view, //点击的UI
   user //点击的用户
   ->
   // todo 跳转用户主页等操作
}
//设置item样式
OnlineUserView.adapterProvider={
   kitContext, //上下文
   client, //客户端
   ->
    CustomOnlineUserViewAdapter() //返回自定义列表适配
}
```



## 无UI-标准集成
```kotlin
//初始化
QLive.init(this, QLiveConfig()) {
    callback ->
    //业务方如何获取token  在token过期和登陆时候会回调该方法
    getLoginToken(callback)
}
//登陆 
QLive.auth(object : QLiveCallBack<Void> {
    override fun onError(code: Int, msg: String?) {}
    override fun onSuccess(data: Void) {}
})
//可选 绑定用户资料 第一次绑定后没有跟新个人资料可不用每次都绑定
//也可以在服务端绑定用户 服务端也可以调用
val ext = HashMap<String, String>()
// 可选参数，接入用户希望在直播间的在线用户列表/资料卡等UI中显示自定义字段如vip等级等等接入方的业务字段
ext.put("vip", "1")
ext.put("xxx", "xxx")
//跟新/绑定 业务端的用户信息
QLive.setUser(QUserInfo("your avatar", "your nick", ext), object : QLiveCallBack<Void> {
    override fun onError(code: Int, msg: String?) {}
    override fun onSuccess(data: Void?) {}
})

//创建房间
val param =  QCreateRoomParam()
param.setTitle("xxxtitle");
QLive.getRooms().createRoom(param, object : QLiveCallBack<QLiveRoomInfo> {
    override fun onError(code: Int, msg: String) {
    }
    override fun onSuccess(data: QLiveRoomInfo) {
    }
})

// 主播推流
//创建推流client
val client = QLive.createPusherClient()
val microphoneParams =  QMicrophoneParam()
microphoneParam.setSampleRate(48000)
//启动麦克风模块
client.enableMicrophone(microphoneParam)
val cameraParam =  QCameraParam()
cameraParam.setFPS(15)
//启动摄像头模块
client.enableCamera(cameraParam,findViewById(R.id.renderView))
//注册房间端监听
client.setLiveStatusListener(object : QLiveStatusListener{})
//加入房间
client.joinRoom(roomId, object : QLiveCallBack<QLiveRoomInfo> {
    override fun onError(code: Int, msg: String?) {
    }
    override fun onSuccess(data: QLiveRoomInfo) {
    }
})
//关闭
client.closeRoom(object : QLiveCallBack<Void> {
    override fun onError(code: Int, msg: String?) {
    }
    override fun onSuccess(data: Void?) {
    }
})
//销毁
client.destroy()



//用户拉流房间
val client = QLive.createPlayerClient()
//注册房间端监听
client.setLiveStatusListener(object : QLiveStatusListener{})
//加入房间
client.joinRoom(roomId, object :
    QLiveCallBack<QLiveRoomInfo> {
    override fun onError(code: Int, msg: String?) {
    }
    override fun onSuccess(data: QLiveRoomInfo) {
    }
})
//播放
client.play(findViewById(R.id.renderView));
//离开
client.leaveRoom(object : QLiveCallBack<Void> {
    override fun onError(code: Int, msg: String?) {
    }
    override fun onSuccess(data: Void?) {
    }
})
//销毁
client.destroy()

```

### 使用插件服务

```kotlin
client.getService(QxxxService::class.java) //获取某个业务插件服务
//案列
client.getService(QPKService::class.java)?.start(20 * 1000, receiverRoomID, receiver.userId, null,object : QLiveCallBack<QPKSession> {})
```

### 无UISDK实现连麦


[多人连麦demo](https://github.com/qiniu/QNLiveKit_Android/blob/main/qlive-uicomponnets/uikit-linkmic/src/main/java/com/qlive/uikitlinkmic/MicLinkersView.kt)

[分屏连麦demo](https://github.com/qiniu/QNLiveKit_Android/blob/main/qlive-uicomponnets/uikit-linkmic/src/main/java/com/qlive/uikitlinkmic/MicLinkerSplitScreenPreview.java)


```kotlin
package com.qlive.qnlivekit


//邀请监听
private val mInvitationListener = object : QInvitationHandlerListener {
    //收到邀请
    override fun onReceivedApply(qInvitation: QInvitation) {
        //todo 显示申请弹窗  qInvitation.getInitiator()获取到申请方资料 显示UI 
        // 点击按钮  拒绝操作
        client!!.getService(QLinkMicService::class.java).invitationHandler.reject(qInvitation.invitationID, null, callBack)
        //点击按钮 接受操作 
        client!!.getService(QLinkMicService::class.java).invitationHandler.accept(qInvitation.invitationID, null, callBack)
    }

    //收到对方取消    
    override fun onApplyCanceled(qInvitation: QInvitation) {}

    //发起超时对方没响应
    override fun onApplyTimeOut(qInvitation: QInvitation) {}

    //对方接受
    override fun onAccept(qInvitation: QInvitation) {
        //对方接受后调用开始上麦 传摄像头麦克参数 自动开启相应的媒体流
        client?.getService(QLinkMicService::class.java)?.audienceMicHandler?.startLink(null, QCameraParam(), QMicrophoneParam(), callback)
    }

    //对方拒绝
    override fun onReject(qInvitation: QInvitation) {}
}

//麦位麦位监听
private val mQLinkMicServiceListener = object : QLinkMicServiceListener {
    //有人上麦了
    override fun onLinkerJoin(micLinker: QMicLinker) {

        //麦上用户和主播设置这个用户预览 直播拉流用户无需设置
        val preview = QPushTextureView(context)
        //添加到你要添加到的容器
        containerView.addView(preview)
        //设置该用户的预览
        linkService.setUserPreview(micLinker.user.userId, preview)

        //跟新连麦UI 如果要显示头像 micLinker里取到上麦者资料
    }

    //有人下麦了
    override fun onLinkerLeft(micLinker: QMicLinker) {
        //移除已经设置的预览窗口
        container.removeUserPreView(xxxx)
        //跟新连麦UI 比如去掉麦上头像
    }

    override fun onLinkerMicrophoneStatusChange(micLinker: QMicLinker) {
        //跟新连麦UI 
    }

    override fun onLinkerCameraStatusChange(micLinker: QMicLinker) {
        //跟新连麦UI
    }

    //某个用户被踢麦
    override fun onLinkerKicked(micLinker: QMicLinker, msg: String) {}

    override fun onLinkerExtensionUpdate(micLinker: QMicLinker, extension: QExtension) {}
}

//混流适配 房主负责混流
private val mQMixStreamAdapter =
    object : QLinkMicMixStreamAdapter {
        /**
         * 连麦开始如果要自定义混流画布和背景
         * 返回空则主播推流分辨率有多大就多大默认实现
         * @return
         */
        override fun onMixStreamStart(): QMixStreamParams? {
            return null
        }

        /**
         * 混流布局适配
         */
        override fun onResetMixParam(
            micLinkers: MutableList<QMicLinker>, //所有麦位置
            target: QMicLinker, //目标麦位
            isJoin: Boolean //是否是加入 否则离开
        ): MutableList<QMergeOption> {
            //将变化后的麦位换算成连麦混流参数  
            //比如 案例(x,y)  ->1号麦位（0，200 ）2号麦位（0，200+180+15）3号麦位（0，200+2*180+15）...
            val ops = ArrayList<QMergeOption>()
            val lastY = 200
            micLinkers.forEachIndex { index, linker ->
                ops.add(QMergeOption().apply {

                    // 该连麦者的用户ID
                    uid = linker.user.userId

                    //该用户的摄像头参数
                    cameraMergeOption = CameraMergeOption().apply {
                        isNeed = true
                        x = 0 //麦位x
                        y = lastY + 180 + 15 //每个麦位 依次往下排 180 + 15 个分辨率间距
                        z = 0
                        width = 180
                        height = 180
                    }
                    //该用户的麦克风参数
                    microphoneMergeOption = MicrophoneMergeOption().apply {
                        isNeed = true
                    }
                })
                lastY = lastY + 180 + 15
            }
        }
    }

//观众端连麦器监听
private val mQAudienceMicHandler = object : QAudienceMicHandler.LinkMicHandlerListener {
    override fun onRoleChange(isLinker: Boolean) {
        if (isLinker) {
            //我切换到了连麦模式 -> 使用连麦和麦上用户和主播互动 延迟更低
            client?.getService(QLinkMicService::class.java)?.allLinker?.forEach { micLinker ->
                //对原来麦上的人设置预览
                val preview = QPushTextureView(context)
                //添加到你要添加到的容器
                containerView.addView(preview)
                //设置该用户的预览
                linkService.setUserPreview(micLinker.user.userId, preview)
            }
        } else {
            //我切换拉流模式
            client?.getService(QLinkMicService::class.java)?.allLinker?.forEach {
                //移除对原来设置麦位移除设置预览view
                removePreview(it.user.userId)
            }
        }
    }
}

//主播设置混流适配  
client!!.getService(QLinkMicService::class.java).anchorHostMicHandler.setMixStreamAdapter( mQMixStreamAdapter  )
//观众设置观众连麦处理监听
client!!.getService(QLinkMicService::class.java).audienceMicHandler.addLinkMicListener( mQAudienceMicHandler )
//主播和观众都关心麦位监听 
client!!.getService(QLinkMicService::class.java).addMicLinkerListener(mQLinkMicServiceListener)
//注册邀请监听
client.getService(QLinkMicService::class.java).invitationHandler.addInvitationHandlerListener( mInvitationListener )

//点击某个按钮 发起对某个主播申请 或者主播邀请用户
client!!.getService(QLinkMicService::class.java).invitationHandler.apply(10 * 1000, room.liveID, room.anchor.userId, null,callback )
```

```kotlin
如果不使用内置的邀请系统 比如外接匹配系统或者直接上麦不需要邀请
//todo
// 别的邀请或者匹配
//直接调用上麦方法
client?.getService(QLinkMicService::class.java)?.audienceMicHandler?.startLink( null, QCameraParam() , QMicrophoneParam(),callback )
```

### 无UISDK 实现PK

```kotlin

//邀请监听
private val mPKInvitationListener = object : QInvitationHandlerListener {
    //收到邀请
    override fun onReceivedApply(pkInvitation: QInvitation) {
        //拒绝操作
        client!!.getService(QPKService::class.java).invitationHandler.reject(pkInvitation.invitationID, null, callBack)
        //接受 
        client!!.getService(QPKService::class.java).invitationHandler.accept(pkInvitation.invitationID, null, callBack)
    }

    //收到对方取消    
    override fun onApplyCanceled(pkInvitation: QInvitation) {}

    //发起超时对方没响应
    override fun onApplyTimeOut(pkInvitation: QInvitation) {}

    //对方接受
    override fun onAccept(pkInvitation: QInvitation) {
        //对方接受后调用开始pk
        client?.getService(QPKService::class.java)?.start(20 * 1000, invitation.receiverRoomID, invitation.receiver.userId, null, callBack)
    }

    //对方拒绝
    override fun onReject(pkInvitation: QInvitation) {}
}

//pk监听
private val mQPKServiceListener = object : QPKServiceListener {

    override fun onStart(pkSession: QPKSession) {
        //主播设置对方主播预览
        client.getService(QPKService::class.java).setPeerAnchorPreView(findviewbyid(...))
        //主播和观众都显示pk覆盖UI
    }

    override fun onStop(pkSession: QPKSession, code: Int, msg: String) {
        //主播移除预览对方的UI 主播和观众都隐藏pk覆盖UI
    }

    override fun onStartTimeOut(pkSession: QPKSession) {}
    override fun onPKExtensionUpdate(pkSession: QPKSession, extension: QExtension) {
    }
}

//混流适配
private val mQPKMixStreamAdapter = object : QPKMixStreamAdapter {
    //pk对方进入了 返回混流参数（同连麦）
    override fun onPKLinkerJoin(pkSession: QPKSession): MutableList<QMergeOption> {
        return LinkerUIHelper.getPKMixOp(pkSession, user!!)
    }

    //pk开始了 如果修改整个直播混流面板（同连麦）
    override fun onPKMixStreamStart(pkSession: QPKSession): QMixStreamParams {
        return QMixStreamParams()
    }
}

//添加pk监听
client!!.getService(QPKService::
class.java).addServiceListener(mQPKServiceListener)
//主播注册混流适配   
client!!.getService(QPKService::
class.java).setPKMixStreamAdapter(mQPKMixStreamAdapter)
//注册邀请监听
client.getService(QPKService::
class.java).invitationHandler.addInvitationHandlerListener( mPKInvitationListener )

//点击某个按钮 发起对某个主播邀请
client!!.getService(QPKService::
class.java).invitationHandler.apply(10 * 1000, room.liveID, room.anchor.userId, null,callback )
```

```kotlin
如果不使用内置的邀请系统 比如外接匹配pk系统
//todo 别的邀请或者匹配
//直接调用开始PK方法
client?.getService(QPKService::class.java)?.start(20 * 1000, invitation.receiverRoomID, invitation.receiver.userId, null,callBack)
```

### 自定义IM,播放器，rtc sdk参数配置
#### 自定义im初始化
```
QNIMConfig.imSDKConfigGetter =  { appId: String, context: Context ->
   //返回你的自定义配置参数，比如加上推送配置，使用参考七牛im文档。
   BMXSDKConfig()
}
```
#### 自定义播放器参数
```
QMediaPlayerConfig.mAVOptionsGetter = {
  //返回你的自定义AVOptions，使用参考七牛播放器文档
  AVOptions（）
}
```
####  自定义rtc配置
```
QRtcLiveRoomConfig.mRTCSettingGetter = {
  //返回你的rtc配置，使用参考七牛rtc文档
   QNRTCSetting()
}
```


