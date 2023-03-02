# PK打榜最佳实践

对于pk业务QLivekit默认只维护房间里有一场pk以及pk信息：双方主播信息房间信息及状态信息。QLiveUIKit实现效果如下：
![uikit-pk](http://qrnlrydxa.hn-bkt.clouddn.com/doc/pk4.png)

如要实现PK其他业务如常见的pk打榜或者其他pk事件，如下图：
![uikit-pk](http://qrnlrydxa.hn-bkt.clouddn.com/doc/pk3.png)


#### 对接步骤

![uikit-pk](http://qrnlrydxa.hn-bkt.clouddn.com/doc/pk.png)

##### 实现服务pk开始回调
配置低代码服务端的pk开始回调接口

##### 实现自定义的记分业务
业务服务端收到购买礼物请求等pk加分情况是调用服务端跟新扩展字段接口，如定义扩展字段”pk_score“为记分、”pk_win_or_lose“为输赢。
调用pk跟新扩展字段接口后pk双方房间都会收到相应的消息，并且该字段记录在QPKSession(pk会话中)，后进房间的观众也能取得当前相应的值。

##### 客户端监听事件实现自定义UI

```kotlin
 client.getService(QPKService::class.java)
            .addServiceListener(object : QPKServiceListener {
                //pk开始
                override fun onStart(pkSession: QPKSession) {
                    //如果后面进来的观众进入一个已经pk进行中的房间，取出最新的自定义字段的值恢复UI
                    pkSession.extension["pk_score"]?.let {}
                    pkSession.extension["pk_win_or_lose"]?.let {}
                }

                override fun onStop(pkSession: QPKSession, code: Int, msg: String) {}
                override fun onStartTimeOut(pkSession: QPKSession) {}

                //pk自定义扩展字段跟新
                override fun onPKExtensionChange(extension: QExtension) {
                    when (extension.key) {
                        //自定义pk分数事件
                        "pk_score" -> {}
                        //pk输赢事件
                        "pk_win_or_lose" -> {}
                    }
                }
            })
```


