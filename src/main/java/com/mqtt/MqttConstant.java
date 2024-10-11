package com.mqtt;

import java.text.DecimalFormat;

public class MqttConstant {

    public static final String MQTT_USERNAME = "zhihui";

    public static final String MQTT_PASSWORD = "X08nzIXE/NIFHx3gLusk8mNRehWgTGIcWU8J9eP3";


    public static void main(String[] args) {
        String hexValue = "012B";
        int decValue = Integer.parseInt(hexValue, 16);
        double dividedByTen = (double) decValue / 10.0;
        DecimalFormat df = new DecimalFormat("#.00");
        String formattedResult = df.format(dividedByTen);
        System.out.println(formattedResult);
    }

}
