package com.qlive.sdk;

import java.util.HashMap;
import java.util.Map;

/**
 * 跟新用户资料参数
 */
public class QUserInfo {

    public QUserInfo(String avatar, String nick, HashMap<String, String> extension) {
        this.avatar = avatar;
        this.nick = nick;
        this.extension = extension;
    }

    public QUserInfo() {
    }

    /**
     * 头像
     */
    public String avatar;
    /**
     * 名称
     */
    public String nick;
    /**
     * 扩展字段
     */
    public HashMap<String, String> extension;
}


