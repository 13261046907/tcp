package com.rk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rk.domain.DeviceInstancesTcpTemplateEntity;
import com.rk.mapper.DevDeviceTcpInstanceMapper;
import com.rk.service.DeviceTcpInstanceService;
import org.springframework.stereotype.Service;

@Service("deviceTcpInstanceService")
public class DeviceTcpInstanceServiceImpl extends ServiceImpl<DevDeviceTcpInstanceMapper, DeviceInstancesTcpTemplateEntity> implements DeviceTcpInstanceService {
    @Override
    public DeviceInstancesTcpTemplateEntity findTcpTemplateByDeviceId(String deviceId,int paramNum) {
        LambdaQueryWrapper<DeviceInstancesTcpTemplateEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(DeviceInstancesTcpTemplateEntity::getDeviceId,deviceId);
        objectLambdaQueryWrapper.eq(DeviceInstancesTcpTemplateEntity::getNum,paramNum);
        return this.getOne(objectLambdaQueryWrapper);
    }

    @Override
    public DeviceInstancesTcpTemplateEntity findDeviceInstanceTcpTemplateByCrc(String hexCrx) {
        LambdaQueryWrapper<DeviceInstancesTcpTemplateEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(DeviceInstancesTcpTemplateEntity::getInstructionCrc,hexCrx);
        return this.getOne(objectLambdaQueryWrapper);
    }
}
