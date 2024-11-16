package com.task;

import cn.hutool.core.collection.CollectionUtil;
import com.enums.DeviceStateEnum;
import com.rk.domain.DeviceModel;
import com.rk.service.DeviceInstanceService;
import com.tcp.NettyTcpServerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Classname HypeTaskService
 * @Description TODO
 * @Date 2022/4/13 4:33 下午
 * @Created by yangtao
 */
@Component
@Slf4j
public class StateTaskService {
    @Autowired
    private DeviceInstanceService deviceInstanceService;
    @Autowired
    private NettyTcpServerHandler serverHandler;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void chatGPTTask1(){
        log.info("5分钟任务更新完工数据:{}",new Date());
        List<DeviceModel> deviceModelVos = deviceInstanceService.selectAllTcpTemp();
        if(CollectionUtil.isNotEmpty(deviceModelVos)){
            deviceModelVos.stream().forEach(deviceModelVo -> {
                String deviceId = deviceModelVo.getDeviceId();
                String modelId = deviceModelVo.getModelId();
                DeviceModel deviceModel = deviceInstanceService.selectDeviceModelByChannelId(null, modelId);
                if(!Objects.isNull(deviceModel)){
                    String imei = deviceModel.getImei();
                    try {
                        log.info("IMEI:{},设备ID:deviceId:{},channel:{},发送指令:{}",modelId,deviceId,deviceModel.getChannel(),deviceModelVo.getInstructionCrc());
                        //判断当前4g模块心跳时间大于2分钟说明掉线
                        Date createTime = deviceModel.getCreateTime();
                        // 将 createTime 转换为 Instant
                        Instant createTimeInstant = createTime.toInstant();
                        Instant currentTime = Instant.now();
                        // 计算时间差
                        Duration duration = Duration.between(createTimeInstant, currentTime);
                        // 判断差值是否大于两分钟
                        if (duration.toMinutes() > 5) {
                            log.info("IMEI:{},4G掉线,删除数据",imei);
                            deviceInstanceService.deleteDeviceModelByChannelId(deviceModel.getChannel());
                            //修改设备和产品下线
                            List<String> deviceIdList = deviceInstanceService.selectDeviceIdByModelId(deviceModel.getModelId());
                            if(!CollectionUtils.isEmpty(deviceIdList)){
                                deviceIdList.stream().forEach(queryDeviceId ->{
                                    log.info("设备ID:deviceId:{},设备掉线",queryDeviceId);
                                    deviceInstanceService.updateDeviceStateByDeviceId(DeviceStateEnum.offline.getValue(),queryDeviceId);
                                    String productId = deviceInstanceService.selectProductIdByDeviceId(queryDeviceId);
                                    log.info("产品ID:deviceId:{},设备掉线",productId);
                                    deviceInstanceService.updateProductStateByProductId(DeviceStateEnum.offline.getName(),productId);
                                });
                            }
                        } else {
                            log.info("4G在线:{}",imei);
                        }
//                        serverHandler.syncChannelWrite(deviceModel.getChannel(), deviceModelVo.getInstructionCrc());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    //设备下线
                    log.info("deviceId:{},下线",deviceId);
                    deviceInstanceService.updateDeviceStateByDeviceId(DeviceStateEnum.offline.getValue(),deviceId);
                    log.info("产品ID:IMEI:{},下线",modelId);
                    deviceInstanceService.updateProductStateByProductId(DeviceStateEnum.offline.getName(),modelId);
                }
            });
        }
    }
}
