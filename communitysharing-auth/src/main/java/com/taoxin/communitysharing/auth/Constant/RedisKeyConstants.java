package com.taoxin.communitysharing.auth.Constant;


/**
 * Redis key 常量类
 * @redis数据规划：
 * 在redis中如果数据存在上下级关系则用“:”分割，如:user:roles:phone=1 表示用户phone的角色是1
 * 如果数据没有上下级关系则用“.”分割，如：communitysharing.id.generator 表示社区分享ID生成器
 */
public class RedisKeyConstants {
    /**
     * 验证码 KEY 前缀
     */
    private static final String VERIFICATION_CODE_KEY = "verification_code:";

    public static String getVerificationCodeKey(String phoneOrEmail) {
        return VERIFICATION_CODE_KEY + phoneOrEmail;
    }
}
