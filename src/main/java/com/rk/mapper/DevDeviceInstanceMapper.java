package com.rk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rk.domain.DeviceInstance;
import com.rk.domain.DeviceModel;
import com.rk.domain.DeviceProperty;


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

    String selectDeviceIdByAddress(String deviceAddress);

    String selectTcpTempByDeviceAddress(String deviceAddress);

    DeviceModel selectChannelByDeviceId(String modelId,String deviceAddress);

    void updateDeriveMetadataValueById(String deriveMetadataValue,String deviceId);

    void  updateDeviceModelByDeviceId(String channel,String modelId,String deviceAddress);

    void insertDeviceModel(DeviceModel deviceModel);

    void insertDeviceProperty(DeviceProperty deviceProperty);

}
