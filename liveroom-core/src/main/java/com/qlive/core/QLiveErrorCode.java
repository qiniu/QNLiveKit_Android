package com.qlive.core;

public class QLiveErrorCode {
    /**
     * 未知错误
     */
    public static int UNKNOWN = -1;

    /**
     * 没有登录
     */
    public static int NOT_LOGGED_IN = -2;

    /**
     * 用户已取消操作
     */
    public static int CANCELED_JOIN = -3;

    /**
     * 没有相应的权限
     */
    public static int NO_PERMISSION = -4;

    /**
     * 当前用户不是房间成员
     */
    public static int NOT_A_ROOM_MEMBER = -5;

    /**
     * 不是连麦成员
     */
    public static int NOT_A_LINKER_MEMBER = -6;

    /**
     * pk状态错误
     */
    public static int PK_STATUS_ERROR = -7;
}
