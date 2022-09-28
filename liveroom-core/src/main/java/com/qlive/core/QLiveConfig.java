package com.qlive.core;

/**
 * sdk 配置
 */
public class QLiveConfig {
    public QLiveConfig(String serverURL) {
        this.serverURL = serverURL;
    }
    public QLiveConfig(){}

    /**
     * 打印日志开关
     */
    public boolean isLogAble = true;
    /**
     * 服务器地址 默认为低代码demo地址
     * 如果自己部署可改为自己的服务地址
     */
   public String serverURL = "https://live-api.qiniu.com";
  //  public String serverURL  = "http://10.200.20.28:8099";
    //其他配置
}
