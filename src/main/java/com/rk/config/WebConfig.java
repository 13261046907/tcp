package com.rk.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(
        prefix = "analyzer"
)
public class WebConfig {
    private static Integer deviceConnectPort;
    private static String imageFilePath;
    private static String notifyUrlBase;
    private static String imageUploadNotifyUrl;
    private static String deviceLoginNotifyUrl;
    private static String deviceRealTimeDataNotifyUrl;
    private static String deviceCtrlResultNotifyUrl;
    private static String deviceReceiveDataNotifyUrl;
    private static String deviceTransDataNotifyUrl;

    public WebConfig() {
    }

    public static Integer getPort() {
        return deviceConnectPort;
    }

    public static String getUploadPath() {
        return imageFilePath;
    }

    public static String getLogin() {
        return notifyUrlBase + deviceLoginNotifyUrl;
    }

    public static String getRealTimeData() {
        return notifyUrlBase + deviceRealTimeDataNotifyUrl;
    }

    public static String getCtrl() {
        return notifyUrlBase + deviceCtrlResultNotifyUrl;
    }

    public static String getReceive() {
        return notifyUrlBase + deviceReceiveDataNotifyUrl;
    }

    public static String getTrans() {
        return notifyUrlBase + deviceTransDataNotifyUrl;
    }

    public static String getFileAddress() {
        return notifyUrlBase + imageUploadNotifyUrl;
    }

    public void setDeviceConnectPort(Integer deviceConnectPort) {
        WebConfig.deviceConnectPort = deviceConnectPort;
    }

    public void setImageFilePath(String imageFilePath) {
        WebConfig.imageFilePath = imageFilePath;
    }

    public void setNotifyUrlBase(String notifyUrlBase) {
        WebConfig.notifyUrlBase = notifyUrlBase;
    }

    public void setImageUploadNotifyUrl(String imageUploadNotifyUrl) {
        WebConfig.imageUploadNotifyUrl = imageUploadNotifyUrl;
    }

    public void setDeviceLoginNotifyUrl(String deviceLoginNotifyUrl) {
        WebConfig.deviceLoginNotifyUrl = deviceLoginNotifyUrl;
    }

    public void setDeviceRealTimeDataNotifyUrl(String deviceRealTimeDataNotifyUrl) {
        WebConfig.deviceRealTimeDataNotifyUrl = deviceRealTimeDataNotifyUrl;
    }

    public void setDeviceCtrlResultNotifyUrl(String deviceCtrlResultNotifyUrl) {
        WebConfig.deviceCtrlResultNotifyUrl = deviceCtrlResultNotifyUrl;
    }

    public void setDeviceReceiveDataNotifyUrl(String deviceReceiveDataNotifyUrl) {
        WebConfig.deviceReceiveDataNotifyUrl = deviceReceiveDataNotifyUrl;
    }

    public void setDeviceTransDataNotifyUrl(String deviceTransDataNotifyUrl) {
        WebConfig.deviceTransDataNotifyUrl = deviceTransDataNotifyUrl;
    }
}
