package com.softsugar.library.sdk.entity;

public class AuthEntity {
    public String data;
    public String sign;
    public String token;

    @Override
    public String toString() {
        return "AuthEntity{" +
                "data='" + data + '\'' +
                ", sign='" + sign + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
