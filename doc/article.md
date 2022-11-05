# 玩转七牛云qlivekit
## qlivekit 到底是什么样的？
### qlivekit简介
qlivekit是七牛云推出的一款互动直播低代码解决方案sdk，只需几行代码快速接入互动连麦pk直播
在直播场景下，qlive具有以下特点：
- 丰富的业务插件：房间管理，聊天室，连麦，pk ，购物车，小窗播放，美颜...
- 扩展性：业务扩展及对接接入方的业务系统，自定义ui
- 可靠的基础设施服务
- 完善的插件机制
- 简单，易用


## qlivekit 低代码到底低在哪里？

### 快速启动

```kotlin
import com.qlive.sdk.QLive

val config = QLiveConfig("serverURL")
QLive.init(appContext, config, tokenGetter)
QLive.auth(callback)
QLive.setUser(QUserInfo("your avatar", "your nick", extraInfo), callback)
QLive.getLiveUIKit().launch(context)
```
启动后选择直播间进入效果如下：

![alt ](http://qrnlrydxa.hn-bkt.clouddn.com/live-kit/origin.png)
### 快速定制业务以及UI

点击进入某个直播间此刻我们已经启动了一个基础的直播间，假设我们业务有定制化要求

- 要求1: 主播有家族阵营，在直播间右上方显示主播家族信息，点击进入家族页面
- 要求2: 用户有vip等级，vip3以上的用户才能发公屏聊天否则点击提示充值跳转接入方的充值页面


qilve的用户 quserinfo{ 头像，昵称，其他资料，}


自定义：
- 增加接入的业务：
  quserinfo{ 头像，昵称，其他资料，家族，vip }

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

在UI配置文件里替换和增加新的组件

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
启动后效果如下：

![alt ](http://qrnlrydxa.hn-bkt.clouddn.com/live-kit/new.png)



## qlivekit架构解析

### 业务层
![alt ](http://qrnlrydxa.hn-bkt.clouddn.com/live-kit/qlive.drawio.png)

- 业务底座实现基础直播功能包括：进房，退房，推流，拉流，开播，关播等
- 业务插件层为接入用户可选需要的插件同时也能扩展插件
- 内置的业务插件实现提供足够灵活自定义空间

#### 业务扩展的案例：
对于一个连麦麦位qlive实现的基础业务实现为：一个房间里有n个人在连麦以及他们的音视频状态(谁开关了麦克风摄像头)及连麦者的头像姓名资料

> 内置连麦业务模型：连麦者 ：{音视频状态，资料}

假设接入场景是相亲场景，对连麦业务的要求是上麦者身份是红娘/女嘉宾/男嘉宾，红娘可以踢人强制关男嘉宾的麦

> 此时连麦业务模型：连麦者 ：{音视频状态，资料， 角色，被禁用状态} ，同时可能还需要自定义状态变化通知

qlive在业务模型测提供了字段扩展扩展机制和事件扩展机制
> qlive业务模型  mode : {内置自定义1，扩展字段hashMap<String,String> }

### ui层

![alt ](http://qrnlrydxa.hn-bkt.clouddn.com/live-kit/qliveuidrawio.png)

- 平台业务能快速插拔各个组件为其提供运行时管理
- 能对内置的组件配置化删除，修改，替换
- 能快速无侵入试增加自己的组件

对UI开发通常分为命令式和声明式：

``` 
//命令试案例：

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
//命令试案例：
class LiveRoomViewModel : ViewModel() {
    private lateinit var client: QPlayerClient

    val noticeLiveData = MutableLiveData<String>()
    val likeLiveData = MutableLiveData<String>()

    fun join(){
        client.joinRoom("",null)
        val room=  client.roomInfo
        noticeLiveData.value =  room.notice
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
在标准的UI开发模式中，业务UI组件越多逻辑越复杂，接入用户的定制难度越大
，同时在已经实现直播UI的sdk中，动态定制UI的难度更大

qlivekit插件化UI方案：


![alt ](http://qrnlrydxa.hn-bkt.clouddn.com/live-kit/qlive.drawio.png)


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
复用平台布局设置直接组装内置插件即可