package com.taoxin.communitysharing.gateway.constant;

public class RedisKeyConstants {
    private static final String USER_ROLES_KEY_PREFIX = "user:roles:";

    private static final String USER_PERMISSIONS_KEY_PREFIX = "role:permissions:";

    public static String getUserRolesKey(Long userId) {
        return USER_ROLES_KEY_PREFIX + userId;
    }

    public static String getUserPermissionsKey(String roleKey) {
        return USER_PERMISSIONS_KEY_PREFIX + roleKey;
    }
}
