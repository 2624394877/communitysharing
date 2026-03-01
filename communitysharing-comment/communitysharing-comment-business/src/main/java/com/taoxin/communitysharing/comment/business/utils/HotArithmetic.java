package com.taoxin.communitysharing.comment.business.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class HotArithmetic {
    private static final double LIKE_TOTAL = 0.7;
    private static final double REPLAY_TOTAL = 0.3;

    /**
     * 计算热度
     * @param likeCount 点赞数
     * @param replyCount 回复数
     * @BigDecimal 高精度数字类型
     * @return 热度值
     */
    public static BigDecimal calculateHeat(long likeCount, long replyCount) {
        BigDecimal likeWeight = new BigDecimal(LIKE_TOTAL); // 点赞数权重
        BigDecimal replyWeight = new BigDecimal(REPLAY_TOTAL); // 回复数权重

        // 将参数转为BigDecimal
        BigDecimal likeTotal = new BigDecimal(likeCount);
        BigDecimal replyTotal = new BigDecimal(replyCount);

        // 计算热度值: 点赞数 * 点赞数权重 + 回复数 * 回复数权重
        BigDecimal heat = likeTotal.multiply(likeWeight).add(replyTotal.multiply(replyWeight));

        return heat.setScale(2, RoundingMode.HALF_UP); // 保留两位小数（四舍五入）
    }
}
