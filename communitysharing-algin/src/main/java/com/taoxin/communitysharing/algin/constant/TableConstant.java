package com.taoxin.communitysharing.algin.constant;

public class TableConstant {
    /**
     * 表中分隔符
     */
    private static final String TABLE_NAME_SEPARATE = "_";

    /**
     * 获取表后缀
     * @param date 日期
     * @param hashKey 标识符
     * @return 完整表名
     */
    public static String buildTableNameSuffix(String date, long hashKey) {
        // 拼接完整的表名
        return date + TABLE_NAME_SEPARATE + hashKey;
    }
}
