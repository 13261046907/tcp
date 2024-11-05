package com.tcp;

import com.config.RedisUtil;
import com.rk.domain.DeviceModel;
import com.rk.service.DeviceInstanceService;
import com.rk.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

@Slf4j
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
        log.info("code:{},mgs:{}",code,msg);
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
        log.info("【" + channel + "】" + " :" + hex);
        //根据chanelID查询4g模块关系
        DeviceModel queryDeviceModel = deviceInstanceService.selectDeviceModelByChannelId(channel,null);
        if(Objects.isNull(queryDeviceModel)){
            DeviceModel deviceModel = new DeviceModel();
            deviceModel.setModelId(hex);
            deviceModel.setChannel(channel);
            deviceInstanceService.insertDeviceModel(deviceModel);
        }
        log.info("接收原始数据1:{}: " + hex);
        if(!Objects.isNull(queryDeviceModel)){
            String modelId = queryDeviceModel.getModelId();
            String deviceAddress = "";
            int preModelLength = modelId.length();
            if(hex.length() >= preModelLength ){
                String result = hex.substring(preModelLength);
                int endCRC = preModelLength + 16;
                String sendHex = hex.substring(preModelLength, endCRC);
                String deviceId = deviceInstanceService.selectTcpTempBySendHex(sendHex);
                if(StringUtils.isBlank(deviceId)){
                    //不带发送指令
                    deviceAddress = hex.substring(preModelLength, preModelLength+2);
                    deviceId = deviceInstanceService.selectDeviceIdByAddress(modelId,deviceAddress);
                }else {
                    deviceAddress = hex.substring(endCRC, endCRC+2);
                }
                System.out.println("perStr:"+modelId+";deviceAddress:"+deviceAddress+";deviceId::"+deviceId+";result="+result);
                hexBuild(deviceId,result);
            }
        }
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

    public static void main(String[] args) {
        String input = "313431303334323238343334300D01030000000AC5CD01031400D101B2000003F701FA000000000000000000338A34";

        // 去掉前30个字节
        String afterRemovingBytes = input.substring(30);

        // 获取前16个字节
        String result = afterRemovingBytes.substring(0, 16);  // 从去掉前30字节后的字符串中截取前16字节

        // 打印结果
        System.out.println("截取的字符串: " + result);
    }
}
