package com.qlive.qnlivekit.uitil;

public class BZUser {

    public Integer code;
    public String message;
    public DataDTO data;
    public String requestId;

    public static class DataDTO {
        public String accountId;
        public String nickname;
        public String avatar;
        public String phone;
        public String profile;
        public String loginToken;

        public static class ImConfigDTO {
            public String imToken;
            public Integer type;
            public String imUsername;
            public String imPassword;
            public String imUid;
            public Integer imGroupId;
        }
    }
}

