package com.taoxin.communitysharing.common.uitl;

import java.util.regex.Pattern;

/**
 * 参数工具类
 */
public final class ParamsUtil {
    private final static int NICK_NAME_STR_MIN_LENGTH = 2; // 昵称最小长度
    private final static int NICK_NAME_STR_MAX_LENGTH = 20; // 昵称最大长度

    private final static String NICK_NAME_STR_REGEX = "[!@#$%^&*(),.?\\\":{}|<>'\\\\/\\[\\];`=+\\-~_]+"; // 昵称正则
    private static final Pattern NICK_NAME_PATTERN = Pattern.compile(NICK_NAME_STR_REGEX); // 昵称正则模式

    /**
     * 昵称校验
     * @param nickName 昵称
     * @return true: 合法, false: 非法
     */
    public static boolean isValidNickName(String nickName) {
        // 昵称不为空
        // 昵称长度在2-20个字符之间
        // 昵称不能包含特殊字符
        return nickName != null && // 昵称不为空
                nickName.length() >= NICK_NAME_STR_MIN_LENGTH &&
                nickName.length() <= NICK_NAME_STR_MAX_LENGTH && // 昵称长度在2-20个字符之间
                !NICK_NAME_PATTERN.matcher(nickName).find();
    }


    private final static int COMMUNITYSHARING_ID_MIN_LENGTH = 6; // communitysharingId最小长度
    private final static int COMMUNITYSHARING_ID_MAX_LENGTH = 11; // communitysharingId最大长度

    private final static String COMMUNITYSHARING_ID_REGEX = "^[a-zA-Z0-9_]+$"; // communitysharingId正则表达式
    private final static Pattern COMMUNITYSHARING_ID_PATTERN = Pattern.compile(COMMUNITYSHARING_ID_REGEX); // communitysharingId正则表达式

    /**
     * 验证communitysharingId
     *
     * @param communitysharingId communitysharingId
     * @return 验证结果
     */
    public static boolean isValidCommunitysharingId(String communitysharingId) {
        if (communitysharingId.length() > COMMUNITYSHARING_ID_MAX_LENGTH || communitysharingId.length() < COMMUNITYSHARING_ID_MIN_LENGTH) {
            return false; // communitysharingId长度不符合要求
        }
        return COMMUNITYSHARING_ID_PATTERN.matcher(communitysharingId).matches(); // 匹配communitysharingId正则表达式
    }

    /**
     * 校验字符串长度
     * @param str 待校验的字符串
     * @param maxLength 最大长度
     * @return 是否合法
     */
    public static boolean isValidStringLength(String str, int maxLength) {
        // 去除空格
        str = str.trim();
        return str.length() <= maxLength;
    }

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"; // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX); // 创建正则表达式模式

    public static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches(); // 匹配邮箱
    }

    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!~.])[A-Za-z0-9!~.]{6,11}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    public static boolean isValidPassword(String password) {
    	return PASSWORD_PATTERN.matcher(password).matches();
    }

    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return PHONE_PATTERN.matcher(phoneNumber).matches();
    }
}
