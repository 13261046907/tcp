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
                DeviceModel deviceModel = deviceInstanceService.selectDeviceModelByChannelId(null, deviceModelVo.getModelId());
                if(!Objects.isNull(deviceModel)){
                    try {
                        serverHandler.channelWrite(deviceModel.getChannel(), deviceModelVo.getInstructionCrc());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    //设备下线
                    String deviceId = deviceModelVo.getDeviceId();
                    deviceInstanceService.updateDeviceStateByDeviceId(DeviceStateEnum.offline.getValue(),deviceId);
                }
            });
        }
    }
}
