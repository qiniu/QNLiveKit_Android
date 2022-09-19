```kotlin
import com.qlive.sdk.QLive

val config = QLiveConfig("serverURL")
QLive.init(appContext, config, tokenGetter)
QLive.auth(callback)
QLive.setUser(QUserInfo("your avatar", "your nick", extraInfo), callback)
QLive.getLiveUIKit().launch(context)
```