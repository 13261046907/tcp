package com.rk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.model.DeviceInstanceEntity;
import com.rk.domain.DeviceInstance;
import com.rk.domain.DeviceModel;
import com.rk.domain.DeviceProperty;

import java.util.Date;
import java.util.List;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fc
 * @since 2023-04-30
 */
public interface DevDeviceInstanceMapper extends BaseMapper<DeviceInstance> {

    String selectMataDataById(String deviceId);

    String selectMataDataValueById(String deviceId);

    String selectTcpTempBySendHex(String sendHex);

    String selectDeviceIdByAddress(String modelId,String deviceAddress);

    String selectProductIdByDeviceId(String deviceId);

    List<String> selectDeviceIdByModelId(String modelId);

    List<DeviceModel> selectAllTcpTemp();

    String selectTcpTempByDeviceAddress(String deviceAddress);

    DeviceModel selectDeviceModelByChannelId(String channelId,String modelId);

    void updateDeriveMetadataValueById(String deriveMetadataValue,String deviceId);

    void  updateDeviceModelByDeviceId(String channel,String modelId,String deviceAddress);

    void insertDeviceModel(DeviceModel deviceModel);

    void insertDeviceProperty(DeviceProperty deviceProperty);

    void deleteDeviceModelByChannelId(String channelId);

    void updateDeviceStateByDeviceId(String state,String deviceId);

    void updateProductStateByProductId(String state,String productId);

    void updateDeviceModelDate(DeviceModel queryDeviceModel);

    List<DeviceInstanceEntity> selectDevDeviceByProductId(String productId);

    List<DeviceInstanceEntity> selectAllDevDeviceMetadata();

    void insertDeviceInstance(DeviceInstanceEntity deviceInstanceEntity);

    void insertDeviceTcpTemplate(DeviceInstanceEntity deviceInstanceEntity);
}
