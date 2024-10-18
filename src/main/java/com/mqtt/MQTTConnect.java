package com.mqtt;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

/**
 * MQTT工具类操作
 */
@Slf4j
@Component
@Data
public class MQTTConnect {

  private String HOST = "tcp://101.201.119.26:11883";
  private  String clientId = "jhyzhihuinongyetcp2024101010";
  private  String topic = "";
  private MqttClient mqttClient;
  private final InitCallback initCallback;


  public MQTTConnect(InitCallback initCallback) {
    this.initCallback = initCallback;
  }

  /**
   * 客户端connect连接mqtt服务器
   *
   * @param username 用户名
   * @param password 密码
   * @param mqttCallback 回调函数
   **/
  public void setMqttClient(String username, String password, MqttCallback mqttCallback)
          throws MqttException {
    MqttConnectOptions options = mqttConnectOptions(username, password);
    if (mqttCallback == null) {
      mqttClient.setCallback(mqttCallback);
    } else {
    }
    mqttClient.setCallback(mqttCallback);
    mqttClient.connect(options);
  }

  /**
   * MQTT连接参数设置
   */
  private MqttConnectOptions mqttConnectOptions(String userName, String passWord)
          throws MqttException {
    mqttClient = new MqttClient(HOST, clientId, new MemoryPersistence());
    MqttConnectOptions options = new MqttConnectOptions();
    options.setUserName(userName);
    options.setPassword(passWord.toCharArray());
    options.setConnectionTimeout(30);///默认：30
    options.setAutomaticReconnect(true);//默认：false
    options.setCleanSession(true);//默认：true
    options.setKeepAliveInterval(60);//默认：60
    return options;
  }

  /**
   * 关闭MQTT连接
   */
  public void close() throws MqttException {
    mqttClient.close();
    mqttClient.disconnect();
  }

  /**
   * 向某个主题发布消息 默认qos：1
   */
  public void pub(String msg) throws MqttException {
    MqttMessage mqttMessage = new MqttMessage();
    mqttMessage.setPayload(msg.getBytes(Charset.forName("GBK")));
    MqttTopic mqttTopic = mqttClient.getTopic(topic);
    MqttDeliveryToken token = mqttTopic.publish(mqttMessage);
    token.waitForCompletion();
  }
  public void pub(String topic, String msg) throws MqttException {
    MqttMessage mqttMessage = new MqttMessage();
    mqttMessage.setPayload(msg.getBytes(Charset.forName("GBK")));
    if(mqttClient == null){
      setMqttClient(MqttConstant.MQTT_USERNAME, MqttConstant.MQTT_PASSWORD, initCallback);
    }
    MqttTopic mqttTopic = mqttClient.getTopic(topic);
    MqttDeliveryToken token = mqttTopic.publish(mqttMessage);
    token.waitForCompletion();
  }
  public void pub(String topic,MqttMessage mqttMessage) throws MqttException {
    if(mqttClient == null){
      setMqttClient(MqttConstant.MQTT_USERNAME, MqttConstant.MQTT_PASSWORD, initCallback);
    }
    MqttTopic mqttTopic = mqttClient.getTopic(topic);
    MqttDeliveryToken token = mqttTopic.publish(mqttMessage);
    token.waitForCompletion();
  }
  public void pub(String topic, String msg, int qos) throws MqttException {
    MqttMessage mqttMessage = new MqttMessage();
    mqttMessage.setQos(qos);
    mqttMessage.setPayload(msg.getBytes(Charset.forName("GBK")));
    MqttTopic mqttTopic = mqttClient.getTopic(topic);
    MqttDeliveryToken token = mqttTopic.publish(mqttMessage);
    token.waitForCompletion();
  }

  /**
   * 订阅某一个主题 ，此方法默认的的Qos等级为：1
   *
   * @param topic 主题
   */
  public void sub(String topic) throws MqttException {
    mqttClient.subscribe(topic);
  }

  /**
   * 订阅某一个主题，可携带Qos
   *
   * @param topic 所要订阅的主题
   * @param qos 消息质量：0、1、2
   */
  public void sub(String topic, int qos) throws MqttException {
    mqttClient.subscribe(topic, qos);
  }

}
