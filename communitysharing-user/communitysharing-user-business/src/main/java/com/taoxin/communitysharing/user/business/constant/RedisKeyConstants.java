package com.taoxin.communitysharing.user.business.constant;


/**
 * Redis key 常量类
 * @redis数据规划：
 * 在redis中如果数据存在上下级关系则用“:”分割，如:user:roles:phone=1 表示用户phone的角色是1
 * 如果数据没有上下级关系则用“.”分割，如：communitysharing.id.generator 表示社区分享ID生成器
 */
public class RedisKeyConstants {

    // 社区分享 ID 生成器(redis中)
    public static final String COMMUNITYSHARING_ID_GENERATOR = "communitysharing.id.generator";

    // 用户角色 KEY 前缀
    private static final String USER_ROLES_KEY_PREFIX = "user:roles:";

    // 用户权限 KEY 前缀
    private static final String USER_PERMISSIONS_KEY_PREFIX = "role:permissions:";

    // 用户信息 KEY 前缀
    private static final String USER_INFO_KEY_PREFIX = "user:info:";

    /**
     * 验证码 KEY 前缀
     */
    private static final String VERIFICATION_CODE_KEY = "verification_code:";

    /**
     * 验证码 KEY 前缀
     * @param userId 用户 ID
     * @return 角色 KEY
     */
    public static String getUserRolesKey(Long userId) {
        return USER_ROLES_KEY_PREFIX + userId;
    }

    /**
     * 获取用户权限 KEY
     * @param roleName 角色名
     * @return 用户权限 KEY
     */
    public static String getUserPermissionsKey(String roleName) {
        return USER_PERMISSIONS_KEY_PREFIX + roleName;
    }

    public static String getUserInfoKey(Long userId) {
        return USER_INFO_KEY_PREFIX + userId;
    }

    public static String getVerificationCodeKey(String phoneOrEmail) {
        return VERIFICATION_CODE_KEY + phoneOrEmail;
    }
}
