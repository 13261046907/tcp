package com.rk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.model.DeviceInstanceEntity;
import com.rk.domain.DeviceInstance;
import com.rk.domain.DeviceModel;
import com.rk.domain.DeviceProperty;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author fc
 * @since 2023-04-30
 */
public interface DeviceInstanceService extends IService<DeviceInstance> {

    String selectMataDataById(String deviceId);

    String selectMataDataValueById(String deviceId);

    String selectTcpTempBySendHex(String sendHex);

    String selectDeviceIdByAddress(String modelId,String deviceAddress);

    String selectProductIdByDeviceId(String deviceId);

    List<String> selectDeviceIdByModelId(String modelId);

    List<DeviceModel> selectAllTcpTemp();

    DeviceModel selectDeviceModelByChannelId(String channelId,String modelId);

    void insertDeviceModel(DeviceModel deviceModel);

    void insertDeviceProperty(DeviceProperty deviceProperty);

    void updateDeriveMetadataValueById(String deriveMetadataValue,String deviceId);

    void deleteDeviceModelByChannelId(String channelId);

    void updateDeviceStateByDeviceId(String state,String deviceId);

    void updateProductStateByProductId(String state,String productId);

    void updateDeviceModelDate(DeviceModel queryDeviceModel);

    List<DeviceInstanceEntity> selectDevDeviceByProductId(String productId);

    List<DeviceInstanceEntity> selectAllDevDeviceMetadata();

    void insertDeviceInstance(DeviceInstanceEntity deviceInstanceEntity);

    void insertDeviceTcpTemplate(DeviceInstanceEntity deviceInstanceEntity);
}
