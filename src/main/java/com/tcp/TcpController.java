package com.tcp;

import com.config.RedisUtil;
import com.mqtt.MQTTConnect;
import com.rk.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TcpController {
    @Autowired
    private  MQTTConnect mqttConnect;
    @Autowired
    private NettyTcpServerHandler serverHandler;
    @Autowired
    private RedisUtil redisUtil;

    @RequestMapping(
            value = {"/send"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public R send(String code , String msg) {
        String deviceid = "";
        try {
            serverHandler.channelWrite(code,msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  R.ok(deviceid);
    }

    @RequestMapping(
            value = {"/sendMqtt"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public void sendMqtt(@PathVariable(value = "code") String code , @PathVariable(value = "msg") String msg) {
        try {
            mqttConnect.pub(code,msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
