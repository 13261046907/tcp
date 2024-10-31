package com.tcp;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HexUtils {
    public static List<String> getHexList(String convertedHexString, int num, Map<Integer,String> metricsMap){
        int startIndex = 6; // Starting index for the first humidity
        List<String> hexList = new ArrayList<>();
        int length = 4; // Length of each humidity substring
        for (int i = 0; i < num; i++) { // Adjust the loop count based on how many substrings you want
            Boolean isMetrics = false;
            String symbol = "";
            String symbolValue = metricsMap.get(i);
            if(StringUtils.isNotBlank(symbolValue)){
                isMetrics = true;
                symbol = symbolValue;
            }
            String hex= convertedHexString.substring(startIndex + (i * length), startIndex + ((i + 1) * length));
            String hexStr = hexToStr(hex,isMetrics,symbol);
            hexList.add(hexStr);
        }
        return  hexList;
    }

    public static String hexToStr(String hexValue, Boolean isMetrics,String symbol){
        int decValue = Integer.parseInt(hexValue, 16);
        String result = "";
        if(isMetrics && StringUtils.isNotBlank(symbol)){
            Object buildResult = ExpressionEvaluator.buildResult(decValue, symbol);
            if(!Objects.isNull(buildResult)){
                result = buildResult + "";
            }
        }else {
            result = String.valueOf(decValue);
        }
        return result;
    }

}
