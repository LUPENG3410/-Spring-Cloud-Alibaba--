package com.changxing.common.constant;

public class Constants {

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_HEADER = "Authorization";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLE_HEADER = "X-User-Role";

    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    public static final long ACCESS_TOKEN_EXPIRE = 2 * 60 * 60 * 1000L;
    public static final long REFRESH_TOKEN_EXPIRE = 7 * 24 * 60 * 60 * 1000L;

    public static final String LOGIN_TOKEN_PREFIX = "login:token:";
    public static final String REFRESH_TOKEN_PREFIX = "login:refresh:";
}
