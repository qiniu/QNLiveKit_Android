package com.qlive.chatservice;

import org.jetbrains.annotations.NotNull;

/**
 *  聊天室监听
 */
public interface QChatRoomServiceListener {

    /**
     * On user join.
     *
     * @param memberID the member id
     */
    default void onUserJoin(@NotNull String memberID){};

    /**
     * On user left.
     *
     * @param memberID the member id
     */
    default void onUserLeft(@NotNull String memberID){}

    /**
     * On received c 2 c msg.
     *
     * @param msg    the msg
     * @param fromID the from id
     * @param toID   the to id
     */
    default  void onReceivedC2CMsg(@NotNull String msg,@NotNull  String fromID,@NotNull  String toID){}

    /**
     * On received group msg.
     *
     * @param msg    the msg
     * @param fromID the from id
     * @param toID   the to id
     */
    default  void onReceivedGroupMsg(@NotNull String msg,@NotNull  String fromID, @NotNull String toID){}

    /**
     * On user kicked.
     *
     * @param memberID the member id
     */
    default void onUserKicked(@NotNull String memberID){}

    /**
     * On user be muted.
     *
     * @param isMute   the is mute
     * @param memberID the member id
     * @param duration the duration
     */
    default void onUserBeMuted(@NotNull boolean isMute, @NotNull  String memberID,@NotNull  long duration){}

    /**
     * On admin add.
     *
     * @param memberID the member id
     */
    default void onAdminAdd(@NotNull String memberID){}

    /**
     * On admin removed.
     *
     * @param memberID the member id
     * @param reason   the reason
     */
    default void onAdminRemoved(@NotNull String memberID,@NotNull  String reason){}
}
