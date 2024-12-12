package com.utils;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang.StringUtils;

/**
 * StrUtils
 *
 * @author liuchao
 * @created date 2022/4/13 23:06
 */
public class StrUtils extends StrUtil {

    public static String subString(String strValue, int maxLength) {
        StringBuffer resultStr = new StringBuffer();
        if(StringUtils.isBlank(strValue)){
            return null;
        }

        String chinese = "[\u0391-\uFFE5]";
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
        for (int i = 0; i < strValue.length(); i++) {
            /* 获取一个字符 */
            String temp = strValue.substring(i, i + 1);
            /* 判断是否为中文字符 */
            if (temp.matches(chinese)) {
                /* 中文字符长度为2 */
                maxLength -= 2;
            } else {
                /* 其他字符长度为1 */
                maxLength -= 1;
            }
            resultStr.append(temp);
            if (maxLength <= 0) {
                resultStr.append("...");
                return resultStr.toString();
            }

        }
        return resultStr.toString();
    }
}
