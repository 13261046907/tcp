package com.tcp;

import com.config.RedisUtil;
import com.rk.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TcpController {

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
        return R.ok(deviceid);
    }
    @RequestMapping(
            value = {"/sendCodeByDevice"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public R sendCodeByDevice(String code ,String deviceId) {
        try {
            redisUtil.set(code,deviceId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.ok(deviceId);
    }

    @RequestMapping(
            value = {"/hexBuild"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public R hexBuild(String deviceId ,String convertedHexString) {
        try {
            serverHandler.hexBuild(deviceId,convertedHexString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.ok(deviceId);
    }
}
