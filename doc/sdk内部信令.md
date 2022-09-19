``` 
房间信令

//跟新房间扩展字段
liveroom_extension_change ,"data":{"liveId":"", "extension":extension实体}

连麦：

上麦

发送信令：
{"action":"liveroom_miclinker_join","data":"micLinker实体" }


下麦
发送信令：
{"action":"liveroom_miclinker_left","data":{"uid":"a"} }



连麦踢人
{"action":"liveroom_miclinker_kick","data":{"uid":"a","msg":"老铁你涉黄了！！"}


本地麦克风状态
liveroom_miclinker_microphone_mute  ,"data":{"uid":"a","mute":"true"}


本地摄像头状态
liveroom_miclinker_camera_mute,"data":{"uid":"a","mute":"true}


管理员禁用
liveroom_miclinker_microphone_forbidden ,"data":{"uid":"a","msg":"","forbidden":"true"}
liveroom_miclinker_camera_forbidden ,"data":{"uid":"a","msg":"","forbidden":"true"}


自定义麦位扩展字段变化
liveroom_miclinker_extension_change ,"data":{"uid":"", "extension":extension实体}


连麦邀请：

invitationName = "liveroom_linkmic_invitation"


邀请

public static String ACTION_SEND = "invite_send";
public static String ACTION_CANCEL = "invite_cancel";
public static String ACTION_ACCEPT = "invite_accept";
public static String ACTION_REJECT = "invite_reject";

pk 信令
action:
val liveroom_pk_start = "liveroom_pk_start"  date : pk会话实体 //发送给c2c对方 是协商 发送本段群里为开始
val liveroom_pk_stop = "liveroom_pk_stop"    date : pk会话实体

val liveroom_pk_extions_change = "liveroom_pk_extions_change" data {"pkSession": pk会话实体, "extions":extions实体 }

pk邀请
invitationName = "liveroom_pk_invitation"


公屏

action ->
 "liveroom-welcome" 欢迎
 "liveroom-bye-bye" 离开
 "liveroom-like"   点赞
 "liveroom-pubchat" 公聊
 "liveroom-pubchat-custom" 自定义

date  -> PubChatModel


弹幕

action_danmu = "living_danmu";
data -> 弹幕实体

``` 
