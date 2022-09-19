#参数名不参与混淆
-keepparameternames
-keepparameternames
#保留行号
-keepattributes SourceFile,LineNumberTable
#保持泛型
-keepattributes Signature

-keepclasseswithmembernames class * {
    native <methods>;
}
#信令
-keep class com.qlive.rtm.**{*;}
-keep class com.qlive.rtminvitation.**{*;}

-keep class com.qlive.sdk.**{*;}
#coreimpl层 been不混淆
-keep class com.qlive.coreimpl.model.**{*;}
#core层不混淆
-keep class com.qlive.core.**{*;}
-keep interface com.qlive.pushclient.QPusherClient{*;}
-keep interface com.qlive.playerclient.QPlayerClient{*;}
#QLiveService
-keep class * implements com.qlive.core.QLiveService {
#匹配所有构造器
  public <init>();
}
-keep class * implements com.qlive.coreimpl.BaseService {
  #匹配所有构造器
  public <init>();
}

#im
-keep class im.floo.floolib.**{*;}
#播放器
-keep class com.pili.pldroid.player.** { *; }
-keep class com.qiniu.qplayer.mediaEngine.MediaPlayer{*;}
#rtc
-keep class org.webrtc.** {*;}
-dontwarn org.webrtc.**
-keep class com.qiniu.droid.rtc.**{*;}
-keep interface com.qiniu.droid.rtc.**{*;}

