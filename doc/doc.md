```java
//低代码直播客户端
QLive{

	//初始化
	//@param-context:安卓上下文	@param-config:sdk配置	@param-tokenGetter:token获取	
	public static void init(Context context,QLiveConfig config,QTokenGetter tokenGetter)

	//登陆认证成功后才能使用qlive的功能
	//@param-callBack:操作回调	
	public static void auth(QLiveCallBack callBack)

	//跟新用户信息
	//@param-userInfo:用户参数	@param-callBack:回调函数	
	public static void setUser(QUserInfo userInfo,QLiveCallBack callBack)

	//获取当前登陆用户资料
	public static com.qlive.core.been.QLiveUser getLoginUser()

	//创建推流客户端
	public static com.qlive.pushclient.QPusherClient createPusherClient()

	//创建拉流客户端
	public static com.qlive.playerclient.QPlayerClient createPlayerClient()

	//获取房间管理接口
	public static com.qlive.sdk.QRooms getRooms()

	//获得UIkit
	public static com.qlive.sdk.QLiveUIKit getLiveUIKit()
}

//UIkit客户端
QLiveUIKit{

	//获取内置页面每个页面有相应的UI配置
	//@param-pageClass:页面的类 目前子类为 RoomListPage-> 房间列表页面 RoomPage->直播间页面	
	public com.qlive.sdk.QPage getPage(Class pageClass)

	//跳转到直播列表页面
	//@param-context:安卓上下文	
	public void launch(Context context)
}

//跟新用户资料参数
QUserInfo{
	//头像
	public String avatar
	//名称
	public String nick
	//扩展字段
	public HashMap extension
}

//token获取回调  当token过期后自动调用getTokenInfo
QTokenGetter{

	//如何获取token
	//@param-callback:业务（同步/异步）获取后把结果通知给sdk	
	public void getTokenInfo(QLiveCallBack callback)
}

//sdk 配置
QLiveConfig{
	//打印日志开关
	public boolean isLogAble
	//服务器地址 默认为低代码demo地址  如果自己部署可改为自己的服务地址
	public String serverURL
}

//房间管理接口
QRooms{

	//创建房间
	//@param-param:创建房间参数	@param-callBack:	
	public void createRoom(QCreateRoomParam param,QLiveCallBack callBack)

	//删除房间
	//@param-roomID:房间ID	@param-callBack:	
	public void deleteRoom(String roomID,QLiveCallBack callBack)

	//根据ID获取房间信息
	//@param-roomID:房间ID	@param-callBack:	
	public void getRoomInfo(String roomID,QLiveCallBack callBack)

	//房间列表
	//@param-pageNumber:	@param-pageSize:	@param-callBack:	
	public void listRoom(int pageNumber,int pageSize,QLiveCallBack callBack)

	//我的直播记录
	//@param-pageNumber:	@param-pageSize:	@param-callBack:	
	public void liveRecord(int pageNumber,int pageSize,QLiveCallBack callBack)

	//获取直播间数据统计
	//@param-roomID:	@param-callBack:	
	public void getLiveStatistics(String roomID,QLiveCallBack callBack)

	//获取礼物配置
	//@param-type:礼物类型 -1代表全部	@param-callback:	
	public void getGiftConfig(int type,QLiveCallBack callback)

	//获取直播间礼物统计
	//@param-roomID:	@param-pageNumber:	@param-pageSize:	@param-callback:	
	public void getLiveGiftStatistics(String roomID,int pageNumber,int pageSize,QLiveCallBack callback)

	//获取主播礼物统计
	//@param-pageNumber:	@param-pageSize:	@param-callback:	
	public void getAnchorGiftStatistics(int pageNumber,int pageSize,QLiveCallBack callback)

	//获取用户礼物统计
	//@param-pageNumber:	@param-pageSize:	@param-callback:	
	public void getUserGiftStatistics(int pageNumber,int pageSize,QLiveCallBack callback)
}

//直播状态枚举
QLiveStatus{
	//房间已创建
	public static final QLiveStatus PREPARE
	//房间已发布
	public static final QLiveStatus ON
	//强制关闭
	public static final QLiveStatus FORCE_CLOSE
	//主播上线
	public static final QLiveStatus ANCHOR_ONLINE
	//主播已离线
	public static final QLiveStatus ANCHOR_OFFLINE
	//房间已关闭
	public static final QLiveStatus OFF

	//
	public static com.qlive.core.QLiveStatus[] values()

	//
	public static com.qlive.core.QLiveStatus valueOf()
}

//基础回调函数
QLiveCallBack{

	//操作失败
	//@param-code:错误码	@param-msg:消息	
	public void onError(int code,String msg)

	//操作成功
	//@param-data:数据	
	public void onSuccess(Object data)
}

//客户端类型枚举
QClientType{
	//Pusher 推流端
	public static final QClientType PUSHER
	//Player 拉流观众端
	public static final QClientType PLAYER

	//
	public static com.qlive.core.QClientType[] values()

	//
	public static com.qlive.core.QClientType valueOf()
}

//ui组件实现的房间生命周期
QClientLifeCycleListener{

	//进入回调
	//@param-user:进入房间的用户	@param-liveId:房间ID	
	public void onEntering(String user,QLiveUser liveId)

	//加入回调房间验证成功加入了房间我在房间里
	//@param-roomInfo:房间信息	
	public void onJoined(QLiveRoomInfo roomInfo)

	//用户离开回调
	public void onLeft()

	//销毁
	public void onDestroyed()
}

//邀请处理器
QInvitationHandler{

	//发起邀请/申请
	//@param-expiration:过期时间 单位毫秒 过期后不再响应	@param-receiverRoomID:接收方所在房间ID	@param-receiverUID:接收方用户ID	@param-extension:扩展字段	@param-callBack:回调函数	
	public void apply(long expiration,String receiverRoomID,String receiverUID,HashMap extension,QLiveCallBack callBack)

	//取消邀请/申请
	//@param-invitationID:邀请ID	@param-callBack:	
	public void cancelApply(int invitationID,QLiveCallBack callBack)

	//接受对方的邀请/申请
	//@param-invitationID:邀请ID	@param-extension:扩展字段	@param-callBack:	
	public void accept(int invitationID,HashMap extension,QLiveCallBack callBack)

	//拒绝对方
	//@param-invitationID:邀请ID	@param-extension:扩展字段	@param-callBack:	
	public void reject(int invitationID,HashMap extension,QLiveCallBack callBack)

	//移除监听
	//@param-listener:	
	public void removeInvitationHandlerListener(QInvitationHandlerListener listener)

	//添加监听
	//@param-listener:	
	public void addInvitationHandlerListener(QInvitationHandlerListener listener)
}

//邀请监听
QInvitationHandlerListener{

	//收到申请/邀请
	//@param-invitation:	
	public void onReceivedApply(QInvitation invitation)

	//对方取消申请
	//@param-invitation:	
	public void onApplyCanceled(QInvitation invitation)

	//申请/邀请超时
	//@param-invitation:	
	public void onApplyTimeOut(QInvitation invitation)

	//被接受
	//@param-invitation:	
	public void onAccept(QInvitation invitation)

	//被拒绝
	//@param-invitation:	
	public void onReject(QInvitation invitation)
}

//创建房间参数
QCreateRoomParam{
	//房间标题
	public String title
	//房间公告
	public String notice
	//封面
	public String coverURL
	//扩展字段
	public HashMap extension
	//非必须  预计开播时间
	public long startAt
	//非必须  预计结束时间
	public long endAt
	//非必须  推流token 过期时间
	public long publishExpireAt
}

//弹幕实体
QDanmaku{
	//
	public static String action_danmu
	//发送方用户
	public QLiveUser sendUser
	//弹幕内容
	public String content
	//发送方所在房间ID
	public String senderRoomID
	//扩展字段
	public HashMap extension
}

//扩展字段
QExtension{
	//
	public String key
	//
	public String value
}

//邀请信息
QInvitation{
	//发起方
	public QLiveUser initiator
	//接收方
	public QLiveUser receiver
	//发起方所在房间ID
	public String initiatorRoomID
	//接收方所在房间ID
	public String receiverRoomID
	//扩展字段
	public HashMap extension
	//邀请ID
	public int invitationID
}

//房间信息
QLiveRoomInfo{
	//房间ID
	public String liveID
	//房间标题
	public String title
	//房间公告
	public String notice
	//封面
	public String coverURL
	//扩展字段
	public Map extension
	//主播信息
	public QLiveUser anchor
	//
	public String roomToken
	//当前房间的pk会话信息
	public String pkID
	//在线人数
	public long onlineCount
	//开始时间
	public long startTime
	//结束时间
	public long endTime
	//聊天室ID
	public String chatID
	//推流地址
	public String pushURL
	//拉流地址
	public String hlsURL
	//拉流地址
	public String rtmpURL
	//拉流地址
	public String flvURL
	//pv
	public Double pv
	//uv
	public Double uv
	//总人数
	public int totalCount
	//连麦者数量
	public int totalMics
	//直播间状态
	public int liveStatus
	//主播在线状态
	public int anchorStatus

	//
	public com.qlive.core.been.QLiveRoomInfo clone()
}

//用户
QLiveUser{
	//用户ID
	public String userId
	//用户头像
	public String avatar
	//名字
	public String nick
	//扩展字段
	public Map extensions
	//用户im id
	public String imUid
	//用户Im名称
	public String im_username
}

//连麦用户
QMicLinker{
	//麦上用户资料
	public QLiveUser user
	//
	public String userRoomID
	//扩展字段
	public HashMap extension
	//是否开麦克风
	public boolean isOpenMicrophone
	//是否开摄像头
	public boolean isOpenCamera
}

//pk 会话
QPKSession{
	//PK场次ID
	public String sessionID
	//发起方
	public QLiveUser initiator
	//接受方
	public QLiveUser receiver
	//发起方所在房间
	public String initiatorRoomID
	//接受方所在房间
	public String receiverRoomID
	//扩展字段
	public Map extension
	//pk状态  RelaySessionStatusWaitAgree(0) - 等待接收方同意  RelaySessionStatusAgreed(1) - 接收方已同意  RelaySessionStatusInitSuccess(2)  -  发起方已经完成跨房，等待对方完成  RelaySessionStatusRecvSuccess(3)  -  接收方已经完成跨房，等待对方完成  RelaySessionStatusSuccess(4)  -  两方都完成跨房  RelaySessionStatusRejected(5)  -  接收方拒绝  RelaySessionStatusStopped(6)  -  结束
	public int status
	//pk开始时间戳
	public long startTimeStamp
}

//公屏类型消息
QPublicChat{
	//类型 -- 加入房间欢迎
	public static String action_welcome
	//类型 -- 离开房间
	public static String action_bye
	//类型 -- 点赞
	public static String action_like
	//类型 -- 公屏输入
	public static String action_puchat
	//
	public static String action_pubchat_custom
	//消息类型
	public String action
	//发送方
	public QLiveUser sendUser
	//消息体
	public String content
	//发送方所在房间ID
	public String senderRoomId
	//消息ID
	public String msgID
}

//拉流事件回调
QPlayerEventListener{

	//拉流器准备中
	//@param-preparedTime:准备耗时	
	public void onPrepared(int preparedTime)

	//拉流器信息回调
	//@param-what:事件 参考七牛霹雳播放器	@param-extra:数据	
	public void onInfo(int what,int extra)

	//拉流缓冲跟新
	//@param-percent:缓冲比分比	
	public void onBufferingUpdate(int percent)

	///视频尺寸变化回调
	//@param-width:变化后的宽	@param-height:变化后高	
	public void onVideoSizeChanged(int width,int height)

	//播放出错回调
	//@param-errorCode:错误码 参考七牛霹雳播放器	
	public boolean onError(int errorCode)
}

//默认美颜参数（免费）
QBeautySetting{

	//
	public boolean isEnabled()

	//设置是否可用
	public void setEnable()

	//
	public float getSmoothLevel()

	//磨皮等级
	//@param-smoothLevel:0.0 -1.0	
	public void setSmoothLevel(float smoothLevel)

	//
	public float getWhiten()

	//设置美白等级
	//@param-whiten:0.0 -1.0	
	public void setWhiten(float whiten)

	//
	public float getRedden()

	//设置红润等级0.0-1.0
	//@param-redden:	
	public void setRedden(float redden)
}

//rtc推流链接状态监听
QConnectionStatusLister{

	//rtc推流链接状态
	//@param-state:状态枚举	
	public void onConnectionStatusChanged(QRoomConnectionState state)
}

//观众播放器预览  子类 QPlayerTextureRenderView 和 QSurfaceRenderView
QPlayerRenderView{

	//设置预览模式
	//@param-previewMode:预览模式枚举	
	public void setDisplayAspectRatio(PreviewMode previewMode)

	//
	public void setRenderCallback()

	//
	public View getView()

	//
	public Surface getSurface()
}

//麦克风参数
QMicrophoneParam{
	//采样率 默认值48000
	public int sampleRate
	//采样位深 默认16
	public int bitsPerSample
	//通道数 默认 1
	public int channelCount
	//码率 默认64
	public int bitrate
}

//摄像头参数
QCameraParam{
	//默认码率
	public static int DEFAULT_BITRATE
	//分辨率宽 默认值 720
	public int width
	//分辨高  默认值 1280
	public int height
	//帧率 默认值25
	public int FPS
	//
	public int bitrate
	//
	public QVideoCaptureConfig captureConfig
}

//混流画布参数
QMixStreaming.MixStreamParams{
	//
	public static int DEFAULT_BITRATE
	//混流画布宽
	public int mixStreamWidth
	//混流画布高
	public int mixStringHeight
	//混流码率
	public int mixBitrate
	//混流帧率
	public int FPS
	//混流背景图片
	public TranscodingLiveStreamingImage backGroundImg
}

//背景图片
QMixStreaming.TranscodingLiveStreamingImage{
	//背景图网络url
	public String url
	//x坐标
	public int x
	//y坐标
	public int y
	//背景图宽
	public int width
	//背景图高
	public int height
}

//摄像头混流参数
QMixStreaming.CameraMergeOption{
	//是否参与混流
	public boolean isNeed
	//x坐标
	public int x
	//y坐标
	public int y
	//z坐标
	public int z
	//用户视频宽
	public int width
	//用户视频高
	public int height
}

//某个用户的混流参数  只需要指定用户ID 和他的摄像头麦克风混流参数
QMixStreaming.MergeOption{
	//用户混流参数的ID
	public String uid
	//视频混流参数
	public CameraMergeOption cameraMergeOption
	//音频混流参数
	public MicrophoneMergeOption microphoneMergeOption
}

//麦克风混流参数
QMixStreaming.MicrophoneMergeOption{
	//是否参与混流
	public boolean isNeed
}

//推流客户端（主播端）
QPusherClient{

	//获取插件服务实例
	//@param-serviceClass:插件的类	
	public QLiveService getService(Class serviceClass)

	//设置直播状态回调
	//@param-liveStatusListener:直播事件监听	
	public void addLiveStatusListener(QLiveStatusListener liveStatusListener)

	//
	public void removeLiveStatusListener()

	//当前客户端类型QClientType.PUSHER代表推流端QClientType.PLAYER代表拉流端
	public com.qlive.core.QClientType getClientType()

	//启动视频采集和预览
	//@param-cameraParam:摄像头参数	@param-renderView:预览窗口	
	public void enableCamera(QCameraParam cameraParam,QPushRenderView renderView)

	//启动麦克采集
	//@param-microphoneParam:麦克风参数	
	public void enableMicrophone(QMicrophoneParam microphoneParam)

	//加入房间
	//@param-roomID:房间ID	@param-callBack:回调函数	
	public void joinRoom(String roomID,QLiveCallBack callBack)

	//主播关闭房间
	//@param-callBack:	
	public void closeRoom(QLiveCallBack callBack)

	//主播离开房间房间不关闭
	//@param-callBack:	
	public void leaveRoom(QLiveCallBack callBack)

	//销毁推流客户端销毁后不能使用
	public void destroy()

	//主播设置推流链接状态监听
	//@param-connectionStatusLister:	
	public void setConnectionStatusLister(QConnectionStatusLister connectionStatusLister)

	//Switchcamera
	//@param-callBack:切换摄像头回调	
	public void switchCamera(QLiveCallBack callBack)

	//禁/不禁用本地视频流禁用后本地能看到预览观众不能看到主播的画面
	//@param-muted:是否禁用	@param-callBack:	
	public void muteCamera(boolean muted,QLiveCallBack callBack)

	//禁用麦克风推流
	//@param-muted:是否禁用	@param-callBack:	
	public void muteMicrophone(boolean muted,QLiveCallBack callBack)

	//设置视频帧回调
	//@param-frameListener:视频帧监听	
	public void setVideoFrameListener(QVideoFrameListener frameListener)

	//设置本地音频数据监听
	//@param-frameListener:音频帧回调	
	public void setAudioFrameListener(QAudioFrameListener frameListener)

	//暂停
	public void pause()

	//恢复
	public void resume()

	//设置默认免费版美颜参数
	//@param-beautySetting:美颜参数	
	public void setDefaultBeauty(QBeautySetting beautySetting)

	//
	public void enableEarMonitor()

	//
	public boolean isEarMonitorEnable()

	//
	public void setMicrophoneVolume()

	//
	public double getMicrophoneVolume()
}

//推流预览窗口  子类实现 QPushSurfaceView 和 QPushTextureView
QPushRenderView{

	//
	public View getView()
}

//拉流客户端
QPlayerClient{

	//获取插件服务实例
	//@param-serviceClass:插件的类	
	public QLiveService getService(Class serviceClass)

	//设置直播状态回调
	//@param-liveStatusListener:直播事件监听	
	public void addLiveStatusListener(QLiveStatusListener liveStatusListener)

	//
	public void removeLiveStatusListener()

	//当前客户端类型QClientType.PUSHER代表推流端QClientType.PLAYER代表拉流端
	public com.qlive.core.QClientType getClientType()

	//加入房间
	//@param-roomID:房间ID	@param-callBack:回调	
	public void joinRoom(String roomID,QLiveCallBack callBack)

	//离开房间离开后可继续加入其他房间如上下滑动切换房间
	//@param-callBack:回调	
	public void leaveRoom(QLiveCallBack callBack)

	//销毁释放资源离开房间后退出页面不再使用需要释放
	public void destroy()

	//设置预览窗口内置QPlayerTextureRenderView(推荐)/QSurfaceRenderView
	//@param-renderView:预览窗口	
	public void play(QPlayerRenderView renderView)

	//设置播放器音量若参数为0f，则会将视频静音；若参数大于1f，播放音量会大于视频原来的音量
	//@param-leftVolume:左声道音量	@param-rightVolume:右声道音量	
	public void setVolume(float leftVolume,float rightVolume)

	//暂停
	public void pause()

	//恢复
	public void resume()

	//添加播放器事件监听
	//@param-playerEventListener:播放器事件监听	
	public void addPlayerEventListener(QPlayerEventListener playerEventListener)

	//移除播放器事件监听
	//@param-playerEventListener:播放器事件监听	
	public void removePlayerEventListener(QPlayerEventListener playerEventListener)
}

//直播状态监听
QLiveStatusListener{

	//直播间状态变化业务状态
	//@param-liveStatus:业务状态	
	public void onLiveStatusChanged(QLiveStatus liveStatus)
}

//连麦服务
QLinkMicService{

	//获取当前房间所有连麦用户
	public java.util.List getAllLinker()

	//设置某人的连麦视频预览麦上用户调用上麦后才会使用切换成rtc连麦下麦后使用拉流预览
	//@param-uID:用户ID	@param-preview:预览窗口	
	public void setUserPreview(String uID,QPushRenderView preview)

	//踢人
	//@param-uID:用户ID	@param-msg:附加消息	@param-callBack:操作回调	
	public void kickOutUser(String uID,String msg,QLiveCallBack callBack)

	//跟新扩展字段
	//@param-micLinker:麦位置	@param-QExtension:扩展字段	
	public void updateExtension(QMicLinker micLinker,QExtension QExtension)

	//添加麦位监听
	//@param-listener:麦位监听	
	public void addMicLinkerListener(QLinkMicServiceListener listener)

	//移除麦位监听
	//@param-listener:麦位监听	
	public void removeMicLinkerListener(QLinkMicServiceListener listener)

	//获得连麦邀请处理
	public com.qlive.core.QInvitationHandler getInvitationHandler()

	//观众向主播连麦处理器
	public com.qlive.linkmicservice.QAudienceMicHandler getAudienceMicHandler()

	//主播处理自己被连麦处理器
	public com.qlive.linkmicservice.QAnchorHostMicHandler getAnchorHostMicHandler()
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

//主播端连麦器
QAnchorHostMicHandler{

	//设置混流适配器
	//@param-QLinkMicMixStreamAdapter:混流适配器	
	public void setMixStreamAdapter(QLinkMicMixStreamAdapter QLinkMicMixStreamAdapter)
}

//混流适配器
QLinkMicMixStreamAdapter{

	//连麦开始如果要自定义混流画布和背景返回空则主播推流分辨率有多大就多大默认实现
	public com.qlive.avparam.QMixStreaming.MixStreamParams onMixStreamStart()

	//混流布局适配
	//@param-micLinkers:变化后所有连麦者	@param-target:当前变化的连麦者	@param-isJoin:当前变化的连麦者是新加还是离开	
	public java.util.List onResetMixParam(List micLinkers,QMicLinker target,boolean isJoin)
}

//观众连麦器
QAudienceMicHandler{

	//添加连麦监听
	//@param-listener:监听	
	public void addLinkMicListener(LinkMicHandlerListener listener)

	//移除连麦监听
	//@param-listener:监听	
	public void removeLinkMicListener(LinkMicHandlerListener listener)

	//开始上麦
	//@param-extension:麦位扩展字段	@param-cameraParams:摄像头参数 空代表不开	@param-microphoneParams:麦克参数  空代表不开	@param-callBack:上麦成功失败回调	
	public void startLink(HashMap extension,QCameraParam cameraParams,QMicrophoneParam microphoneParams,QLiveCallBack callBack)

	//我是不是麦上用户
	public boolean isLinked()

	//结束连麦
	//@param-callBack:操作回调	
	public void stopLink(QLiveCallBack callBack)

	//上麦后可以切换摄像头
	//@param-callBack:	
	public void switchCamera(QLiveCallBack callBack)

	//上麦后可以禁言本地视频流
	//@param-muted:	@param-callBack:	
	public void muteCamera(boolean muted,QLiveCallBack callBack)

	//上麦后可以禁用本地音频流
	//@param-muted:	@param-callBack:	
	public void muteMicrophone(boolean muted,QLiveCallBack callBack)

	//上麦后可以设置本地视频帧回调
	//@param-frameListener:	
	public void setVideoFrameListener(QVideoFrameListener frameListener)

	//上麦后可以设置音频帧回调
	//@param-frameListener:	
	public void setAudioFrameListener(QAudioFrameListener frameListener)

	//上麦后可以设置免费的默认美颜参数
	//@param-beautySetting:	
	public void setDefaultBeauty(QBeautySetting beautySetting)

	//
	public void enableEarMonitor()

	//
	public boolean isEarMonitorEnable()

	//
	public void setMicrophoneVolume()

	//
	public double getMicrophoneVolume()
}

//观众连麦处理器监听  观众需要处理的事件
QAudienceMicHandler.LinkMicHandlerListener{

	//连麦模式连接状态连接成功后连麦器会主动禁用推流器改用rtc
	//@param-state:状态	
	public void onConnectionStateChanged(QRoomConnectionState state)

	//本地角色变化
	//@param-isLinker:当前角色是不是麦上用户 上麦后true 下麦后false	
	public void onRoleChange(boolean isLinker)
}

//音频帧监听
QAudioFrameListener{

	//音频帧回调
	//@param-srcBuffer:输入pcm数据	@param-size:大小	@param-bitsPerSample:位深	@param-sampleRate:采样率	@param-numberOfChannels:通道数	
	public void onAudioFrameAvailable(ByteBuffer srcBuffer,int size,int bitsPerSample,int sampleRate,int numberOfChannels)
}

//视频帧监听
QVideoFrameListener{

	//yuv帧回调
	//@param-data:yuv数据	@param-type:帧类型	@param-width:宽	@param-height:高	@param-rotation:旋转角度	@param-timestampNs:时间戳	
	public void onYUVFrameAvailable(byte data,QVideoFrameType type,int width,int height,int rotation,long timestampNs)

	//纹理ID回调
	//@param-textureID:输入的纹理ID	@param-type:纹理类型	@param-width:宽	@param-height:高	@param-rotation:旋转角度	@param-timestampNs:时间戳	@param-transformMatrix:转化矩阵	
	public int onTextureFrameAvailable(int textureID,QVideoFrameType type,int width,int height,int rotation,long timestampNs,float transformMatrix)
}

//pk服务
QPKService{

	//主播设置混流适配器
	//@param-adapter:混流适配	
	public void setPKMixStreamAdapter(QPKMixStreamAdapter adapter)

	//添加pk监听
	//@param-serviceListener:	
	public void addServiceListener(QPKServiceListener serviceListener)

	//移除pk监听
	//@param-serviceListener:	
	public void removeServiceListener(QPKServiceListener serviceListener)

	//开始pk
	//@param-timeoutTimestamp:等待对方流超时时间时间戳 毫秒	@param-receiverRoomID:接受方所在房间ID	@param-receiverUID:接收方用户ID	@param-extension:扩展字段	@param-callBack:操作回调函数	
	public void start(long timeoutTimestamp,String receiverRoomID,String receiverUID,HashMap extension,QLiveCallBack callBack)

	//结束pk
	//@param-callBack:操作回调	
	public void stop(QLiveCallBack callBack)

	//主播设置对方的连麦预览
	//@param-view:预览窗口	
	public void setPeerAnchorPreView(QPushRenderView view)

	//获得pk邀请处理
	public com.qlive.core.QInvitationHandler getInvitationHandler()

	//当前正在pk信息没有PK则空
	public com.qlive.pkservice.QPKSession currentPKingSession()
}

//pk回调
QPKServiceListener{

	//pk开始回调观众刚进入房间如果房间正在pk也马上会回调
	//@param-pkSession:pk会话	
	public void onStart(QPKSession pkSession)

	//pk结束回调
	//@param-pkSession:pk会话	@param-code:-1 异常结束 0主动结束 1对方结束	@param-msg:	
	public void onStop(QPKSession pkSession,int code,String msg)

	//主播主动开始后收对方流超时pk没有建立起来
	//@param-pkSession:pk会话	
	public void onStartTimeOut(QPKSession pkSession)
}

//pk混流适配器
QPKMixStreamAdapter{

	//当pk开始如何混流
	//@param-pkSession:	
	public java.util.List onPKLinkerJoin(QPKSession pkSession)

	//pk开始时候混流画布变成多大返回null则原来主播有多大就有多大
	//@param-pkSession:	
	public com.qlive.avparam.QMixStreaming.MixStreamParams onPKMixStreamStart(QPKSession pkSession)

	//当pk结束后如果还有其他普通连麦者如何混流如果pk结束后没有其他连麦者则不会回调
	public java.util.List onPKLinkerLeft()

	//当pk结束后如果还有其他普通连麦者如何混流如果pk结束后没有其他连麦者则不会回调返回空则默认之前的不变化
	public com.qlive.avparam.QMixStreaming.MixStreamParams onPKMixStreamStop()
}

//公屏服务
QPublicChatService{

	//发送公聊
	//@param-msg:公屏消息内容	@param-callBack:操作回调	
	public void sendPublicChat(String msg,QLiveCallBack callBack)

	//
	public void getHistoryChatMsg()

	//发送进入消息
	//@param-msg:消息内容	@param-callBack:操作回调	
	public void sendWelCome(String msg,QLiveCallBack callBack)

	//发送拜拜
	//@param-msg:消息内容	@param-callBack:操作回调	
	public void sendByeBye(String msg,QLiveCallBack callBack)

	//点赞
	//@param-msg:消息内容
            * @param callBack 操作回调	
	public void sendLike(String msg)

	//自定义要显示在公屏上的消息
	//@param-action:消息code 用来区分要做什么响应	@param-msg:消息内容	@param-callBack:回调	
	public void sendCustomPubChat(String action,String msg,QLiveCallBack callBack)

	//往本地公屏插入消息不发送到远端
	public void pubMsgToLocal()

	//添加监听
	//@param-lister:	
	public void addServiceLister(QPublicChatServiceLister lister)

	//移除监听
	//@param-lister:	
	public void removeServiceLister(QPublicChatServiceLister lister)
}

//
QPublicChatServiceLister{

	//收到公聊消息pubChat.action可以区分是啥类型的公聊消息
	//@param-pubChat:消息实体	
	public void onReceivePublicChat(QPublicChat pubChat)
}

//房间服务
QRoomService{

	//添加监听
	//@param-listener:	
	public void addRoomServiceListener(QRoomServiceListener listener)

	//移除监听
	//@param-listener:	
	public void removeRoomServiceListener(QRoomServiceListener listener)

	//获取当前房间
	public com.qlive.core.been.QLiveRoomInfo getRoomInfo()

	//刷新房间信息
	public void getRoomInfo()

	//跟新直播扩展信息
	//@param-extension:扩展字段	@param-callBack:操作回调	
	public void updateExtension(QExtension extension,QLiveCallBack callBack)

	//当前房间在线用户
	//@param-pageNum:页号 1开始	@param-pageSize:每页大小	@param-callBack:操作回调	
	public void getOnlineUser(int pageNum,int pageSize,QLiveCallBack callBack)

	//某个房间在线用户
	//@param-pageNum:页号 1开始	@param-pageSize:每页大小	@param-callBack:操作回调	@param-roomId:房间ID	
	public void getOnlineUser(int pageNum,int pageSize,String callBack,QLiveCallBack roomId)

	//使用用户ID搜索房间用户
	//@param-uid:用户ID	@param-callBack:操作回调	
	public void searchUserByUserId(String uid,QLiveCallBack callBack)

	//使用用户imuid搜索用户
	//@param-imUid:用户im 用户ID	@param-callBack:操作回调	
	public void searchUserByIMUid(String imUid,QLiveCallBack callBack)
}

//房间服务监听
QRoomServiceListener{

	//直播间某个属性变化
	//@param-extension:扩展字段	
	public void onRoomExtensionUpdate(QExtension extension)

	//收到管理员审查通知
	//@param-message:消息提示	
	public void onReceivedCensorNotify(String message)
}

//弹幕服务
QDanmakuService{

	//添加弹幕监听
	//@param-listener:弹幕消息监听	
	public void addDanmakuServiceListener(QDanmakuServiceListener listener)

	//移除弹幕监听
	//@param-listener:弹幕消息监听	
	public void removeDanmakuServiceListener(QDanmakuServiceListener listener)

	//发送弹幕消息
	//@param-msg:弹幕内容	@param-extension:扩展字段	@param-callBack:发送回调	
	public void sendDanmaku(String msg,HashMap extension,QLiveCallBack callBack)
}

//弹幕消息监听
QDanmakuServiceListener{

	//收到弹幕消息
	//@param-danmaku:弹幕实体	
	public void onReceiveDanmaku(QDanmaku danmaku)
}

//聊天室服务
QChatRoomService{

	//添加聊天室监听
	//@param-chatServiceListener:监听	
	public void addServiceListener(QChatRoomServiceListener chatServiceListener)

	//移除聊天室监听
	//@param-chatServiceListener:监听	
	public void removeServiceListener(QChatRoomServiceListener chatServiceListener)

	//发c2c消息
	//@param-isCMD:是不是信令消息	@param-msg:消息内容	@param-memberID:成员im ID	@param-callBack:回调	
	public void sendCustomC2CMsg(boolean isCMD,String msg,String memberID,QLiveCallBack callBack)

	//发群消息
	//@param-isCMD:是不是信令消息	@param-msg:消息内容	@param-callBack:回调	
	public void sendCustomGroupMsg(boolean isCMD,String msg,QLiveCallBack callBack)

	//踢人
	//@param-msg:消息内容	@param-memberID:成员im ID	@param-callBack:回调	
	public void kickUser(String msg,String memberID,QLiveCallBack callBack)

	//禁言
	//@param-isMute:是否禁言	@param-msg:消息内容	@param-memberID:成员im ID	@param-duration:禁言时常	@param-callBack:回调	
	public void muteUser(boolean isMute,String msg,String memberID,long duration,QLiveCallBack callBack)

	//禁言列表
	public void getBannedMembers()

	//拉黑
	//@param-isBlock:是否拉黑	@param-memberID:成员im ID	@param-callBack:回调	
	public void blockUser(boolean isBlock,String memberID,QLiveCallBack callBack)

	//黑名单列表
	//@param-forceRefresh:是否强制拉服务端数据否则缓存	@param-callBack:	
	public void getBlockList(boolean forceRefresh,QLiveCallBack callBack)

	//添加管理员
	//@param-memberID:成员im ID	@param-callBack:回调	
	public void addAdmin(String memberID,QLiveCallBack callBack)

	//移除管理员
	//@param-msg:	@param-memberID:成员im ID	@param-callBack:回调	
	public void removeAdmin(String msg,String memberID,QLiveCallBack callBack)
}

//聊天室监听
QChatRoomServiceListener{

	//Onuserjoin.
	//@param-memberID:the member id	
	public void onUserJoin(String memberID)

	//Onuserleft.
	//@param-memberID:the member id	
	public void onUserLeft(String memberID)

	//Onreceivedc2cmsg.
	//@param-msg:the msg	
	public void onReceivedC2CMsg(TextMsg msg)

	//Onreceivedgroupmsg.
	//@param-msg:the msg	
	public void onReceivedGroupMsg(TextMsg msg)

	//Onuserkicked.
	//@param-memberID:the member id	
	public void onUserKicked(String memberID)

	//Onuserbemuted.
	//@param-isMute:the is mute	@param-memberID:the member id	@param-duration:the duration	
	public void onUserBeMuted(boolean isMute,String memberID,long duration)

	//Onadminadd.
	//@param-memberID:the member id	
	public void onAdminAdd(String memberID)

	//Onadminremoved.
	//@param-memberID:the member id	@param-reason:the reason	
	public void onAdminRemoved(String memberID,String reason)

	//添加黑名单
	//@param-memberID:	
	public void onBlockAdd(String memberID)

	//移除黑名单
	//@param-memberID:	
	public void onBlockRemoved(String memberID)
}

//
RoomPage{
	//
	public QCameraParam cameraParam
	//
	public QMicrophoneParam microphoneParam

	//
	public int getAnchorCustomLayoutID()

	//自定义布局如果需要替换自定义布局自定义主播端布局如果需要替换自定义布局
	//@param-anchorCustomLayoutID:自定义布局ID	
	public void setAnchorCustomLayoutID(int anchorCustomLayoutID)

	//
	public int getPlayerCustomLayoutID()

	//自定义布局如果需要替换自定义布局自定义主播端布局如果需要替换自定义布局
	//@param-playerCustomLayoutID:自定义布局ID	
	public void setPlayerCustomLayoutID(int playerCustomLayoutID)

	//根据房间信息自动跳转主播页直播间或观众直播间
	//@param-context:安卓上下文	@param-roomInfo:房间信息	@param-callBack:回调	
	public final void startRoomActivity(Context context,QLiveRoomInfo roomInfo,QLiveCallBack callBack)

	//根据房间信息自动跳转主播页直播间或观众直播间并且带有自定义Intent
	//@param-context:	@param-roomInfo:	@param-extSetter:	@param-callBack:	
	public final void startRoomActivity(Context context,QLiveRoomInfo roomInfo,StartRoomActivityExtSetter extSetter,QLiveCallBack callBack)

	//跳转观众直播间
	//@param-context:安卓上下文	@param-liveRoomId:房间ID	@param-callBack:回调	
	public final void startPlayerRoomActivity(Context context,String liveRoomId,QLiveCallBack callBack)

	//跳转观众直播间并且带有自定义Intent
	//@param-context:	@param-liveRoomId:	@param-extSetter:	@param-callBack:	
	public final void startPlayerRoomActivity(Context context,String liveRoomId,StartRoomActivityExtSetter extSetter,QLiveCallBack callBack)

	//跳转已经存在的主播直播间
	//@param-context:安卓上下文	@param-liveRoomId:直播间ID	@param-callBack:回调	
	public final void startAnchorRoomActivity(Context context,String liveRoomId,QLiveCallBack callBack)

	//跳转已经存在的主播直播间并且带有自定义Intent
	//@param-context:	@param-liveRoomId:	@param-extSetter:	@param-callBack:	
	public final void startAnchorRoomActivity(Context context,String liveRoomId,StartRoomActivityExtSetter extSetter,QLiveCallBack callBack)

	//跳转到创建直播间开播页面
	//@param-context:安卓上下文	@param-callBack:回调	
	public final void startAnchorRoomWithPreview(Context context,QLiveCallBack callBack)

	//跳转到创建直播间开播页面并且带有自定义Intent
	//@param-context:	@param-extSetter:自定义参数	@param-callBack:	
	public final void startAnchorRoomWithPreview(Context context,StartRoomActivityExtSetter extSetter,QLiveCallBack callBack)
}

//房间列表页面
RoomListPage{

	//
	public final int getCustomLayoutID()

	//设置房间列表页面的自定义布局
	//@param-layoutID:拷贝kit_activity_room_list.xml 修改后的自定义布局	
	public final void setCustomLayoutID(int layoutID)
}

//
QLiveFuncComponent{
}

//
QLiveComponent{
}

//uikit 房间里的UI组件上下文  1在UI组件中能获取平台特性的能力 如activiy 显示弹窗  2能获取房间client 主要资源和关键操作
QLiveUIKitContext{
}

//商品信息
QItem{
	//所在房间ID
	public String liveID
	//商品ID
	public String itemID
	//商品号
	public int order
	//标题
	public String title
	//商品标签 多个以,分割
	public String tags
	//缩略图
	public String thumbnail
	//链接
	public String link
	//当前价格
	public String currentPrice
	//原价
	public String originPrice
	//上架状态  已下架  PULLED(0),  已上架售卖  ON_SALE(1),  上架不能购买  ONLY_DISPLAY(2);
	public int status
	//商品扩展字段
	public Map extensions
	//商品讲解录制信息
	public RecordInfo record
}

//商品讲解录制信息
QItem.RecordInfo{
	//录制完成
	public static int RECORD_STATUS_FINISHED
	//等待处理
	public static int RECORD_STATUS_WAITING
	//正在生成视频
	public static int RECORD_STATUS_GENERATING
	//失败
	public static int RECORD_STATUS_ERROR
	//正在录制
	public static int RECORD_STATUS_RECORDING
	//录制ID
	public int id
	//播放路径
	public String recordURL
	//开始时间戳
	public long start
	//结束时间戳
	public long end
	//状态
	public int status
	//所在直播间ID
	public String liveID
	//所在商品ID
	public String itemID
}

//商品状态枚举
QItemStatus{
	//已下架
	public static final QItemStatus PULLED
	//已上架售卖
	public static final QItemStatus ON_SALE
	//上架不能购买
	public static final QItemStatus ONLY_DISPLAY

	//
	public static com.qlive.shoppingservice.QItemStatus[] values()

	//
	public static com.qlive.shoppingservice.QItemStatus valueOf()

	//
	public int getValue()
}

//商品顺序参数
QOrderParam{
	//商品ID
	public String itemID
	//调节后的顺序
	public int order
}

//单个商品调节顺序
QSingleOrderParam{
	//商品ID
	public String itemID
	//原来的顺序
	public int from
	//调节后的顺序
	public int to
}

//购物服务
QShoppingService{

	//获取直播间所有商品
	//@param-callBack:回调	
	public void getItemList(QLiveCallBack callBack)

	//跟新商品状态
	//@param-itemID:商品ID	@param-status:商品状态	@param-callBack:回调	
	public void updateItemStatus(String itemID,QItemStatus status,QLiveCallBack callBack)

	//
	public void updateItemStatus()

	//跟新商品扩展字段并通知房间所有人
	//@param-item:商品	@param-extension:扩展字段	@param-callBack:回调	
	public void updateItemExtension(QItem item,QExtension extension,QLiveCallBack callBack)

	//设置讲解中的商品并通知房间所有人
	//@param-item:商品	@param-callBack:回调	
	public void setExplaining(QItem item,QLiveCallBack callBack)

	//取消设置讲解中的商品并通知房间所有人
	//@param-callBack:回调	
	public void cancelExplaining(QLiveCallBack callBack)

	//获取当前讲解中的
	public com.qlive.shoppingservice.QItem getExplaining()

	//跟新单个商品顺序
	//@param-param:调节顺序	@param-callBack:回调	
	public void changeSingleOrder(QSingleOrderParam param,QLiveCallBack callBack)

	//跟新单个商品顺序
	//@param-params:所有商品 调节后的顺序	@param-callBack:回调	
	public void changeOrder(List params,QLiveCallBack callBack)

	//删除商品
	//@param-itemIDS:	@param-callBack:	
	public void deleteItems(List itemIDS,QLiveCallBack callBack)

	//添加购物服务监听
	//@param-listener:监听	
	public void addServiceListener(QShoppingServiceListener listener)

	//移除商品监听
	//@param-listener:监听	
	public void removeServiceListener(QShoppingServiceListener listener)

	//开始录制正在讲解的商品
	//@param-callBack:回调	
	public void startRecord(QLiveCallBack callBack)

	//删除讲解中的商品
	//@param-recordIds:商品ID列表	@param-callBack:回调	
	public void deleteRecord(List recordIds,QLiveCallBack callBack)

	//
	public void statsQItemClick()
}

//购物车服务监听
QShoppingServiceListener{

	//正在展示的商品切换通知
	//@param-item:商品	
	public void onExplainingUpdate(QItem item)

	//商品扩展字段跟新通知
	//@param-item:商品	@param-extension:扩展字段	
	public void onExtensionUpdate(QItem item,QExtension extension)

	//主播操作了商品列表商品列表变化
	public void onItemListUpdate()
}

//礼物模型
QGift{
	//
	public int giftID
	//礼物类型
	public int type
	//礼物名称
	public String name
	//礼物金额，0 表示自定义金额
	public int amount
	//礼物图片
	public String img
	//动态效果类型
	public String animationType
	//动态效果图片
	public String animationImg
	//排序，从小到大排序，相同order 根据创建时间排序',
	public int order
	//创建时间
	public long createdAt
	//更新时间
	public long updatedAt
	//扩展字段
	public Map extension
}

//礼物消息
QGiftMsg{
	//
	public static String GIFT_ACTION
	//所在直播间
	public String liveID
	//礼物信息
	public QGift gift
	//发送者信息
	public QLiveUser sender
}

//礼物服务
QGiftService{

	//发礼物
	//@param-giftID:	@param-amount:	@param-callback:	
	public void sendGift(int giftID,int amount,QLiveCallBack callback)

	//添加礼物监听
	//@param-listener:	
	public void addGiftServiceListener(QGiftServiceListener listener)

	//移除礼物监听
	//@param-listener:	
	public void removeGiftServiceListener(QGiftServiceListener listener)
}

//礼物监听
QGiftServiceListener{

	//收到礼物消息
	//@param-giftMsg:	
	public void onReceivedGiftMsg(QGiftMsg giftMsg)
}

//点赞
QLike{
	//直播间ID
	public String liveID
	//点赞数量
	public int count
	//点赞者
	public QLiveUser sender
}

//点赞响应
QLikeResponse{
	//直播间总点赞数
	public int total
	//我在直播间内的总点赞数
	public int count
}

//点赞服务
QLikeService{

	//点赞
	//@param-count:单次点赞数量	@param-callback:	
	public void like(int count,QLiveCallBack callback)

	//添加点赞监听
	//@param-listener:	
	public void addLikeServiceListener(QLikeServiceListener listener)

	//移除点赞监听
	//@param-listener:	
	public void removeLikeServiceListener(QLikeServiceListener listener)
}

//点赞监听
QLikeServiceListener{

	//有人点赞
	//@param-like:	
	public void onReceivedLikeMsg(QLike like)
}

//当前房间播放的音乐信息
QKTVMusic{
	//
	public static int playStatus_pause
	//
	public static int playStatus_playing
	//
	public static int playStatus_error
	//
	public static int playStatus_completed
	//
	public static int playStatus_stop
	//
	public static String track_accompany
	//
	public static String track_originVoice
	//
	public static String track_lrc
	//音乐ID
	public String musicId
	//混音主人ID
	public String mixerUid
	//开始播放的时间戳
	public long startTimeMillis
	//当前进度对应的时间戳
	public long currentTimeMillis
	//当前播放进度
	public long currentPosition
	//播放状态 0 暂停  1 播放  2 出错
	public int playStatus
	//音乐总长度
	public long duration
	//音轨名称
	public String track
	//播放的歌曲信息
	public String musicInfo
	//轨道信息
	public HashMap tracks
}

//ktv服务
QKTVService{

	//开始播放音乐仅仅房主能调用
	//@param-tracks:音乐轨道 key-轨道名字 value-轨道对映地址	@param-track:当前选中的轨道	@param-musicId:音乐ID	@param-startPosition:开始位置	@param-musicInfo:歌曲自定义详细信息如：json	
	public boolean play(HashMap tracks,String track,String musicId,long startPosition,String musicInfo)

	//切换轨道仅仅房主能调用
	//@param-track:	
	public void switchTrack(String track)

	//调整播放位置仅仅房主能调用
	//@param-position:	
	public void seekTo(long position)

	//暂停仅仅房主能调用
	public void pause()

	//恢复仅仅房主能调用
	public void resume()

	//设置音乐音量仅仅房主能调用
	//@param-volume:	
	public void setMusicVolume(float volume)

	//获取当前音乐音量仅仅房主能调用
	public float getMusicVolume()

	//获取当前音乐
	public com.qlive.ktvservice.QKTVMusic getCurrentMusic()

	//音乐监听房主改变音乐状态房间所有人通过监听收到状态
	//@param-listener:	
	public void addKTVServiceListener(QKTVServiceListener listener)

	//
	public void removeKTVServiceListener()
}

//音乐监听  房间里所有人都能监听到当前房间的音乐信息
QKTVServiceListener{

	//播放失败
	//@param-errorCode:	@param-msg:	
	public void onError(int errorCode,String msg)

	//开始播放
	//@param-ktvMusic:音乐信息	
	public void onStart(QKTVMusic ktvMusic)

	//切换播放音轨
	public void onSwitchTrack()

	//暂停
	public void onPause()

	//恢复
	public void onResume()

	//
	public void onStop()

	//播放进度更新
	//@param-position:进度	@param-duration:文件时长	
	public void onPositionUpdate(long position,long duration)

	//播放完成
	public void onPlayCompleted()
}

```