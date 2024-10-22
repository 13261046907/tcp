package com.rk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rk.domain.DeviceInstance;
import com.rk.mapper.DevDeviceInstanceMapper;
import com.rk.service.DeviceInstanceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("deviceInstanceService")
public class DeviceInstanceServiceImpl extends ServiceImpl<DevDeviceInstanceMapper, DeviceInstance> implements DeviceInstanceService {

    @Resource
    private DevDeviceInstanceMapper devDeviceInstanceMapper;
    @Override
    public String selectMataDataById(String deviceId) {
        return devDeviceInstanceMapper.selectMataDataById(deviceId);
    }

    public void updateDeriveMetadataValueById(String deriveMetadataValue,String deviceId) {
        devDeviceInstanceMapper.updateDeriveMetadataValueById(deriveMetadataValue,deviceId);
    }
}
