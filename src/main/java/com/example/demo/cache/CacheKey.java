package com.example.demo.cache;

public final class CacheKey {
    public static String myInfo(String userId) {
        return "my-info::" + userId;
    }

    public static String adminUser(String id) {
        return "admin-user::" + id;
    }
}
