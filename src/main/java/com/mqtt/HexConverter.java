package com.mqtt;

import java.nio.charset.StandardCharsets;

public class HexConverter {

    // 将字节数据转换为十六进制字符串
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // 将十六进制字符串转换回字节数据
    public static byte[] hexToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        else if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }

        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                + Character.digit(hexString.charAt(i+1), 16));
        }
        return bytes;
    }

    public static void main(String[] args) {
        // 示例使用
        String originalString = "0103000000044409";
        byte[] originalBytes = originalString.getBytes(StandardCharsets.UTF_8);

        // 转换为十六进制字符串
        String hexString = bytesToHex(originalBytes);
        System.out.println("Hex: " + hexString);

        // 转换回字节数据
        byte[] convertedBytes = hexToBytes(hexString);
        System.out.println("Converted String: " + new String(convertedBytes, StandardCharsets.UTF_8));
    }
}