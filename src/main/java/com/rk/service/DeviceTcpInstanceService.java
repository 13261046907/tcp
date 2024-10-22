package com.rk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rk.domain.DeviceInstancesTcpTemplateEntity;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author fc
 * @since 2023-04-30
 */
public interface DeviceTcpInstanceService extends IService<DeviceInstancesTcpTemplateEntity> {

    DeviceInstancesTcpTemplateEntity findTcpTemplateByDeviceId(String deviceId);

    DeviceInstancesTcpTemplateEntity findDeviceInstanceTcpTemplateByCrc(String hexCrx);

}
