

## qlive-sdk: 低代码互动直播sdk

qlive-sdk是七牛云推出的一款互动直播低代码解决方案sdk。只需几行代码快速接入互动连麦pk直播。

#### 一，直播基础业务：房间，聊天室，连麦，pk ，购物车，小窗播放，美颜。
#### 二，扩展性：支持业务扩展，支持自定义UI
#### 三，简单，易用

[体验demo](http://fir.qnsdk.com/s6py)

[接入文档](https://developer.qiniu.com/lowcode/manual/12027/android-fast-access)

[api接口文档](https://developer.qiniu.com/lowcode/api/12032/the-android-api-documentation)

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

```    




