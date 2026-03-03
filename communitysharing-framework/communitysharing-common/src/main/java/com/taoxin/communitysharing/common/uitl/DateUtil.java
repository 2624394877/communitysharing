package com.taoxin.communitysharing.common.uitl;

import com.taoxin.communitysharing.common.constant.DateConstants;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {
    /**
     * LocalDateTime转时间戳
     * @param localDateTime 本地时间
     * @return 时间戳
     */
    public static Long LocalTimestampToDate(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli(); // 获取时间戳
    }

    /**
     * LocalDateTime转字符串
     * @param localDateTime 本地时间
     * @return 时间字符串
     */
    public static String LocalDateTimeToString(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern(DateConstants.LOCAL_DATE_TIME_PATTERN));
    }


    /**
     * 计算相对时间
     * @param localDateTime 时间
     * @return 相对时间
     */
    public static String formatRelativeTime(LocalDateTime localDateTime) {
        // 当前时间
        LocalDateTime now = LocalDateTime.now();

        // 计算与当前时间的差距 ChronoUnit说明：时间单位
        long days = ChronoUnit.DAYS.between(localDateTime.toLocalDate(), now);
        long hours = ChronoUnit.HOURS.between(localDateTime, now);
        long minutes = ChronoUnit.MINUTES.between(localDateTime, now);

        if (days < 1) {
            // 1天内
            if (hours < 1)
                // 1小时内
                return Math.max(minutes, 1) + "分钟前";
            else
                // 1小时外
                return hours + "小时前";
        } else if (days < 2)
            return "昨天" + localDateTime.format(DateTimeFormatter.ofPattern(DateConstants.HOUR_MINUTE_PATTERN));
        else if (days < 3)
            return "前天" + localDateTime.format(DateTimeFormatter.ofPattern(DateConstants.HOUR_MINUTE_PATTERN));
        else if (days < 7)
            return days + "天前";
        else if (localDateTime.getMonth() == now.getMonth())
            return days / 7 + "周前";
        else if (localDateTime.getYear() == now.getYear()) {
            return localDateTime.format(DateTimeFormatter.ofPattern(DateConstants.MONTH_DAY_PATTERN));
        }else {
            return localDateTime.format(DateTimeFormatter.ofPattern(DateConstants.LOCAL_DATE_PATTERN));
        }
    }
}
