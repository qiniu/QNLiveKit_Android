
```
//低代码直播客户端
QLive{

	//初始化
	//@param-context:安卓上下文	@param-config:sdk配置	@param-tokenGetter:token获取	
	public static void init(Context context, QSdkConfig config, QTokenGetter tokenGetter);初始化

	//登陆认证成功后才能使用qlive的功能
	//@param-callBack:操作回调	
	public static void auth(@NotNull()QLiveCallBack<Void> callBack);登陆认证成功后才能使用qlive的功能

	//跟新用户信息
	//@param-userInfo:用户参数	@param-callBack:回调函数	
	public static void setUser(@NotNull()QUserInfo userInfo, @NotNull()QLiveCallBack<Void> callBack);跟新用户信息

	//获取当前登陆用户资料
	public static QLiveUser getLoginUser();获取当前登陆用户资料

	//创建推流客户端
	public static QPusherClient createPusherClient();创建推流客户端

	//创建拉流客户端
	public static QPlayerClient createPlayerClient();创建拉流客户端

	//获取房间管理接口
	public static QRooms getRooms();获取房间管理接口

	//获得UIkit
	public static QLiveUIKit getLiveUIKit();获得UIkit
}

//UIkit客户端
QLiveUIKit{

	//获取内置页面每个页面有相应的UI配置
	//@param-pageClass:页面的类 目前子类为 RoomListPage-> 房间列表页面 RoomPage->直播间页面	
	<T extends QPage>T getPage(Class<T> pageClass);获取内置页面每个页面有相应的UI配置

	//跳转到直播列表页面
	//@param-context:安卓上下文	
	void launch(Context context);跳转到直播列表页面
}

//跟新用户资料参数
QUserInfo{
	public String avatar;//[头像]
	public String nick;//[名称]
	public HashMap extension;//[扩展字段]
}

//token获取回调
 当token过期后自动调用getTokenInfo
QTokenGetter{

	//如何获取token
	//@param-callback:业务（同步/异步）获取后把结果通知给sdk	
	void getTokenInfo(QLiveCallBack<String> callback);如何获取token
}

//sdk 配置
QSdkConfig{
	public boolean isLogAble;//[打印日志开关]
	public String serverURL;//[服务器地址 默认为低代码demo地址
 如果自己部署可改为自己的服务地址]
}

//房间管理接口
QRooms{

	//创建房间
	//@param-param:创建房间参数	@param-callBack:	
	void createRoom(QCreateRoomParam param, QLiveCallBack<QLiveRoomInfo> callBack);创建房间

	//删除房间
	//@param-roomID:房间ID	@param-callBack:	
	void deleteRoom(String roomID, QLiveCallBack<Void> callBack);删除房间

	//房间列表
	//@param-pageNumber:	@param-pageSize:	@param-callBack:	
	void listRoom(int pageNumber, int pageSize, QLiveCallBack<List<QLiveRoomInfo>> callBack);房间列表

	//根据ID获取房间信息
	//@param-roomID:房间ID	@param-callBack:	
	void getRoomInfo(String roomID, QLiveCallBack<QLiveRoomInfo> callBack);根据ID获取房间信息
}

//直播状态枚举
QLiveStatus{
	public static final QLiveStatus PREPARE;//[房间已创建]
	public static final QLiveStatus ON;//[房间已发布]
	public static final QLiveStatus ANCHOR_ONLINE;//[主播上线]
	public static final QLiveStatus ANCHOR_OFFLINE;//[主播已离线]
	public static final QLiveStatus OFF;//[房间已关闭]

	//
	public static com.qlive.core.QLiveStatus[] values();

	//
	public static com.qlive.core.QLiveStatus valueOf(java.lang.String name);
}

//基础回调函数
QLiveCallBack{

	//操作失败
	//@param-code:错误码	@param-msg:消息	
	void onError(int code, String msg);操作失败

	//操作成功
	//@param-data:数据	
	void onSuccess(T data);操作成功
}

//客户端类型枚举
QClientType{
	public static final QClientType PUSHER;//[Pusher 推流端]
	public static final QClientType PLAYER;//[Player 拉流观众端]

	//
	public static com.qlive.core.QClientType[] values();

	//
	public static com.qlive.core.QClientType valueOf(java.lang.String name);
}

//ui组件实现的房间生命周期
QClientLifeCycleListener{

	//进入回调
	//@param-user:进入房间的用户	@param-liveId:房间ID	
	void onEntering(@NotNull()String liveId, @NotNull()QLiveUser user);进入回调

	//加入回调
	//@param-isResumeUIFromFloating:是不是从小窗恢复回来的 -- 从小窗恢复代表原来的UI都销毁了，从新创建了一个恢复数据	@param-roomInfo:房间信息	
	void onJoined(@NotNull()QLiveRoomInfo roomInfo, boolean isResumeUIFromFloating);加入回调

	//用户离开回调
	void onLeft();用户离开回调

	//销毁
	void onDestroyed();销毁
}

//邀请处理器
QInvitationHandler{

	//发起邀请/申请
	//@param-expiration:过期时间 单位毫秒 过期后不再响应	@param-receiverRoomID:接收方所在房间ID	@param-receiverUID:接收方用户ID	@param-extension:扩展字段	@param-callBack:回调函数	
	void apply(long expiration, String receiverRoomID, String receiverUID, HashMap<String, String> extension, QLiveCallBack<QInvitation> callBack);发起邀请/申请

	//取消邀请/申请
	//@param-invitationID:邀请ID	@param-callBack:	
	void cancelApply(int invitationID, QLiveCallBack<Void> callBack);取消邀请/申请

	//接受对方的邀请/申请
	//@param-invitationID:邀请ID	@param-extension:扩展字段	@param-callBack:	
	void accept(int invitationID, HashMap<String, String> extension, QLiveCallBack<Void> callBack);接受对方的邀请/申请

	//拒绝对方
	//@param-invitationID:邀请ID	@param-extension:扩展字段	@param-callBack:	
	void reject(int invitationID, HashMap<String, String> extension, QLiveCallBack<Void> callBack);拒绝对方

	//移除监听
	//@param-listener:	
	void removeInvitationHandlerListener(QInvitationHandlerListener listener);移除监听

	//添加监听
	//@param-listener:	
	void addInvitationHandlerListener(QInvitationHandlerListener listener);添加监听
}

//邀请监听
QInvitationHandlerListener{

	//收到申请/邀请
	//@param-invitation:	
	void onReceivedApply(QInvitation invitation);收到申请/邀请

	//对方取消申请
	//@param-invitation:	
	void onApplyCanceled(QInvitation invitation);对方取消申请

	//申请/邀请超时
	//@param-invitation:	
	void onApplyTimeOut(QInvitation invitation);申请/邀请超时

	//被接受
	//@param-invitation:	
	void onAccept(QInvitation invitation);被接受

	//被拒绝
	//@param-invitation:	
	void onReject(QInvitation invitation);被拒绝
}

//创建房间参数
QCreateRoomParam{
	public String title;//[房间标题]
	public String notice;//[房间公告]
	public String coverURL;//[封面]
	public HashMap extension;//[扩展字段]
}

//弹幕实体
QDanmaku{
	public static String action_danmu;//[]
	public QLiveUser sendUser;//[发送方用户]
	public String content;//[弹幕内容]
	public String senderRoomID;//[发送方所在房间ID]
	public HashMap extension;//[扩展字段]
}

//扩展字段
QExtension{
	public String key;//[]
	public String value;//[]
}

//邀请信息
QInvitation{
	public QLiveUser initiator;//[发起方]
	public QLiveUser receiver;//[接收方]
	public String initiatorRoomID;//[发起方所在房间ID]
	public String receiverRoomID;//[接收方所在房间ID]
	public HashMap extension;//[扩展字段]
	public int invitationID;//[邀请ID]
}

//房间信息
QLiveRoomInfo{
	public String liveID;//[房间ID]
	public String title;//[房间标题]
	public String notice;//[房间公告]
	public String coverURL;//[封面]
	public Map extension;//[扩展字段]
	public QLiveUser anchor;//[主播信息]
	public String roomToken;//[]
	public String pkID;//[当前房间的pk会话信息]
	public long onlineCount;//[在线人数]
	public long startTime;//[开始时间]
	public long endTime;//[结束时间]
	public String chatID;//[聊天室ID]
	public String pushURL;//[推流地址]
	public String hlsURL;//[拉流地址]
	public String rtmpURL;//[拉流地址]
	public String flvURL;//[拉流地址]
	public Double pv;//[pv]
	public Double uv;//[uv]
	public int totalCount;//[总人数]
	public int totalMics;//[连麦者数量]
	public int liveStatus;//[直播间状态]
	public int anchorStatus;//[主播在线状态]
}

//用户
QLiveUser{
	public String userId;//[用户ID]
	public String avatar;//[用户头像]
	public String nick;//[名字]
	public Map extensions;//[扩展字段]
	public String imUid;//[用户im id]
	public String im_username;//[用户Im名称]
}

//连麦用户
QMicLinker{
	public QLiveUser user;//[麦上用户资料]
	public String userRoomID;//[]
	public HashMap extension;//[扩展字段]
	public boolean isOpenMicrophone;//[是否开麦克风]
	public boolean isOpenCamera;//[是否开摄像头]
}

//pk 会话
QPKSession{
	public String sessionID;//[PK场次ID]
	public QLiveUser initiator;//[发起方]
	public QLiveUser receiver;//[接受方]
	public String initiatorRoomID;//[发起方所在房间]
	public String receiverRoomID;//[接受方所在房间]
	public Map extension;//[扩展字段]
	public int status;//[pk状态
     RelaySessionStatusWaitAgree(0),//等待接收方同意
     RelaySessionStatusAgreed(1),//接收方已同意
     RelaySessionStatusInitSuccess(2),//发起方已经完成跨房，等待对方完成
     RelaySessionStatusRecvSuccess(3),//接收方已经完成跨房，等待对方完成
     RelaySessionStatusSuccess(4),//两方都完成跨房
     RelaySessionStatusRejected(5),//接收方拒绝
     RelaySessionStatusStopped(6),//结束]
	public long startTimeStamp;//[pk开始时间戳]
}

//公屏类型消息
QPublicChat{
	public static String action_welcome;//[类型 -- 加入房间欢迎]
	public static String action_bye;//[类型 -- 离开房间]
	public static String action_like;//[类型 -- 点赞]
	public static String action_puchat;//[类型 -- 公屏输入]
	public static String action_pubchat_custom;//[]
	public String action;//[消息类型]
	public QLiveUser sendUser;//[发送方]
	public String content;//[消息体]
	public String senderRoomId;//[发送方所在房间ID]
}

//拉流事件回调
QPlayerEventListener{

	//拉流器准备中
	//@param-preparedTime:准备耗时	
	void onPrepared(int preparedTime);拉流器准备中

	//拉流器信息回调
	//@param-what:事件 参考七牛霹雳播放器	@param-extra:数据	
	void onInfo(int what, int extra);拉流器信息回调

	//拉流缓冲跟新
	//@param-percent:缓冲比分比	
	void onBufferingUpdate(int percent);拉流缓冲跟新

	///视频尺寸变化回调
	//@param-width:变化后的宽	@param-height:变化后高	
	void onVideoSizeChanged(int width, int height);/视频尺寸变化回调

	//播放出错回调
	//@param-errorCode:错误码 参考七牛霹雳播放器	
	boolean onError(int errorCode);播放出错回调
}

//默认美颜参数（免费）
QBeautySetting{

	//
	public boolean isEnabled();

	//设置是否可用
	public void setEnable(boolean enable);设置是否可用

	//
	public float getSmoothLevel();

	//磨皮等级
	//@param-smoothLevel:0.0 -1.0	
	public void setSmoothLevel(float smoothLevel);磨皮等级

	//
	public float getWhiten();

	//设置美白等级
	//@param-whiten:0.0 -1.0	
	public void setWhiten(float whiten);设置美白等级

	//
	public float getRedden();

	//设置红润等级0.0-1.0
	//@param-redden:	
	public void setRedden(float redden);设置红润等级0.0-1.0
}

//rtc推流链接状态监听
QConnectionStatusLister{

	//rtc推流链接状态
	//@param-state:状态枚举	
	void onConnectionStatusChanged(QRoomConnectionState state);rtc推流链接状态
}

//观众播放器预览
 子类 QPlayerTextureRenderView 和 QSurfaceRenderView
QPlayerRenderView{

	//设置预览模式
	//@param-previewMode:预览模式枚举	
	void setDisplayAspectRatio(PreviewMode previewMode);设置预览模式

	//
	void setRenderCallback(QRenderCallback rendCallback);

	//
	View getView();

	//
	Surface getSurface();
}

//麦克风参数
QMicrophoneParam{
	public int sampleRate;//[采样率 默认值48000]
	public int bitsPerSample;//[采样位深 默认16]
	public int channelCount;//[通道数 默认 1]
	public int bitrate;//[码率 默认64]
}

//摄像头参数
QCameraParam{
	public static int DEFAULT_BITRATE;//[默认码率]
	public int width;//[分辨率宽 默认值 720]
	public int height;//[分辨高  默认值 1280]
	public int FPS;//[帧率 默认值25]
	public int bitrate;//[码率 默认值1500]
}

//
QMixStreaming{
}

//混流画布参数
QMixStreaming.MixStreamParams{
	public static int DEFAULT_BITRATE;//[]
	public int mixStreamWidth;//[混流画布宽]
	public int mixStringHeight;//[混流画布高]
	public int mixBitrate;//[混流码率]
	public int FPS;//[混流帧率]
	public TranscodingLiveStreamingImage backGroundImg;//[混流背景图片]
}

//背景图片
QMixStreaming.TranscodingLiveStreamingImage{
	public String url;//[背景图网络url]
	public int x;//[x坐标]
	public int y;//[y坐标]
	public int width;//[背景图宽]
	public int height;//[背景图高]
}

//
QMixStreaming.TrackMergeOption{
}

//摄像头混流参数
QMixStreaming.CameraMergeOption{
	public boolean isNeed;//[是否参与混流]
	public int x;//[x坐标]
	public int y;//[y坐标]
	public int z;//[z坐标]
	public int width;//[用户视频宽]
	public int height;//[用户视频高]
}

//某个用户的混流参数
 只需要指定用户ID 和他的摄像头麦克风混流参数
QMixStreaming.MergeOption{
	public String uid;//[用户混流参数的ID]
	public CameraMergeOption cameraMergeOption;//[视频混流参数]
	public MicrophoneMergeOption microphoneMergeOption;//[音频混流参数]
}

//麦克风混流参数
QMixStreaming.MicrophoneMergeOption{
	public boolean isNeed;//[是否参与混流]
}

//推流客户端（主播端）
QPusherClient{

	//获取插件服务实例
	//@param-serviceClass:插件的类	
	@Override()<T extends QLiveService>T getService(Class<T> serviceClass);获取插件服务实例

	//设置直播状态回调
	//@param-liveStatusListener:直播事件监听	
	@Override()void addLiveStatusListener(QLiveStatusListener liveStatusListener);设置直播状态回调

	//
	@Override()void removeLiveStatusListener(QLiveStatusListener liveStatusListener);

	//当前客户端类型QClientType.PUSHER代表推流端QClientType.PLAYER代表拉流端
	@Override()QClientType getClientType();当前客户端类型QClientType.PUSHER代表推流端QClientType.PLAYER代表拉流端

	//启动视频采集和预览
	//@param-cameraParam:摄像头参数	@param-renderView:预览窗口	
	void enableCamera(QCameraParam cameraParam, QPushRenderView renderView);启动视频采集和预览

	//启动麦克采集
	//@param-microphoneParam:麦克风参数	
	void enableMicrophone(QMicrophoneParam microphoneParam);启动麦克采集

	//加入房间
	//@param-roomID:房间ID	@param-callBack:回调函数	
	void joinRoom(String roomID, QLiveCallBack<QLiveRoomInfo> callBack);加入房间

	//主播关闭房间
	//@param-callBack:	
	void closeRoom(QLiveCallBack<Void> callBack);主播关闭房间

	//主播离开房间房间不关闭
	//@param-callBack:	
	void leaveRoom(QLiveCallBack<Void> callBack);主播离开房间房间不关闭

	//销毁推流客户端销毁后不能使用
	void destroy();销毁推流客户端销毁后不能使用

	//主播设置推流链接状态监听
	//@param-connectionStatusLister:	
	void setConnectionStatusLister(QConnectionStatusLister connectionStatusLister);主播设置推流链接状态监听

	//Switchcamera
	//@param-callBack:切换摄像头回调	
	void switchCamera(QLiveCallBack<QCameraFace> callBack);Switchcamera

	//禁/不禁用本地视频流禁用后本地能看到预览观众不能看到主播的画面
	//@param-muted:是否禁用	@param-callBack:	
	void muteCamera(boolean muted, QLiveCallBack<Boolean> callBack);禁/不禁用本地视频流禁用后本地能看到预览观众不能看到主播的画面

	//禁用麦克风推流
	//@param-muted:是否禁用	@param-callBack:	
	void muteMicrophone(boolean muted, QLiveCallBack<Boolean> callBack);禁用麦克风推流

	//设置视频帧回调
	//@param-frameListener:视频帧监听	
	void setVideoFrameListener(QVideoFrameListener frameListener);设置视频帧回调

	//设置本地音频数据监听
	//@param-frameListener:音频帧回调	
	void setAudioFrameListener(QAudioFrameListener frameListener);设置本地音频数据监听

	//暂停
	void pause();暂停

	//恢复
	void resume();恢复

	//设置默认免费版美颜参数
	//@param-beautySetting:美颜参数	
	void setDefaultBeauty(QBeautySetting beautySetting);设置默认免费版美颜参数
}

//推流预览窗口
 子类实现 QPushSurfaceView 和 QPushTextureView
QPushRenderView{

	//
	View getView();

	//renview
	QNRenderView getQNRender();renview
}

//拉流客户端
QPlayerClient{

	//获取插件服务实例
	//@param-serviceClass:插件的类	
	@Override()<T extends QLiveService>T getService(Class<T> serviceClass);获取插件服务实例

	//设置直播状态回调
	//@param-liveStatusListener:直播事件监听	
	@Override()void addLiveStatusListener(QLiveStatusListener liveStatusListener);设置直播状态回调

	//
	@Override()void removeLiveStatusListener(QLiveStatusListener liveStatusListener);

	//当前客户端类型QClientType.PUSHER代表推流端QClientType.PLAYER代表拉流端
	@Override()QClientType getClientType();当前客户端类型QClientType.PUSHER代表推流端QClientType.PLAYER代表拉流端

	//加入房间
	//@param-roomID:房间ID	@param-callBack:回调	
	void joinRoom(String roomID, QLiveCallBack<QLiveRoomInfo> callBack);加入房间

	//离开房间离开后可继续加入其他房间如上下滑动切换房间
	//@param-callBack:回调	
	void leaveRoom(QLiveCallBack<Void> callBack);离开房间离开后可继续加入其他房间如上下滑动切换房间

	//销毁释放资源离开房间后退出页面不再使用需要释放
	@Override()void destroy();销毁释放资源离开房间后退出页面不再使用需要释放

	//设置预览窗口内置QPlayerTextureRenderView(推荐)/QSurfaceRenderView
	//@param-renderView:预览窗口	
	void play(@NotNull()QPlayerRenderView renderView);设置预览窗口内置QPlayerTextureRenderView(推荐)/QSurfaceRenderView

	//暂停
	void pause();暂停

	//恢复
	void resume();恢复

	//添加播放器事件监听
	//@param-playerEventListener:播放器事件监听	
	void addPlayerEventListener(QPlayerEventListener playerEventListener);添加播放器事件监听

	//移除播放器事件监听
	//@param-playerEventListener:播放器事件监听	
	void removePlayerEventListener(QPlayerEventListener playerEventListener);移除播放器事件监听
}

//直播状态监听
QLiveStatusListener{

	//直播间状态变化业务状态
	//@param-liveStatus:业务状态	
	void onLiveStatusChanged(QLiveStatus liveStatus);直播间状态变化业务状态
}

//连麦服务
QLinkMicService{

	//获取当前房间所有连麦用户
	List<QMicLinker> getAllLinker();获取当前房间所有连麦用户

	//设置某人的连麦视频预览麦上用户调用上麦后才会使用切换成rtc连麦下麦后使用拉流预览
	//@param-uID:用户ID	@param-preview:预览窗口	
	void setUserPreview(String uID, QPushRenderView preview);设置某人的连麦视频预览麦上用户调用上麦后才会使用切换成rtc连麦下麦后使用拉流预览

	//踢人
	//@param-uID:用户ID	@param-msg:附加消息	@param-callBack:操作回调	
	void kickOutUser(String uID, String msg, QLiveCallBack<Void> callBack);踢人

	//跟新扩展字段
	//@param-micLinker:麦位置	@param-QExtension:扩展字段	
	void updateExtension(@NotNull()QMicLinker micLinker, QExtension QExtension, QLiveCallBack<Void> callBack);跟新扩展字段

	//添加麦位监听
	//@param-listener:麦位监听	
	void addMicLinkerListener(QLinkMicServiceListener listener);添加麦位监听

	//移除麦位监听
	//@param-listener:麦位监听	
	void removeMicLinkerListener(QLinkMicServiceListener listener);移除麦位监听

	//获得连麦邀请处理
	QInvitationHandler getInvitationHandler();获得连麦邀请处理

	//观众向主播连麦处理器
	QAudienceMicHandler getAudienceMicHandler();观众向主播连麦处理器

	//主播处理自己被连麦处理器
	QAnchorHostMicHandler getAnchorHostMicHandler();主播处理自己被连麦处理器
}

//麦位监听
QLinkMicServiceListener{

	//有人上麦
	//@param-micLinker:连麦者	
	void onLinkerJoin(QMicLinker micLinker);有人上麦

	//有人下麦
	//@param-micLinker:连麦者	
	void onLinkerLeft(@NotNull()QMicLinker micLinker);有人下麦

	//有人麦克风变化
	//@param-micLinker:连麦者	
	void onLinkerMicrophoneStatusChange(@NotNull()QMicLinker micLinker);有人麦克风变化

	//有人摄像头状态变化
	//@param-micLinker:连麦者	
	void onLinkerCameraStatusChange(@NotNull()QMicLinker micLinker);有人摄像头状态变化

	//有人被踢
	//@param-micLinker:连麦者	@param-msg:自定义扩展消息	
	void onLinkerKicked(@NotNull()QMicLinker micLinker, String msg);有人被踢

	//有人扩展字段变化
	//@param-micLinker:连麦者	@param-QExtension:扩展信息	
	void onLinkerExtensionUpdate(@NotNull()QMicLinker micLinker, QExtension QExtension);有人扩展字段变化
}

//主播端连麦器
QAnchorHostMicHandler{

	//设置混流适配器
	//@param-QLinkMicMixStreamAdapter:混流适配器	
	public void setMixStreamAdapter(QLinkMicMixStreamAdapter QLinkMicMixStreamAdapter);设置混流适配器
}

//混流适配器
QLinkMicMixStreamAdapter{

	//连麦开始如果要自定义混流画布和背景返回空则主播推流分辨率有多大就多大默认实现
	QMixStreaming.MixStreamParams onMixStreamStart();连麦开始如果要自定义混流画布和背景返回空则主播推流分辨率有多大就多大默认实现

	//混流布局适配
	//@param-micLinkers:变化后所有连麦者	@param-target:当前变化的连麦者	@param-isJoin:当前变化的连麦者是新加还是离开	
	List<QMixStreaming.MergeOption> onResetMixParam(List<QMicLinker> micLinkers, QMicLinker target, boolean isJoin);混流布局适配
}

//观众连麦器
QAudienceMicHandler{

	//添加连麦监听
	//@param-listener:监听	
	void addLinkMicListener(LinkMicHandlerListener listener);添加连麦监听

	//移除连麦监听
	//@param-listener:监听	
	void removeLinkMicListener(LinkMicHandlerListener listener);移除连麦监听

	//开始上麦
	//@param-extension:麦位扩展字段	@param-cameraParams:摄像头参数 空代表不开	@param-microphoneParams:麦克参数  空代表不开	@param-callBack:上麦成功失败回调	
	void startLink(HashMap<String, String> extension, QCameraParam cameraParams, QMicrophoneParam microphoneParams, QLiveCallBack<Void> callBack);开始上麦

	//我是不是麦上用户
	boolean isLinked();我是不是麦上用户

	//结束连麦
	//@param-callBack:操作回调	
	void stopLink(QLiveCallBack<Void> callBack);结束连麦

	//上麦后可以切换摄像头
	//@param-callBack:	
	void switchCamera(QLiveCallBack<QCameraFace> callBack);上麦后可以切换摄像头

	//上麦后可以禁言本地视频流
	//@param-muted:	@param-callBack:	
	void muteCamera(boolean muted, QLiveCallBack<Boolean> callBack);上麦后可以禁言本地视频流

	//上麦后可以禁用本地音频流
	//@param-muted:	@param-callBack:	
	void muteMicrophone(boolean muted, QLiveCallBack<Boolean> callBack);上麦后可以禁用本地音频流

	//上麦后可以设置本地视频帧回调
	//@param-frameListener:	
	void setVideoFrameListener(QVideoFrameListener frameListener);上麦后可以设置本地视频帧回调

	//上麦后可以设置音频帧回调
	//@param-frameListener:	
	void setAudioFrameListener(QAudioFrameListener frameListener);上麦后可以设置音频帧回调

	//上麦后可以设置免费的默认美颜参数
	//@param-beautySetting:	
	void setDefaultBeauty(QBeautySetting beautySetting);上麦后可以设置免费的默认美颜参数
}

//观众连麦处理器监听
 观众需要处理的事件
QAudienceMicHandler.LinkMicHandlerListener{

	//连麦模式连接状态连接成功后连麦器会主动禁用推流器改用rtc
	//@param-state:状态	
	void onConnectionStateChanged(QRoomConnectionState state);连麦模式连接状态连接成功后连麦器会主动禁用推流器改用rtc

	//本地角色变化
	//@param-isLinker:当前角色是不是麦上用户 上麦后true 下麦后false	
	void onRoleChange(boolean isLinker);本地角色变化
}

//音频帧监听
QAudioFrameListener{

	//音频帧回调
	//@param-srcBuffer:输入pcm数据	@param-size:大小	@param-bitsPerSample:位深	@param-sampleRate:采样率	@param-numberOfChannels:通道数	
	void onAudioFrameAvailable(ByteBuffer srcBuffer, int size, int bitsPerSample, int sampleRate, int numberOfChannels);音频帧回调
}

//视频帧监听
QVideoFrameListener{

	//yuv帧回调
	//@param-data:yuv数据	@param-type:帧类型	@param-width:宽	@param-height:高	@param-rotation:旋转角度	@param-timestampNs:时间戳	
	default void onYUVFrameAvailable(byte[] data, QVideoFrameType type, int width, int height, int rotation, long timestampNs);yuv帧回调

	//纹理ID回调
	//@param-textureID:输入的纹理ID	@param-type:纹理类型	@param-width:宽	@param-height:高	@param-rotation:旋转角度	@param-timestampNs:时间戳	@param-transformMatrix:转化矩阵	
	default int onTextureFrameAvailable(int textureID, QVideoFrameType type, int width, int height, int rotation, long timestampNs, float[] transformMatrix);纹理ID回调
}

//pk服务
QPKService{

	//主播设置混流适配器
	//@param-adapter:混流适配	
	void setPKMixStreamAdapter(QPKMixStreamAdapter adapter);主播设置混流适配器

	//添加pk监听
	//@param-QPKServiceListener:	
	void addServiceListener(QPKServiceListener QPKServiceListener);添加pk监听

	//移除pk监听
	//@param-QPKServiceListener:	
	void removeServiceListener(QPKServiceListener QPKServiceListener);移除pk监听

	//开始pk
	//@param-timeoutTimestamp:等待对方流超时时间时间戳 毫秒	@param-receiverRoomID:接受方所在房间ID	@param-receiverUID:接收方用户ID	@param-extension:扩展字段	@param-callBack:操作回调函数	
	void start(long timeoutTimestamp, String receiverRoomID, String receiverUID, HashMap<String, String> extension, QLiveCallBack<QPKSession> callBack);开始pk

	//结束pk
	//@param-callBack:操作回调	
	void stop(QLiveCallBack<Void> callBack);结束pk

	//主播设置对方的连麦预览
	//@param-view:预览窗口	
	void setPeerAnchorPreView(QPushRenderView view);主播设置对方的连麦预览

	//获得pk邀请处理
	QInvitationHandler getInvitationHandler();获得pk邀请处理

	//当前正在pk信息没有PK则空
	QPKSession currentPKingSession();当前正在pk信息没有PK则空
}

//pk回调
QPKServiceListener{

	//pk开始回调观众刚进入房间如果房间正在pk也马上会回调
	//@param-pkSession:pk会话	
	void onStart(@NotNull()QPKSession pkSession);pk开始回调观众刚进入房间如果房间正在pk也马上会回调

	//pk结束回调
	//@param-pkSession:pk会话	@param-code:-1 异常结束 0主动结束 1对方结束	@param-msg:	
	void onStop(@NotNull()QPKSession pkSession, int code, @NotNull()String msg);pk结束回调

	//主播主动开始后收对方流超时pk没有建立起来
	//@param-pkSession:pk会话	
	void onStartTimeOut(@NotNull()QPKSession pkSession);主播主动开始后收对方流超时pk没有建立起来
}

//pk混流适配器
QPKMixStreamAdapter{

	//当pk开始如何混流
	//@param-pkSession:	
	List<QMixStreaming.MergeOption> onPKLinkerJoin(@NotNull()QPKSession pkSession);当pk开始如何混流

	//pk开始时候混流画布变成多大返回null则原来主播有多大就有多大
	//@param-pkSession:	
	QMixStreaming.MixStreamParams onPKMixStreamStart(@NotNull()QPKSession pkSession);pk开始时候混流画布变成多大返回null则原来主播有多大就有多大

	//当pk结束后如果还有其他普通连麦者如何混流如果pk结束后没有其他连麦者则不会回调
	default List<QMixStreaming.MergeOption> onPKLinkerLeft();当pk结束后如果还有其他普通连麦者如何混流如果pk结束后没有其他连麦者则不会回调

	//当pk结束后如果还有其他普通连麦者如何混流如果pk结束后没有其他连麦者则不会回调返回空则默认之前的不变化
	default QMixStreaming.MixStreamParams onPKMixStreamStop();当pk结束后如果还有其他普通连麦者如何混流如果pk结束后没有其他连麦者则不会回调返回空则默认之前的不变化
}

//公屏服务
QPublicChatService{

	//发送公聊
	//@param-msg:公屏消息内容	@param-callBack:操作回调	
	public void sendPublicChat(String msg, QLiveCallBack<QPublicChat> callBack);发送公聊

	//发送进入消息
	//@param-msg:消息内容	@param-callBack:操作回调	
	public void sendWelCome(String msg, QLiveCallBack<QPublicChat> callBack);发送进入消息

	//发送拜拜
	//@param-msg:消息内容	@param-callBack:操作回调	
	public void sendByeBye(String msg, QLiveCallBack<QPublicChat> callBack);发送拜拜

	//点赞
	//@param-msg:消息内容
            * @param callBack 操作回调	
	public void sendLike(String msg, QLiveCallBack<QPublicChat> callBack);点赞

	//自定义要显示在公屏上的消息
	//@param-action:消息code 用来区分要做什么响应	@param-msg:消息内容	@param-callBack:回调	
	public void sendCustomPubChat(String action, String msg, QLiveCallBack<QPublicChat> callBack);自定义要显示在公屏上的消息

	//往本地公屏插入消息不发送到远端
	public void pubMsgToLocal(QPublicChat chatModel);往本地公屏插入消息不发送到远端

	//添加监听
	//@param-lister:	
	public void addServiceLister(QPublicChatServiceLister lister);添加监听

	//移除监听
	//@param-lister:	
	public void removeServiceLister(QPublicChatServiceLister lister);移除监听
}

//
QPublicChatServiceLister{

	//收到公聊消息pubChat.action可以区分是啥类型的公聊消息
	//@param-pubChat:消息实体	
	void onReceivePublicChat(QPublicChat pubChat);收到公聊消息pubChat.action可以区分是啥类型的公聊消息
}

//房间服务
QRoomService{

	//添加监听
	//@param-listener:	
	public void addRoomServiceListener(QRoomServiceListener listener);添加监听

	//移除监听
	//@param-listener:	
	public void removeRoomServiceListener(QRoomServiceListener listener);移除监听

	//获取当前房间
	public QLiveRoomInfo getRoomInfo();获取当前房间

	//刷新房间信息
	public void getRoomInfo(QLiveCallBack<QLiveRoomInfo> callBack);刷新房间信息

	//跟新直播扩展信息
	//@param-extension:扩展字段	@param-callBack:操作回调	
	public void updateExtension(QExtension extension, QLiveCallBack<Void> callBack);跟新直播扩展信息

	//当前房间在线用户
	//@param-pageNum:页号 1开始	@param-pageSize:每页大小	@param-callBack:操作回调	
	public void getOnlineUser(int pageNum, int pageSize, QLiveCallBack<List<QLiveUser>> callBack);当前房间在线用户

	//某个房间在线用户
	//@param-pageNum:页号 1开始	@param-pageSize:每页大小	@param-callBack:操作回调	@param-roomId:房间ID	
	public void getOnlineUser(int pageNum, int pageSize, String roomId, QLiveCallBack<List<QLiveUser>> callBack);某个房间在线用户

	//使用用户ID搜索房间用户
	//@param-uid:用户ID	@param-callBack:操作回调	
	public void searchUserByUserId(String uid, QLiveCallBack<QLiveUser> callBack);使用用户ID搜索房间用户

	//使用用户imuid搜索用户
	//@param-imUid:用户im 用户ID	@param-callBack:操作回调	
	public void searchUserByIMUid(String imUid, QLiveCallBack<QLiveUser> callBack);使用用户imuid搜索用户
}

//房间服务监听
QRoomServiceListener{

	//直播间某个属性变化
	//@param-extension:扩展字段	
	void onRoomExtensionUpdate(QExtension extension);直播间某个属性变化
}

//弹幕服务
QDanmakuService{

	//添加弹幕监听
	//@param-listener:弹幕消息监听	
	public void addDanmakuServiceListener(QDanmakuServiceListener listener);添加弹幕监听

	//移除弹幕监听
	//@param-listener:弹幕消息监听	
	public void removeDanmakuServiceListener(QDanmakuServiceListener listener);移除弹幕监听

	//发送弹幕消息
	//@param-msg:弹幕内容	@param-extension:扩展字段	@param-callBack:发送回调	
	public void sendDanmaku(String msg, HashMap<String, String> extension, QLiveCallBack<QDanmaku> callBack);发送弹幕消息
}

//弹幕消息监听
QDanmakuServiceListener{

	//收到弹幕消息
	//@param-danmaku:弹幕实体	
	void onReceiveDanmaku(QDanmaku danmaku);收到弹幕消息
}

//聊天室服务
QChatRoomService{

	//添加聊天室监听
	//@param-chatServiceListener:监听	
	public void addServiceListener(QChatRoomServiceListener chatServiceListener);添加聊天室监听

	//移除聊天室监听
	//@param-chatServiceListener:监听	
	public void removeServiceListener(QChatRoomServiceListener chatServiceListener);移除聊天室监听

	//发c2c消息
	//@param-msg:消息内容	@param-memberID:成员im ID	@param-callBack:回调	
	void sendCustomC2CMsg(String msg, String memberID, QLiveCallBack<Void> callBack);发c2c消息

	//发群消息
	//@param-msg:消息内容	@param-callBack:回调	
	void sendCustomGroupMsg(String msg, QLiveCallBack<Void> callBack);发群消息

	//踢人
	//@param-msg:消息内容	@param-memberID:成员im ID	@param-callBack:回调	
	void kickUser(String msg, String memberID, QLiveCallBack<Void> callBack);踢人

	//禁言
	//@param-isMute:是否禁言	@param-msg:消息内容	@param-memberID:成员im ID	@param-duration:禁言时常	@param-callBack:回调	
	void muteUser(boolean isMute, String msg, String memberID, long duration, QLiveCallBack<Void> callBack);禁言

	//添加管理员
	//@param-memberID:成员im ID	@param-callBack:回调	
	void addAdmin(String memberID, QLiveCallBack<Void> callBack);添加管理员

	//移除管理员
	//@param-msg:	@param-memberID:成员im ID	@param-callBack:回调	
	void removeAdmin(String msg, String memberID, QLiveCallBack<Void> callBack);移除管理员
}

//聊天室监听
QChatRoomServiceListener{

	//Onuserjoin.
	//@param-memberID:the member id	
	default void onUserJoin(@NotNull()String memberID);Onuserjoin.

	//Onuserleft.
	//@param-memberID:the member id	
	default void onUserLeft(@NotNull()String memberID);Onuserleft.

	//Onreceivedc2cmsg.
	//@param-msg:the msg	@param-fromID:the from id	@param-toID:the to id	
	default void onReceivedC2CMsg(@NotNull()String msg, @NotNull()String fromID, @NotNull()String toID);Onreceivedc2cmsg.

	//Onreceivedgroupmsg.
	//@param-msg:the msg	@param-fromID:the from id	@param-toID:the to id	
	default void onReceivedGroupMsg(@NotNull()String msg, @NotNull()String fromID, @NotNull()String toID);Onreceivedgroupmsg.

	//Onuserkicked.
	//@param-memberID:the member id	
	default void onUserKicked(@NotNull()String memberID);Onuserkicked.

	//Onuserbemuted.
	//@param-isMute:the is mute	@param-memberID:the member id	@param-duration:the duration	
	default void onUserBeMuted(@NotNull()boolean isMute, @NotNull()String memberID, @NotNull()long duration);Onuserbemuted.

	//Onadminadd.
	//@param-memberID:the member id	
	default void onAdminAdd(@NotNull()String memberID);Onadminadd.

	//Onadminremoved.
	//@param-memberID:the member id	@param-reason:the reason	
	default void onAdminRemoved(@NotNull()String memberID, @NotNull()String reason);Onadminremoved.
}

//
RoomPage{

	//
	public int getAnchorCustomLayoutID();

	//自定义布局如果需要替换自定义布局自定义主播端布局如果需要替换自定义布局
	//@param-anchorCustomLayoutID:自定义布局ID	
	public void setAnchorCustomLayoutID(int anchorCustomLayoutID);自定义布局如果需要替换自定义布局自定义主播端布局如果需要替换自定义布局

	//
	public int getPlayerCustomLayoutID();

	//自定义布局如果需要替换自定义布局自定义主播端布局如果需要替换自定义布局
	//@param-playerCustomLayoutID:自定义布局ID	
	public void setPlayerCustomLayoutID(int playerCustomLayoutID);自定义布局如果需要替换自定义布局自定义主播端布局如果需要替换自定义布局

	//根据房间信息自动跳转主播页直播间或观众直播间
	//@param-context:安卓上下文	@param-roomInfo:房间信息	@param-callBack:回调	
	public final void startRoomActivity(@NotNull()Context context, @NotNull()QLiveRoomInfo roomInfo, @Nullable()QLiveCallBack<QLiveRoomInfo> callBack);根据房间信息自动跳转主播页直播间或观众直播间

	//根据房间信息自动跳转主播页直播间或观众直播间并且带有自定义Intent
	//@param-context:	@param-roomInfo:	@param-extSetter:	@param-callBack:	
	public final void startRoomActivity(@NotNull()Context context, @NotNull()QLiveRoomInfo roomInfo, @Nullable()StartRoomActivityExtSetter extSetter, @Nullable()QLiveCallBack<QLiveRoomInfo> callBack);根据房间信息自动跳转主播页直播间或观众直播间并且带有自定义Intent

	//跳转观众直播间
	//@param-context:安卓上下文	@param-liveRoomId:房间ID	@param-callBack:回调	
	public final void startPlayerRoomActivity(@NotNull()Context context, @NotNull()String liveRoomId, @Nullable()QLiveCallBack<QLiveRoomInfo> callBack);跳转观众直播间

	//跳转观众直播间并且带有自定义Intent
	//@param-context:	@param-liveRoomId:	@param-extSetter:	@param-callBack:	
	public final void startPlayerRoomActivity(@NotNull()Context context, @NotNull()String liveRoomId, @Nullable()StartRoomActivityExtSetter extSetter, @Nullable()QLiveCallBack<QLiveRoomInfo> callBack);跳转观众直播间并且带有自定义Intent

	//跳转已经存在的主播直播间
	//@param-context:安卓上下文	@param-liveRoomId:直播间ID	@param-callBack:回调	
	public final void startAnchorRoomActivity(@NotNull()Context context, @NotNull()String liveRoomId, @Nullable()QLiveCallBack<QLiveRoomInfo> callBack);跳转已经存在的主播直播间

	//跳转已经存在的主播直播间并且带有自定义Intent
	//@param-context:	@param-liveRoomId:	@param-extSetter:	@param-callBack:	
	public final void startAnchorRoomActivity(@NotNull()Context context, @NotNull()String liveRoomId, @Nullable()StartRoomActivityExtSetter extSetter, @Nullable()QLiveCallBack<QLiveRoomInfo> callBack);跳转已经存在的主播直播间并且带有自定义Intent

	//跳转到创建直播间开播页面
	//@param-context:安卓上下文	@param-callBack:回调	
	public final void startAnchorRoomWithPreview(@NotNull()Context context, @Nullable()QLiveCallBack<QLiveRoomInfo> callBack);跳转到创建直播间开播页面

	//跳转到创建直播间开播页面并且带有自定义Intent
	//@param-context:	@param-extSetter:自定义参数	@param-callBack:	
	public final void startAnchorRoomWithPreview(@NotNull()Context context, @Nullable()StartRoomActivityExtSetter extSetter, @Nullable()QLiveCallBack<QLiveRoomInfo> callBack);跳转到创建直播间开播页面并且带有自定义Intent
}

//房间列表页面
RoomListPage{

	//
	public final int getCustomLayoutID();

	//设置房间列表页面的自定义布局
	//@param-layoutID:拷贝kit_activity_room_list.xml 修改后的自定义布局	
	public final void setCustomLayoutID(int layoutID);设置房间列表页面的自定义布局
}

//
QLiveFuncComponent{
}

//
QLiveComponent{
}

//uikit 房间里的UI组件上下文
 1在UI组件中能获取平台特性的能力 如activiy 显示弹窗
 2能获取房间client 主要资源和关键操作
QLiveUIKitContext{
	val androidContext: Context;//[安卓上下文]
	val fragmentManager: FragmentManager,;//[安卓FragmentManager 用于显示弹窗]
	val currentActivity: Activity;//[当前所在的Activity]
	val lifecycleOwner: LifecycleOwner;//[当前页面的安卓LifecycleOwner]
	 val leftRoomActionCall: (resultCall: QLiveCallBack<Void>) -> Unit;//[离开房间操作 在任意UI组件中可以操作离开房间]
	val createAndJoinRoomActionCall: (param: QCreateRoomParam, resultCall: QLiveCallBack<Void>) -> Unit;//[创建并且加入房间操作 在任意UI组件中可创建并且加入房间]
	val getPlayerRenderViewCall: () -> QPlayerRenderView?;//[获取当前播放器预览窗口 在任意UI组件中如果要对预览窗口变化可直接获取]
	 val getPusherRenderViewCall: () -> QPushRenderView?;//[获取推流预览窗口  在任意UI组件中如果要对预览窗口变化可直接获取]
}

//商品信息
QItem{
	public String liveID;//[所在房间ID]
	public String itemID;//[商品ID]
	public int order;//[商品号]
	public String title;//[标题]
	public String tags;//[商品标签 多个以,分割]
	public String thumbnail;//[缩略图]
	public String link;//[链接]
	public String currentPrice;//[当前价格]
	public String originPrice;//[原价]
	public int status;//[上架状态
 已下架
 PULLED(0),
 已上架售卖
 ON_SALE(1),
 上架不能购买
 ONLY_DISPLAY(2);]
	public Map extensions;//[商品扩展字段]
	public RecordInfo record;//[商品讲解录制信息]
}

//商品讲解录制信息
QItem.RecordInfo{
	public static int RECORD_STATUS_FINISHED;//[录制完成]
	public static int RECORD_STATUS_WAITING;//[等待处理]
	public static int RECORD_STATUS_GENERATING;//[正在生成视频]
	public static int RECORD_STATUS_ERROR;//[失败]
	public static int RECORD_STATUS_RECORDING;//[正在录制]
	public int id;//[录制ID]
	public String recordURL;//[播放路径]
	public long start;//[开始时间戳]
	public long end;//[结束时间戳]
	public int status;//[状态]
	public String liveID;//[所在直播间ID]
	public String itemID;//[所在商品ID]
}

//商品状态枚举
QItemStatus{
	public static final QItemStatus PULLED;//[已下架]
	public static final QItemStatus ON_SALE;//[已上架售卖]
	public static final QItemStatus ONLY_DISPLAY;//[上架不能购买]

	//
	public static com.qlive.shoppingservice.QItemStatus[] values();

	//
	public static com.qlive.shoppingservice.QItemStatus valueOf(java.lang.String name);

	//
	public int getValue();
}

//商品顺序参数
QOrderParam{
	public String itemID;//[商品ID]
	public int order;//[调节后的顺序]
}

//单个商品调节顺序
QSingleOrderParam{
	public String itemID;//[商品ID]
	public int from;//[原来的顺序]
	public int to;//[调节后的顺序]
}

//购物服务
QShoppingService{

	//获取直播间所有商品
	//@param-callBack:回调	
	void getItemList(QLiveCallBack<List<QItem>> callBack);获取直播间所有商品

	//跟新商品状态
	//@param-itemID:商品ID	@param-status:商品状态	@param-callBack:回调	
	void updateItemStatus(String itemID, QItemStatus status, QLiveCallBack<Void> callBack);跟新商品状态

	//
	void updateItemStatus(HashMap<String, QItemStatus> newStatus, QLiveCallBack<Void> callBack);

	//跟新商品扩展字段并通知房间所有人
	//@param-item:商品	@param-extension:扩展字段	@param-callBack:回调	
	void updateItemExtension(QItem item, QExtension extension, QLiveCallBack<Void> callBack);跟新商品扩展字段并通知房间所有人

	//设置讲解中的商品并通知房间所有人
	//@param-item:商品	@param-callBack:回调	
	void setExplaining(QItem item, QLiveCallBack<Void> callBack);设置讲解中的商品并通知房间所有人

	//取消设置讲解中的商品并通知房间所有人
	//@param-callBack:回调	
	void cancelExplaining(QLiveCallBack<Void> callBack);取消设置讲解中的商品并通知房间所有人

	//获取当前讲解中的
	QItem getExplaining();获取当前讲解中的

	//跟新单个商品顺序
	//@param-param:调节顺序	@param-callBack:回调	
	void changeSingleOrder(QSingleOrderParam param, QLiveCallBack<Void> callBack);跟新单个商品顺序

	//跟新单个商品顺序
	//@param-params:所有商品 调节后的顺序	@param-callBack:回调	
	void changeOrder(List<QOrderParam> params, QLiveCallBack<Void> callBack);跟新单个商品顺序

	//删除商品
	//@param-itemIDS:	@param-callBack:	
	void deleteItems(List<String> itemIDS, QLiveCallBack<Void> callBack);删除商品

	//添加购物服务监听
	//@param-listener:监听	
	void addServiceListener(QShoppingServiceListener listener);添加购物服务监听

	//移除商品监听
	//@param-listener:监听	
	void removeServiceListener(QShoppingServiceListener listener);移除商品监听

	//开始录制正在讲解的商品
	//@param-callBack:回调	
	void startRecord(QLiveCallBack<Void> callBack);开始录制正在讲解的商品

	//删除讲解中的商品
	//@param-recordIds:商品ID列表	@param-callBack:回调	
	void deleteRecord(List<Integer> recordIds, QLiveCallBack<Void> callBack);删除讲解中的商品
}

//购物车服务监听
QShoppingServiceListener{

	//正在展示的商品切换通知
	//@param-item:商品	
	void onExplainingUpdate(QItem item);正在展示的商品切换通知

	//商品扩展字段跟新通知
	//@param-item:商品	@param-extension:扩展字段	
	void onExtensionUpdate(QItem item, QExtension extension);商品扩展字段跟新通知

	//主播操作了商品列表商品列表变化
	void onItemListUpdate();主播操作了商品列表商品列表变化
}

```
