package com.tcp;

import com.mqtt.MQTTConnect;
import com.rk.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TcpController {
        @Autowired
        private NettyTcpServerHandler serverHandler;
        @Autowired
        private  MQTTConnect mqttConnect;

        @GetMapping("/send/{code}/{msg}")
        public R send(@PathVariable(value = "code") String code , @PathVariable(value = "msg") String msg) {
                try {
                        serverHandler.send(code,msg);
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return R.ok();
        }

        @GetMapping("/sendMqtt/{code}/{msg}")
        public R sendMqtt(@PathVariable(value = "code") String code , @PathVariable(value = "msg") String msg) {
                try {
                        mqttConnect.pub(code,msg);
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return R.ok();
        }
}
