package com.rk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rk.domain.DeviceInstance;
import com.rk.domain.DeviceModel;

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

    String selectTcpTempBySendHex(String sendHex);

    String selectDeviceIdByAddress(String deviceAddress);

    DeviceModel selectChannelByDeviceId(String modelId,String deviceAddress);

    void insertDeviceModel(DeviceModel deviceModel);

    void updateDeriveMetadataValueById(String deriveMetadataValue,String deviceId);
}
