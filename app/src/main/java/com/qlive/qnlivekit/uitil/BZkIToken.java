package com.qlive.qnlivekit.uitil;

public class BZkIToken {
    public Integer code;
    public String message;
    public TokenDao data;
    public String requestId;

    public class TokenDao {
        public String accessToken;
        public Long expiresAt;
    }
}
