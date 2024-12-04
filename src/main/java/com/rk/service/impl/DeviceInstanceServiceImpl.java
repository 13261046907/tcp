package com.rk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.model.DeviceInstanceEntity;
import com.model.ProductHistory;
import com.rk.domain.DeviceInstance;
import com.rk.domain.DeviceModel;
import com.rk.domain.DeviceProperty;
import com.rk.mapper.DevDeviceInstanceMapper;
import com.rk.service.DeviceInstanceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
    public String selectDeviceIdByAddress(String modelId,String deviceAddress) {
        return devDeviceInstanceMapper.selectDeviceIdByAddress(modelId,deviceAddress);
    }

    @Override
    public String selectProductIdByDeviceId(String deviceId) {
        return devDeviceInstanceMapper.selectProductIdByDeviceId(deviceId);
    }

    @Override
    public List<String> selectDeviceIdByModelId(String modelId) {
        return devDeviceInstanceMapper.selectDeviceIdByModelId(modelId);
    }

    @Override
    public List<DeviceModel> selectAllTcpTemp() {
        return devDeviceInstanceMapper.selectAllTcpTemp();
    }
    @Override
    public DeviceModel selectDeviceModelByChannelId(String channelId,String modelId) {
        return devDeviceInstanceMapper.selectDeviceModelByChannelId(channelId,modelId);
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
    public void deleteDeviceModelByChannelId(String channelId) {
        devDeviceInstanceMapper.deleteDeviceModelByChannelId(channelId);
    }

    @Override
    public void updateDeviceStateByDeviceId(String state,String deviceId) {
        devDeviceInstanceMapper.updateDeviceStateByDeviceId(state,deviceId);
    }

    @Override
    public void updateProductStateByProductId(String state,String productId) {
        devDeviceInstanceMapper.updateProductStateByProductId(state,productId);
    }

    @Override
    public DeviceInstanceEntity selectDevProductInfoById(String productId) {
        return devDeviceInstanceMapper.selectDevProductInfoById(productId);
    }

    @Override
    public void updateDeviceModelDate(DeviceModel queryDeviceModel) {
        devDeviceInstanceMapper.updateDeviceModelDate(queryDeviceModel);
    }

    @Override
    public List<DeviceInstanceEntity> selectDevDeviceByProductId(String productId) {
        return devDeviceInstanceMapper.selectDevDeviceByProductId(productId);
    }
    @Override
    public List<DeviceInstanceEntity> selectAllDevDeviceMetadata(){
        return devDeviceInstanceMapper.selectAllDevDeviceMetadata();
    }
    @Override
    public void insertDeviceInstance(DeviceInstanceEntity deviceInstanceEntity){
        devDeviceInstanceMapper.insertDeviceInstance(deviceInstanceEntity);
    }

    @Override
    public void insertDeviceTcpTemplate(DeviceInstanceEntity deviceInstanceEntity){
        devDeviceInstanceMapper.insertDeviceTcpTemplate(deviceInstanceEntity);
    }

    @Override
    public void insertProductHistory(ProductHistory productHistory){
        devDeviceInstanceMapper.insertProductHistory(productHistory);
    }

    @Override
    public void updateProductHistoryById(ProductHistory productHistory) {
        devDeviceInstanceMapper.updateProductHistoryById(productHistory);
    }

    @Override
    public ProductHistory selectHistoryByProductId(String productId, String startTime,String endTime) {
        return devDeviceInstanceMapper.selectHistoryByProductId(productId,startTime,endTime);
    }
}
