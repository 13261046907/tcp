package com.tcp;

import com.config.RedisUtil;
import com.rk.domain.DeviceModel;
import com.rk.service.DeviceInstanceService;
import com.rk.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

@Controller
public class TcpController {

    @Autowired
    private NettyTcpServerHandler serverHandler;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private DeviceInstanceService deviceInstanceService;

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

    @RequestMapping(
            value = {"/buildAcceptMsg"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public R buildAcceptMsg(String channel,String hex) {
        hex = "38363331323130373739313431393003030800BD00C5016E003ADF47";
        String modelId = "";
        String sendHex = "";
        String deviceAddress = "";
        String result = "";
        if(hex.length() > 32) {
            modelId = hex.substring(0, 30);
            result = hex.substring(modelId.length());
            deviceAddress = hex.substring(30, 32);
            DeviceModel queryDeviceModel = deviceInstanceService.selectChannelByDeviceId(modelId, null);
            System.out.println("perStr:"+modelId+";deviceAddress:"+deviceAddress+";result="+result);
            if(!Objects.isNull(queryDeviceModel)){
                queryDeviceModel.setChannel(channel);
                deviceInstanceService.updateDeviceModelByDeviceId(channel,modelId,deviceAddress);
            }else {
                DeviceModel deviceModel = new DeviceModel();
                deviceModel.setModelId(modelId);
                deviceModel.setChannel(channel);
                deviceModel.setDeviceAddress(deviceAddress);
                deviceInstanceService.insertDeviceModel(deviceModel);
            }
        }
        serverHandler.hexBuild(null,result);
      /*  if(hex.length() > 48){
            modelId = hex.substring(0, 30);
            sendHex = hex.substring(30, 46);
            int index = hex.indexOf(modelId);
            result = hex.substring(index + modelId.length());
            deviceAddress = hex.substring(46, 48);
            System.out.println("perStr:"+modelId+"deviceAddress:"+deviceAddress+";sendHex:"+sendHex+";result="+result);
        }else if(hex.length() > 32){
            //不带发送指令
            modelId = hex.substring(0, 30);
            result = hex.substring(modelId.length());
            deviceAddress = hex.substring(30, 32);
            System.out.println("perStr:"+modelId+"deviceAddress:"+deviceAddress+";result="+result);
        }*/
       /* if(StringUtils.isNotBlank(modelId) && StringUtils.isNotBlank(deviceAddress)){
            //根据4g模块和设备地址查询通道信息
            DeviceModel queryDeviceModel = deviceInstanceService.selectChannelByDeviceId(modelId, deviceAddress);
            if(!Objects.isNull(queryDeviceModel)){
                queryDeviceModel.setChannel(channel);
                deviceInstanceService.updateDeviceModelByDeviceId(channel,modelId,deviceAddress);
            }else {
                DeviceModel deviceModel = new DeviceModel();
                deviceModel.setModelId(modelId);
                deviceModel.setChannel(channel);
                deviceModel.setDeviceAddress(deviceAddress);
                deviceInstanceService.insertDeviceModel(deviceModel);
            }
            //根据sendHex查询数据库是否存在
            String deviceId = deviceInstanceService.selectTcpTempBySendHex(sendHex);
            if(StringUtils.isNotBlank(deviceId)){
                serverHandler.hexBuild(deviceId,result);
            }
        }*/
        return R.ok();
    }
}
