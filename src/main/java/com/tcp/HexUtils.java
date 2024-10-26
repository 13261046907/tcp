package com.tcp;

import com.alibaba.fastjson.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class HexUtils {
    public static List<String> getHexList(String convertedHexString, int num,List<Integer> metricsList){
        int startIndex = 6; // Starting index for the first humidity
        List<String> hexList = new ArrayList<>();
        int length = 4; // Length of each humidity substring
        for (int i = 0; i < num; i++) { // Adjust the loop count based on how many substrings you want
            Boolean isMetrics = true;
            if(metricsList.contains(i)){
                //不需要除以10
                isMetrics = false;
            }
            String hex= convertedHexString.substring(startIndex + (i * length), startIndex + ((i + 1) * length));
            String hexStr = hexToStr(hex,isMetrics);
            hexList.add(hexStr);
        }
        return  hexList;
    }

    public static String hexToStr(String hexValue, Boolean isMetrics){
        int decValue = Integer.parseInt(hexValue, 16);
        double dividedByTen = 0;
        if(isMetrics){
            dividedByTen = (double) decValue / 10.0;
        }else {
            dividedByTen = Double.valueOf(decValue);
        }
        DecimalFormat df = new DecimalFormat("0.00");
        String result = df.format(dividedByTen);
        return result;
    }

    public static void main(String[] args) {
        List<String> hexList = getHexList("03030800BD00C5016E003ADF47",2, new ArrayList<>());
        System.out.println(hexList);
    }
}
