package com.taoxin.communitysharing.common.uitl;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class NumberUtil {
    /**
     * 数字转换字符串
     *
     * @param number
     * @return
     */
    public static String formatNumberString(long number) {
        if (number < 1000) {
            return String.valueOf(number);  // 小于 1 千显示原始数字
        }else if (number < 10000) {
            // 小于万亿，显示千单位
            double result = number / 1000.0;
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.DOWN);
            String formatted = df.format(result);
            return formatted + "千";
        } else if (number < 100000000) {
            // 小于 1 亿，显示万单位
            double result = number / 10000.0;
            DecimalFormat df = new DecimalFormat("#.#"); // 保留 1 位小数
            df.setRoundingMode(RoundingMode.DOWN); // 禁用四舍五入
            String formatted = df.format(result);
            return formatted + "万";
        } else {
            return "9999万";  // 超过 1 亿，统一显示 9999万
        }
    }
}
