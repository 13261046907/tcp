package com.tcp;

import java.text.DecimalFormat;

public class ExpressionEvaluator {
    // 加法
    public static int add(int number,Integer number2) {
        return number + number2;
    }

    // 减法
    public static int subtract(int number,Integer number2) {
        return number - number2;
    }

    // 乘法
    public static int multiply(int number,Integer number2) {
        return number * number2;
    }

    // 除法
    public static String divide(int number,Integer number2) {
        double dividedByTen = (double) number / number2;
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(dividedByTen);
    }

    // 余数
    public static int modulus(int number,Integer number2) {
        return number % number2;
    }



    public static Object buildResult(Integer number,String input) {
        Object result = null;
        String numberString = input.substring(0, 3); // 截取前两位
        Integer num = Integer.valueOf(input.substring(numberString.length()));
        char operator = input.charAt(2); // 获取第三位操作符
        // 根据第三位字符进行相应的运算
        switch (operator) {
            case '+':
                result = add(number,num);
                break;
            case '-':
                result = subtract(number,num);
                break;
            case '*':
                result = multiply(number,num);
                break;
            case '/':
                result = divide(number,num);
                break;
            case '%':
                result = modulus(number,num);
                break;
        }
        return result;
    }
}
