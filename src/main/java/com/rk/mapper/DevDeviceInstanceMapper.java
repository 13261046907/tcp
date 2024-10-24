package com.rk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rk.domain.DeviceInstance;
import com.rk.domain.DeviceModel;


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

    String selectTcpTempBySendHex(String sendHex);

    String selectDeviceIdByAddress(String deviceAddress);

    DeviceModel selectChannelByDeviceId(String modelId,String deviceAddress);

    void updateDeriveMetadataValueById(String deriveMetadataValue,String deviceId);

    void insertDeviceModel(DeviceModel deviceModel);

}
