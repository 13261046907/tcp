package com.rk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rk.domain.DeviceInstance;
import com.rk.domain.DeviceModel;
import com.rk.domain.DeviceProperty;
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

    @Override
    public String selectMataDataValueById(String deviceId) {
        return devDeviceInstanceMapper.selectMataDataValueById(deviceId);
    }

    @Override
    public String selectTcpTempBySendHex(String sendHex) {
        return devDeviceInstanceMapper.selectTcpTempBySendHex(sendHex);
    }

    @Override
    public String selectDeviceIdByAddress(String deviceAddress) {
        return devDeviceInstanceMapper.selectDeviceIdByAddress(deviceAddress);
    }

    @Override
    public String selectTcpTempByDeviceAddress(String deviceAddress) {
        return devDeviceInstanceMapper.selectTcpTempByDeviceAddress(deviceAddress);
    }
    @Override
    public DeviceModel selectChannelByDeviceId(String modelId,String deviceAddress) {
        return devDeviceInstanceMapper.selectChannelByDeviceId(modelId,deviceAddress);
    }

    @Override
    public void insertDeviceModel(DeviceModel deviceModel){
        devDeviceInstanceMapper.insertDeviceModel(deviceModel);
    }

    @Override
    public void insertDeviceProperty(DeviceProperty deviceProperty){
        devDeviceInstanceMapper.insertDeviceProperty(deviceProperty);
    }

    @Override
    public void updateDeriveMetadataValueById(String deriveMetadataValue,String deviceId) {
        devDeviceInstanceMapper.updateDeriveMetadataValueById(deriveMetadataValue,deviceId);
    }

    @Override
    public void updateDeviceModelByDeviceId(String channel,String modelId,String deviceAddress) {
        devDeviceInstanceMapper.updateDeviceModelByDeviceId(channel,modelId,deviceAddress);
    }

}
