package com.tcp;

import cn.hutool.core.collection.CollectionUtil;
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

    public static List<String> getHexCo2List(String hexString, Map<Integer, String> metricsMap){
        // 按4个字节分割字符串
        hexString = hexString.substring(6);
        String[] dataChunks = splitIntoChunks(hexString, 4);
        // 读取所需的数据
        Integer humidity = Integer.parseInt(dataChunks[0], 16); // 湿度，字符串转十六进制转换为十进制
        Integer temperature = Integer.parseInt(dataChunks[1], 16); // 温度
        Integer co2 = Integer.parseInt(dataChunks[5], 16); // CO2
        Integer light = Integer.parseInt(dataChunks[7], 16) + Integer.parseInt(dataChunks[8], 16); // 光照
        List<Integer> hexList = new ArrayList<>();
        hexList.add(humidity);
        hexList.add(temperature);
        hexList.add(co2);
        hexList.add(light);
        List<String> hexLists = new ArrayList<>();
        if(CollectionUtil.isNotEmpty(hexList)){
            for (int i = 0; i < hexList.size(); i++) {
                String result = hexList.get(i) +"";
                String symbolValue = metricsMap.get(i);
                if(StringUtils.isNotBlank(result)){
                    if(StringUtils.isNotBlank(symbolValue)){
                        Object buildResult = ExpressionEvaluator.buildResult(hexList.get(i), symbolValue);
                        if(!Objects.isNull(buildResult)){
                            result = buildResult + "";
                        }
                    }
                    hexLists.add(result);
                }
            }
        }
        return hexLists;
    }

    public static void main(String[] args) {
        String aa = "010312015E00B700000000000002240000000001B0E985";

        // 按4个字节分割字符串
        String[] dataChunks = splitIntoChunks(aa, 4);

        // 读取所需的数据
        int humidity = Integer.parseInt(dataChunks[0], 16); // 湿度，字符串转十六进制转换为十进制
        int temperature = Integer.parseInt(dataChunks[1], 16); // 温度
        int co2 = Integer.parseInt(dataChunks[5], 16); // CO2
        int light = Integer.parseInt(dataChunks[8], 16) + Integer.parseInt(dataChunks[9], 16); // 光照

        // 输出结果
        System.out.println("湿度: " + humidity);
        System.out.println("温度: " + temperature);
        System.out.println("CO2: " + co2);
        System.out.println("光照: " + light);
    }


    private static String[] splitIntoChunks(String str, int chunkSize) {
        int numChunks = (int) Math.ceil((double) str.length() / chunkSize);
        String[] chunks = new String[numChunks];

        for (int i = 0; i < numChunks; i++) {
            int endIndex = Math.min(str.length(), (i + 1) * chunkSize);
            chunks[i] = str.substring(i * chunkSize, endIndex);
        }
        return chunks;
    }
}
