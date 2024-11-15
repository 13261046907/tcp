package com.tcp;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.config.RedisUtil;
import com.enums.DeviceStateEnum;
import com.model.DeviceInstanceEntity;
import com.rk.domain.DeviceModel;
import com.rk.service.DeviceInstanceService;
import com.rk.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
            String imei = "";
            DeviceModel queryDeviceModel = deviceInstanceService.selectDeviceModelByChannelId(channelId,null);
            if(Objects.isNull(queryDeviceModel)){
                DeviceModel deviceModel = Modbus.buildModel(hex);
                deviceModel.setModelId(hex);
                deviceModel.setChannel(channelId);
                deviceInstanceService.insertDeviceModel(deviceModel);
                imei = deviceModel.getImei();
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
                imei = queryDeviceModel.getImei();
                deviceInstanceService.updateDeviceModelDate(queryDeviceModel);
            }
            log.info("接收原始数据1:{}: " + hex);
            //根据imei查询4g下是否有设备,如果没有新增
            buildDeviceInstance(imei);
            try {
                if(!Objects.isNull(queryDeviceModel)){
                    String modelId = queryDeviceModel.getImei();
                    String deviceAddress = "";
                    int preModelLength = modelId.length();
                    if(hex.length() >= preModelLength ){
                        deviceAddress = hex.substring(0, 2);
                        String deviceId = deviceInstanceService.selectDeviceIdByAddress(modelId,deviceAddress);
                        System.out.println("perStr:"+modelId+";deviceAddress:"+deviceAddress+";deviceId::"+deviceId+";result="+hex);
                        hexBuild(deviceId,hex);
                        /*String result = hex.substring(preModelLength);
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
                        hexBuild(deviceId,result);*/
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
            //查询产品id,更新产品在线
            DeviceInstanceEntity deviceInstanceEntityQuyery = deviceInstanceService.selectDevProductInfoById(productId);
            String productName = "";
            String samplingFrequency = "";
            if(!Objects.isNull(deviceInstanceEntityQuyery)){
                productName = deviceInstanceEntityQuyery.getProductName();
                samplingFrequency = deviceInstanceEntityQuyery.getSamplingFrequency();
            }
            deviceInstanceService.updateProductStateByProductId(DeviceStateEnum.online.getName(),productId);
            List<DeviceInstanceEntity> deviceInstanceEntities = deviceInstanceService.selectDevDeviceByProductId(productId);
            if(CollectionUtil.isEmpty(deviceInstanceEntities)){
                //查询土壤和空气模块
                List<DeviceInstanceEntity> deviceInstanceEntitiesList = deviceInstanceService.selectAllDevDeviceMetadata();
                if(CollectionUtil.isNotEmpty(deviceInstanceEntitiesList)){
                    String finalProductName = productName;
                    String finalSamplingFrequency = samplingFrequency;
                    deviceInstanceEntitiesList.stream().forEach(deviceInstanceEntity -> {
                        String currentDate = new Date().getTime() + "";
                        deviceInstanceEntity.setId(currentDate);
                        deviceInstanceEntity.setProductId(productId);
                        deviceInstanceEntity.setProductName(finalProductName);
                        deviceInstanceEntity.setState("online");
                        //创建设备
                        log.info("insertDeviceInstance:{}", JSONObject.toJSONString(deviceInstanceEntity));
                        deviceInstanceService.insertDeviceInstance(deviceInstanceEntity);
                        if("03".equals(deviceInstanceEntity.getDeviceAddress())){
                            //土壤七合一两个指令
                            //创建氮磷钾模版
                            String deviceId = deviceInstanceEntity.getId();
                            deviceInstanceEntity.setDeviceId(deviceId);
                            deviceInstanceEntity.setDeviceType("1");
                            deviceInstanceEntity.setDeviceAddress("03");
                            deviceInstanceEntity.setFunctionCode("03");
                            deviceInstanceEntity.setRegisterAddress("001E");
                            deviceInstanceEntity.setDataLength("0003");
                            deviceInstanceEntity.setInstructionCrc("0303001E0003642f");
                            deviceInstanceEntity.setTitle("氮磷钾");
                            deviceInstanceEntity.setModelId(productId);
                            deviceInstanceEntity.setId(currentDate);
                            deviceInstanceEntity.setIsPrefix("1");
                            deviceInstanceEntity.setCreateTime(DateUtil.now());
                            if(StringUtils.isNotBlank(finalSamplingFrequency)){
                                String cron = updateFirstField(finalSamplingFrequency, "5");
                                deviceInstanceEntity.setSamplingFrequency(cron);
                            }
                            log.info("insertDeviceTcpTemplate:{}",JSONObject.toJSONString(deviceInstanceEntity));
                            deviceInstanceService.insertDeviceTcpTemplate(deviceInstanceEntity);
                            //创建土壤含水率      温度   电导率   ph模版
                            deviceInstanceEntity.setDeviceId(deviceId);
                            deviceInstanceEntity.setDeviceType("1");
                            deviceInstanceEntity.setDeviceAddress("03");
                            deviceInstanceEntity.setFunctionCode("03");
                            deviceInstanceEntity.setRegisterAddress("0000");
                            deviceInstanceEntity.setDataLength("0004");
                            deviceInstanceEntity.setInstructionCrc("03030000000445EB");
                            deviceInstanceEntity.setTitle("四合一");
                            deviceInstanceEntity.setModelId(productId);
                            deviceInstanceEntity.setId(new Date().getTime() + "");
                            deviceInstanceEntity.setIsPrefix("1");
                            deviceInstanceEntity.setCreateTime(DateUtil.now());
                            if(StringUtils.isNotBlank(finalSamplingFrequency)){
                                String cron = updateFirstField(finalSamplingFrequency, "10");
                                deviceInstanceEntity.setSamplingFrequency(cron);
                            }
                            log.info("insertDeviceTcpTemplate:{}",JSONObject.toJSONString(deviceInstanceEntity));
                            deviceInstanceService.insertDeviceTcpTemplate(deviceInstanceEntity);
                        }else if("01".equals(deviceInstanceEntity.getDeviceAddress())){
                            //创建模版
                            String deviceId = deviceInstanceEntity.getId();
                            deviceInstanceEntity.setDeviceId(deviceId);
                            deviceInstanceEntity.setDeviceType("1");
                            deviceInstanceEntity.setModelId(productId);
                            deviceInstanceEntity.setId(currentDate);
                            deviceInstanceEntity.setIsPrefix("1");
                            deviceInstanceEntity.setTitle(deviceInstanceEntity.getName());
                            deviceInstanceEntity.setCreateTime(DateUtil.now());
                            if(StringUtils.isNotBlank(finalSamplingFrequency)){
                                deviceInstanceEntity.setSamplingFrequency(finalSamplingFrequency);
                            }
                            log.info("insertDeviceTcpTemplate:{}",JSONObject.toJSONString(deviceInstanceEntity));
                            deviceInstanceService.insertDeviceTcpTemplate(deviceInstanceEntity);
                        }
                    });
                }
            }
        }catch (Exception  e){
            e.printStackTrace();
        }
    }

    private static String updateFirstField(String cronExpression,String num) {
        // 使用空格分割 Cron 表达式
        String[] fields = cronExpression.split(" ");

        // 检查首位是否为 "0"
        if (fields[0].trim().equals("0")) {
            // 如果是0，改为5
            fields[0] = num;
        }
        // 重新将字段合并为 Cron 表达式
        String updatedCronExpression = String.join(" ", fields);
        // 输出更新后的 Cron 表达式
        System.out.println("更新后的 Cron 表达式: " + updatedCronExpression);

        return updatedCronExpression;
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
