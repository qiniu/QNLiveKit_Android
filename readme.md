[体验demo](http://fir.qnsdk.com/s6py)

[接入文档](https://developer.qiniu.com/lowcode/manual/12027/android-fast-access)

[api接口文档](https://developer.qiniu.com/lowcode/api/12032/the-android-api-documentation)
# 玩转七牛云qlivekit
## qlivekit 到底是什么样的？
### qlivekit简介
qlivekit是七牛云推出的一款互动直播低代码解决方案sdk，只需几行代码快速接入互动连麦pk直播

业务侧：

```                  
                      +--->  QChatRoomService //聊天室服务 
+----------------+    |
|                |    +--->  QLinkMicService  //连麦业务服务
+  QLiveClient   +---+
|                |    +--->  QPKService       //pk业务服务
+----------------+    |
  推拉流房间客户端       +--->  QPublicChatService //公屏消息服务       
   无UI版本sdk         |
                      +--->  QRoomService     //房间频道业务 
                      |    
                      +--->  QDanmakuService  //弹幕服务 
                      | 
                      +--->  QShoppingService //电商购物服务 
                      |
                      +--->  QGiftService     //礼物服务  
                      |
                      +--->  QLikeService     //点赞服务                                                                                                         
```    
- 基于七牛云基础设施服务：音视频、直播，IM，AI 智能算法和网络实现的基础直播业务-创建房间，开播，推流，拉流，聊天室
- 在此基础之上提供丰富的业务插件：房间管理，连麦，pk，带货，弹幕，礼物...
- 提供业务扩展,业务流程扩展，业务事件扩展，业务数据统计


UI侧：
```                          
                     +---> RoomListPage //房间列表UI实现页面
+---------------+    |  
|               |    |
+---+QLiveUIKIT +----+
|               |    |
+---------------+    | 
                     +---> RoomPage    //直播间页面UI实现                            
                      
```   

- 基础UI插件的形式提供组装好的完整的UI页面实现
- 提供丰富的UI插件：小窗播放，美颜，UI组件等
- 便捷的定制UI方式

### qlivekit 低代码到底低在哪里？
#### 快速启动

```kotlin
val config = QLiveConfig("serverURL")
QLive.init(appContext, config, tokenGetter)
QLive.auth(callback)
QLive.setUser(QUserInfo("your avatar", "your nick", extraInfo), callback)
QLive.getLiveUIKit().launch(context)
```
启动后选择直播间进入效果如下：
![alt](http://qrnlrydxa.hn-bkt.clouddn.com/doc/qk2.png)

#### 快速定制业务以及UI

定制业务以及UI的案例：

点击进入某个直播间此刻我们已经启动了一个基础的直播间，假设我们业务有定制化要求

- 要求1: 主播有家族阵营，在直播间左上方显示主播家族信息，点击进入家族页面
- 要求2: 用户有vip等级，vip3以上的用户才能发公屏聊天否则点击提示充值跳转接入方的充值页面

qilve的用户 quserinfo { 头像，昵称，其他资料，}

自定义：
- 增加接入的业务：quserinfo{ 头像，昵称，其他资料，家族，vip }
- 增加UI：家族UI 在线用户VIP等级显示
- 增加充值业务流程

实现步骤1 - 对接用户系统
可在客户端或者服务端增加用户业务字段
```kotlin
QLive.setUser(QUserInfo().apply {
    avatar = UserManager.user!!.data.avatar //设置当前用户头像
    nick = UserManager.user!!.data.nickname //设置当前用户昵称
    extension = HashMap<String, String>().apply {
        //设置扩展字段
        put("vip", UserInfoManager.getUserVip) //绑定接入方的VIP
        put("family", UserInfoManager.getUserFamily().jsonStr())//绑定接入方的家族信息
    }
}, null)
```

实现步骤2 - 定制UI
自定义家族组件 -- 在QLiveComponent的加入房间阶段取出房间的家族扩展字段显示

```kotlin
//家族组件 继承 QKitViewBindingFrameLayout 继承 QLiveComponent
class FamilyView : QKitViewBindingFrameLayout<ViewFamilyBinding> {

    override fun initView() {
        setOnClickListener {
            val familyInfo = JsonUtil.parse(roomInfo.anchor?.extensions?.get("family"), Family::class.java)  ?: return@setOnClickListener
            //点击跳转到家族页面
            FamilyActivity.start(kitContext.androidContext, familyInfo)
        }
    }

    //重写生命周期 - 加入房间阶段
    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo, isResumeUIFromFloating)
        //取出 房间的房主的家族扩展字段
        val familyInfo:Family = JsonUtil.parse(roomInfo.anchor?.extensions?.get("family"), Family::class.java) ?: return
        //显示家族名字
        binding.tvFamilyName.text = familyInfo.name
        //显示图标
        Glide.with(context).load(familyInfo.icon).into(binding.ivFamilyIcon)
    }
}
```

自定义发消息按钮 -- 点击事件判断 QLiveComponent的成员变量user（当前进入房间的用户)的vip等级

```kotlin
//自定义发消息按钮 继承原来的发消息按钮复用原来的逻辑
class CustomInputView : InputLinkView {
    
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        //设置点击事件
        setOnClickListener {
            //判断进入房间的用户是不是VIP等级足够
            if (((user.extensions.get("vip")?.toInt()) ?: 0) > 3) {
                //调用打开软键盘
                checkShowInputDialog()
            } else {
                //跳转充值页面
                //显示提示弹窗
                CommonTipDialog.TipBuild()
                    .setTittle("去充值发消息")
                    .setListener(object : FinalDialogFragment.BaseDialogListener() {
                        override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                            super.onDialogPositiveClick(dialog, any)
                            //跳转充值页面
                            RechargeActivity.start(kitContext.androidContext)
                        }
                    })
                    .build("ShoppingManagerDialog_manager")
                    //UI 上下文的弹窗上下文
                    .show(kitContext!!.fragmentManager, "")
            }
        }
        
    }
}
```

在拷贝的UI配置文件里替换和增加新的组件

```
<com.qlive.uikit.component.FrameLayoutSlidingCover>
    <!-- 替换新的输入框 -->
    <com.qlive.uikitpublicchat.CustomInputView
        android:layout_width="0dp"
        android:layout_height="32dp"
        android:layout_marginStart="8dp"
        android:layout_marginEnd="8dp"
        android:layout_weight="1"
        android:background="@drawable/kit_shape_40000000_16"
        />
    <!-- 其他原来的组件-->
    <View layout_width=""
        layout_height="" />
    
    <!-- 家族UI-->
    <!-- 放置左上角-->
    <!-- 设置上边距200dp-->
    <com.qlive.uikit.FamilyView
        android:layout_gravity="top|start"
        android:layout_height="wrap_content"
        android:layout_marginTop="200dp"
        android:layout_width="wrap_content" />
</com.qlive.uikit.component.FrameLayoutSlidingCover>
```
设置新的布局
```kotlin
val roomPage = QLive.getLiveUIKit().getPage(RoomPage::class.java)
//自定义房间页面观众房间的布局
roomPage.playerCustomLayoutID = R.layout.customXXXlayout
```
启动后效果如下：

![alt](http://qrnlrydxa.hn-bkt.clouddn.com/doc/qk.png)

## qlivekit架构解析

### 业务层
![alt ](http://qrnlrydxa.hn-bkt.clouddn.com/live-kit/qlive.drawio.png)

- 业务底座实现基础直播功能包括：进房，退房，推流，拉流，开播，关播等
- 业务插件层为接入用户可选的插件同时也能扩展插件
- 内置的业务插件实现提供足够灵活自定义空间

#### qlivekit是如何做业务扩展的？
以连麦业务为案例，qlive实现的基础业务实现为：
- 一个房间里有n个人在连麦以及他们的音视频状态(谁开关了麦克风摄像头)及连麦者的头像姓名资料
- 上麦下麦开关麦触发事件通知到房间所有人

> 内置连麦业务模型：连麦者 ：{音视频状态，用户资料，扩展字段hashMap}

``` java
//连麦用户
QMicLinker{
	//麦上用户资料
	public QLiveUser user
	//扩展字段
	public HashMap extension
	//是否开麦克风
	public boolean isOpenMicrophone
	//是否开摄像头
	public boolean isOpenCamera
}

//连麦服务
QLinkMicService{
	//获取当前房间所有连麦用户
	List<QMicLinker> getAllLinker()
	
	//添加麦位监听
	//@param-listener:麦位监听	
    void addMicLinkerListener(QLinkMicServiceListener listener)
	
	//开始上麦
	//@param-extension:麦位扩展字段	@param-cameraParams:摄像头参数 空代表不开	@param-microphoneParams:麦克参数  空代表不开	@param-callBack:上麦成功失败回调	
	void startLink(HashMap extension,QCameraParam cameraParams,QMicrophoneParam microphoneParams,QLiveCallBack callBack)

	//我是不是麦上用户
	boolean isLinked()

	//结束连麦
	//@param-callBack:操作回调	
	void stopLink(QLiveCallBack callBack)
	
	//跟新扩展字段
	//@param-micLinker:麦位置	@param-QExtension:扩展字段
	void updateExtension(QMicLinker micLinker,QExtension QExtension)

	//其他方法
}

//麦位监听
QLinkMicServiceListener{
	//有人上麦
	//@param-micLinker:连麦者	
	public void onLinkerJoin(QMicLinker micLinker)

	//有人下麦
	//@param-micLinker:连麦者	
	public void onLinkerLeft(QMicLinker micLinker)

	//有人麦克风变化
	//@param-micLinker:连麦者	
	public void onLinkerMicrophoneStatusChange(QMicLinker micLinker)

	//有人摄像头状态变化
	//@param-micLinker:连麦者	
	public void onLinkerCameraStatusChange(QMicLinker micLinker)

	//有人被踢
	//@param-micLinker:连麦者	@param-msg:自定义扩展消息	
	public void onLinkerKicked(QMicLinker micLinker,String msg)

	//有人扩展字段变化
	//@param-micLinker:连麦者	@param-QExtension:扩展信息	
	public void onLinkerExtensionUpdate(QMicLinker micLinker,QExtension QExtension)
}
``` 

假设接入场景是相亲场景，对连麦业务的要求是上麦者身份是红娘/女嘉宾/男嘉宾，红娘可以踢人强制关男嘉宾的麦并且显示特殊的UI状态

> 此时连麦业务模型：连麦者 ：{音视频状态，资料， 角色，被红娘禁用的状态} ，同时可能还需要自定义状态变化通知-红娘强制关麦状态

qlive在业务模型测提供了字段扩展扩展机制和事件扩展机制
此刻可实现为：
```kotlin
val linkerExtParams = HashMap<String, String>().apply {
    //上麦参数-添加自定义角色-红娘
    put("role", "matchmaker")
}
//以红娘身份上麦
service.startLink(linkerExtParams, QCameraParam(), QMicrophoneParam())
//对某个说脏话的连麦者添加一个扩展字段标记为强制关麦
service.updateExtension(linker,QExtension().apply {
    key = "Prohibit2Speak"
    value = "1"
})
//连麦服务监听
service.addMicLinkerListener(object : QLinkMicServiceListener {
    //有人上麦
    override fun onLinkerJoin(micLinker: QMicLinker) {
        //取出连麦者的身份
        val role = micLinker.extension["role"]
        //todo 显示麦位UI
    }

    //扩展字段跟新监听
    override fun onLinkerExtensionUpdate(micLinker: QMicLinker, extension: QExtension) {
        if (extension.key == "Prohibit2Speak") {
            //取出强制关麦状态
            val value = extension.value
        }
    }
})    
``` 

### ui层

![alt](http://qrnlrydxa.hn-bkt.clouddn.com/doc/qui.png)

qliveuikit的能力：
- 平台业务能快速插拔各个组件为其提供运行时管理
- 能对内置的组件配置化删除，修改，替换
- 能快速无侵入试增加自己的组件

UI组件的分类：
- 事件处理型：关心业务事件做出处理在页面表现可无UI，如收到房主离线事件关闭房间
- UI展示型：直接表现为在页面显示UI
- 功能型：实现特定行为功能如开始录制视频

#### qliveuikit是如何做到便捷的UI插件模式的？

对标准的UI开发通常分为命令式和声明式：

``` 
//命令式案例：
<LinearLayout 
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    <TextView
        android:id="@+id/tvNotice"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
    <TextView
        android:id="@+id/tvLikeCount"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
</LinearLayout>

class TestLiveActivity  {
    private lateinit var client: QPlayerClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        client.joinRoom("",null)
        val room=  client.roomInfo
        tvNotice.text = room.notice
        ivRoomCover.setImage(room.coverURL)
        client.addLikeServiceListener {
            tvLikeCount.text = it.count.toString()
        }
    }
}

``` 
``` 
//声明式案例：
class LiveRoomViewModel : ViewModel() {
    lateinit var client: QPlayerClient
    val noticeLiveData = MutableLiveData<String>()
    val likeLiveData = MutableLiveData<String>()

    fun join(){
        client.joinRoom("",null)
        val room=  client.roomInfo
        noticeLiveData.value = room.notice
        client.getService(QLikeService::class.java).addLikeServiceListener {
            likeLiveData.value = it.count.toString()
        }
    }
}

fun LivePage(model: LiveRoomViewModel) {
    val notice by model.noticeLiveData.observeAsState("")
    val like by model.likeLiveData.observeAsState("")
    
    //列表视图
    Column {
        //绑定UI和模型
        Text(text = notice)
        Text(text = like)
    }
}
``` 
在标准的UI开发模式中，业务UI组件越多逻辑越复杂UI样式部分和UI逻辑部分越复杂，接入用户的定制难度越大。 如果作为demo级UI产品标准的UI开发模式是完全没问题的，但是作为UIkit sdk级别产品通常都有以下诉求：
- 在sdk运行前想对UI布局编排进行调整
- 在sdk运行前要对已存在UI删除，修改，替换
- 在sdk运行前要在某个位置插入某个UI

以插入自定义UI为例：接入用户想插入一个官方公告组件，UI逻辑为进入房间，显示房间公告切换房间切换公告，点击公告以弹窗形式显示详情。sdk运行前并不知道自定义组件要执行啥样的逻辑需要啥样的业务数据

qlivekit插件化UI方案：

![alt ](http://qrnlrydxa.hn-bkt.clouddn.com/live-kit/qliveuiplugin.drawio.png)

QLiveComponent的运行环境中拥有：
- 1获取业务实现的qliveclient
- 2最基础业务数据用户和房间
- 3平台特性上下文（安卓为例：activity,fragmentmanager,context
- 房间声明周期回调
- 平台页面声明周期回调

这样的原本需要耦合在平台页面(activity/uicontrol)的UI逻辑分离到各个组件中实现，分离之后接下来考虑如何编排它们的布局？ui组件之间如果存在UI逻辑之间的联系如果通信？

```  
//安卓端案例
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout>
    <!--  被邀请监听  功能组件-->
    <com.qlive.uikitlinkmic.FuncCPTBeInvitedLinkMicMonitor />
    <!--  房间状态变化监听  功能组件-->
    <com.qlive.uikit.component.FuncCPTRoomStatusMonitor />
    <!--  虚拟按键拦截 功能组件-->
    <com.qlive.uikit.component.FuncCPTDefaultKeyDownMonitor />
    <FrameLayout>
        <com.qlive.uikitcore.QKitImageView
            android:layout_height="match_parent"
            android:layout_width="match_parent"
            android:src="@drawable/kit_dafault_room_bg" />
        <!--播放器-->
        <com.qlive.qplayer.QPlayerTextureRenderView
            android:id="@+id/playerRenderView"
            android:layout_height="match_parent"
            android:layout_width="match_parent" />
        <!--    pk预览-->
        <com.qlive.uikitpk.PKPlayerPreview
            android:layout_height="match_parent"
            android:layout_width="match_parent" />
        <!-- 连麦者预览-->
        <com.qlive.uikitlinkmic.MicLinkersView
            android:layout_gravity="end"
            android:layout_height="match_parent" />
        <!-- 主播离线提示text-->
        <com.qlive.uikit.component.AnchorOfflineTipView
            android:background="@drawable/shape_33000000_6"
            android:gravity="center" />
        <!--  关闭房间按钮-->
        <com.qlive.uikit.component.CloseRoomView
           <!--  位置-->
            android:layout_gravity="top|end"
           <!--  图标-->
            android:src="@mipmap/custompic"
            android:layout_height="32dp" />
        
    </FrameLayout>
</FrameLayout>
``` 
在 qliveuikit 中。开发者只需拿到一份原本的页面配置文件，在此之上可以
- 调节原有的组件样式
- 修改删除原有的组件
- 增加组件

只需要简单修改UI配置文件，即可实现定制UIsdk里的页面样式和UI逻辑。
