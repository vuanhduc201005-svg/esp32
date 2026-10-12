package com.dducwsjvbe.web_ui.filter;

public class TokenContext {
    private static final ThreadLocal<String> ACCESS_TOKEN = new ThreadLocal<>();

    public static void setAccessToken(String token) {
        ACCESS_TOKEN.set(token);
    }

    public static String getAccessToken() {
        return ACCESS_TOKEN.get();
    }

    public static void clear() {
        ACCESS_TOKEN.remove(); // BẮT BUỘC clear, tránh leak sang request khác (thread pool tái sử dụng)
    }
}
