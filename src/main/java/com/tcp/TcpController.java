package com.tcp;

import cn.hutool.core.collection.CollectionUtil;
import com.config.RedisUtil;
import com.model.DeviceInstanceEntity;
import com.rk.domain.DeviceModel;
import com.rk.service.DeviceInstanceService;
import com.rk.utils.R;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
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
    public R buildAcceptMsg(String channelId,String hex) {
        try {
            log.info("加载客户端报文......");
            log.info("【" + channelId + "】" + " :" + hex);
            //根据chanelID查询4g模块关系
            DeviceModel queryDeviceModel = deviceInstanceService.selectDeviceModelByChannelId(channelId,null);
            if(Objects.isNull(queryDeviceModel)){
                DeviceModel deviceModel = Modbus.buildModel(hex);
                deviceModel.setModelId(hex);
                deviceModel.setChannel(channelId);
                deviceInstanceService.insertDeviceModel(deviceModel);
            }else {
                if(hex.length() == 46){
                    String coordinates = Modbus.buildHexString(hex);
                    // 按下划线分割
                    String[] parts = coordinates.split("_");
                    // 确保分割后有两个部分
                    if (parts.length == 2) {
                        String longitude = parts[0]; // 第一个部分是精度
                        String latitude = parts[1];  // 第二个部分是纬度
                        queryDeviceModel.setLongitude(longitude);
                        queryDeviceModel.setLatitude(latitude);
                        // 输出结果
                        log.info("精度 (Longitude):{}",longitude);
                        log.info("维度 (Latitude):{}", latitude);
                    } else {
                        log.error("非经纬度格式不正确");
                    }
                }
                queryDeviceModel.setCreateTime(new Date());
                deviceInstanceService.updateDeviceModelDate(queryDeviceModel);
            }
            log.info("接收原始数据1:{}: " + hex);
            //根据imei查询4g下是否有设备,如果没有新增
            buildDeviceInstance(queryDeviceModel.getImei());
            try {
                if(!Objects.isNull(queryDeviceModel)){
                    String modelId = queryDeviceModel.getImei();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return R.ok();
    }

    @Transactional
    public void buildDeviceInstance(String productId) {
        try {
            List<DeviceInstanceEntity> deviceInstanceEntities = deviceInstanceService.selectDevDeviceByProductId(productId);
            if(CollectionUtil.isEmpty(deviceInstanceEntities)){
                //查询土壤和空气模块
                List<DeviceInstanceEntity> deviceInstanceEntitiesList = deviceInstanceService.selectAllDevDeviceMetadata();
                if(CollectionUtil.isNotEmpty(deviceInstanceEntitiesList)){
                    deviceInstanceEntitiesList.stream().forEach(deviceInstanceEntity -> {
                        String currentDate = new Date().getTime() + "";
                        deviceInstanceEntity.setId(currentDate);
                        deviceInstanceEntity.setProductId(productId);
                        //创建设备
                        deviceInstanceService.insertDeviceInstance(deviceInstanceEntity);
                        //创建模版
                        String deviceId = deviceInstanceEntity.getId();
                        deviceInstanceEntity.setDeviceId(deviceId);
                        deviceInstanceEntity.setDeviceType("1");
                        deviceInstanceEntity.setModelId(productId);
                        deviceInstanceEntity.setId(currentDate);
                        deviceInstanceEntity.setIsPrefix("1");
                        deviceInstanceEntity.setCreateTime(new Date().toString());
                        deviceInstanceService.insertDeviceTcpTemplate(deviceInstanceEntity);
                    });
                }
            }
        }catch (Exception  e){
            e.printStackTrace();
        }
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
