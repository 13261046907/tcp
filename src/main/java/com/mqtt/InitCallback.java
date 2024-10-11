package com.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * MQTT回调函数
 */
@Slf4j
@Component
public class InitCallback implements MqttCallback {

    /**
   * MQTT 断开连接会执行此方法
   */
  @Override
  public void connectionLost(Throwable cause) {
  }

  /**
   * publish发布成功后会执行到这里
   */
  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    log.info("消息发送成功");
  }

  /**
   * subscribe订阅后得到的消息会执行到这里
   * 此处是接受 publish发送的消息
   */
  @Override
  public void messageArrived(String topic, MqttMessage message) {
      try {
          String convertedHexString = byteArrayToHexString(message.getPayload());
          log.info("TOPIC: [{}] 消息: {}，id:{}", topic, convertedHexString,message.getId());
      }catch (Exception e){
          log.error(e.getMessage());
      }
  }

    private String hexToStr(String hexValue){
        int decValue = Integer.parseInt(hexValue, 16);
        double dividedByTen = (double) decValue / 10.0;
        DecimalFormat df = new DecimalFormat("0.00");
        String result = df.format(dividedByTen);
        return result;
    }

    public static byte[] hexStringToByteArray(String hexString) {
        int length = hexString.length();
        byte[] byteArray = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                + Character.digit(hexString.charAt(i+1), 16));
        }
        return byteArray;
    }

    public static String byteArrayToHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder(2 * byteArray.length);
        for (byte b : byteArray) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    public List<String> getHexList(String convertedHexString, int num){
        int startIndex = 6; // Starting index for the first humidity
        List<String> hexList = new ArrayList<>();
        int length = 4; // Length of each humidity substring
        for (int i = 0; i <= num; i++) { // Adjust the loop count based on how many substrings you want
            String hex= convertedHexString.substring(startIndex + (i * length), startIndex + ((i + 1) * length));
            String hexStr = hexToStr(hex);
            hexList.add(hexStr);
        }
        return  hexList;
    }

    public static void main(String[] args) {
        byte[] payload = hexStringToByteArray("0103000000044409");
        System.out.println(payload);

        String convertedHexString = "01030800E000260000003B0D3C";
        int num = 4;
        int startIndex = 6; // Starting index for the first humidity
        List<String> hexList = new ArrayList<>();
        int length = 4; // Length of each humidity substring
        for (int i = 0; i <= num; i++) { // Adjust the loop count based on how many substrings you want
            String hex= convertedHexString.substring(startIndex + (i * length), startIndex + ((i + 1) * length));
            int decValue = Integer.parseInt(hex, 16);
            double dividedByTen = (double) decValue / 10.0;
            DecimalFormat df = new DecimalFormat("0.00");
            String result = df.format(dividedByTen);
            hexList.add(result);
        }
        System.out.println(hexList.toString());
    }
    public Boolean messageDate(long timestamp) {
        // 转换为 Instant
        Instant instant = Instant.ofEpochSecond(timestamp);
        // 将 Instant 转换为本地日期（根据系统时区）
        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();

        // 输出转换后的日期
        System.out.println("转换后的日期: " + date);

        // 获取今天的日期
        LocalDate today = LocalDate.now();

        // 判断是否为今天
        if (date.isEqual(today)) {
            return true;
        } else {
           return false;
        }
    }

}

