package com.utils;

import java.text.DecimalFormat;

public class StringUtil {
    public static String StringDecimalFormat(String strValue){
        // 将字符串转换为double
        double parseDouble = Double.parseDouble(strValue);

        // 创建DecimalFormat对象，指定保留一位小数
        DecimalFormat df = new DecimalFormat("#.#");
        // 格式化并输出结果
        return df.format(parseDouble);
    }
}
