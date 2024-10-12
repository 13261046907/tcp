package com.rk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rk.domain.DeviceInstance;
import com.rk.mapper.DevDeviceInstanceMapper;
import com.rk.service.DeviceInstanceService;
import org.springframework.stereotype.Service;

@Service("deviceInstanceService")
public class DeviceInstanceServiceImpl extends ServiceImpl<DevDeviceInstanceMapper, DeviceInstance> implements DeviceInstanceService {
}
