package com.tcp;

import com.alibaba.fastjson.JSONObject;
import com.rk.domain.DeviceModel;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Modbus {

    // 生成Modbus RTU请求
    public static byte[] createModbusRequest(int slaveId, int functionCode, int registerAddress, int quantity) {
        List<Byte> request = new ArrayList<>();

        // 从站ID
        request.add((byte) slaveId);
        // 功能码
        request.add((byte) functionCode);
        // 寄存器地址高字节
        request.add((byte) (registerAddress >> 8));
        // 寄存器地址低字节
        request.add((byte) (registerAddress & 0xFF));
        // 读取数量高字节
        request.add((byte) (quantity >> 8));
        // 读取数量低字节
        request.add((byte) (quantity & 0xFF));

        // 计算CRC
        int crc = calculateCRC(request);
        request.add((byte) (crc & 0xFF)); // CRC低字节
        request.add((byte) (crc >> 8));   // CRC高字节

        // 将List转换为byte数组
        byte[] byteArray = new byte[request.size()];
        for (int i = 0; i < request.size(); i++) {
            byteArray[i] = request.get(i);
        }
        return byteArray;
    }

    // CRC计算
    private static int calculateCRC(List<Byte> request) {
        int crc = 0xFFFF; // 初始值
        for (byte b : request) {
            crc ^= b & 0xFF; // 按位异或
            for (int i = 0; i < 8; i++) { // 处理8位
                if ((crc & 0x0001) != 0) {
                    crc = (crc >> 1) ^ 0xA001; // 多项式
                } else {
                    crc >>= 1;
                }
            }
        }
        return crc;
    }

    public static String buildHexString(String hex) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    public static DeviceModel buildModel(String hex) {
        DeviceModel deviceModel = null;
        String output = buildHexString(hex);
        if(StringUtils.isNotBlank(output)){
            boolean imei = output.contains("imei");
            if(imei){
                deviceModel = JSONObject.parseObject(output, DeviceModel.class);
            }
        }
        return deviceModel;
    }

    public static void main1(String[] args) {
        int slaveId = 1; // 从站ID
        int functionCode = 3; // 功能码：读取保持寄存器
        int registerAddress = 4; // 寄存器地址（40001映射到地址为0x0000）
        int quantity = 1; // 读取的数量

        byte[] request = createModbusRequest(slaveId, functionCode, registerAddress, quantity);

        // 输出请求的十六进制字符串
        String s = bytesToHex(request);
        System.out.println(s);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b)); // 转换为两位十六进制，并添加空格
        }
        return sb.toString().trim(); // 去掉最后的空格
    }

    public static void main(String[] args) {
//        {"imei":"863121078043049","iccid":"89860842032480159440","fver":"YED_DTU2_1.1.10","csq":27}
//        String hex = "7B22696D6569223A22383633313231303738303433303439222C226963636964223A223839383630383432303332343830313539343430222C2266766572223A225945445F445455325F312E312E3130222C22637371223A32377D";
//        DeviceModel deviceModel = buildModel(hex);
//        System.out.println(deviceModel);
        String hex = "010312016500B700000000000001900000000000877EE0";
        // 去除干扰部分（这里假设干扰部分只是占位，实际需要根据真实情况处理）
        hex = hex.replaceAll("\\*+", "");
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        System.out.println(output.toString());
        String coordinates = output.toString();

        // 按下划线分割
        String[] parts = coordinates.split("_");

        // 确保分割后有两个部分
        if (parts.length == 2) {
            String longitude = parts[0]; // 第一个部分是精度
            String latitude = parts[1];  // 第二个部分是维度

            // 输出结果
            System.out.println("精度 (Longitude): " + longitude);
            System.out.println("维度 (Latitude): " + latitude);
        } else {
            System.out.println("输入格式不正确");
        }
    }
}