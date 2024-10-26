package com.rk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rk.domain.DeviceInstancesTcpTemplateEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author fc
 * @since 2023-04-30
 */
public interface DeviceTcpInstanceService extends IService<DeviceInstancesTcpTemplateEntity> {

    List<DeviceInstancesTcpTemplateEntity> findTcpTemplateByDeviceId(String deviceId, int paramNum);

    DeviceInstancesTcpTemplateEntity findDeviceInstanceTcpTemplateByCrc(String hexCrx);

}
