package com.rk.utils;

import com.rk.domain.DeviceDO;
import io.netty.channel.ChannelHandlerContext;
import java.util.HashMap;
import java.util.Map;

public class CacheManager {
    private static CacheManager instance;
    public final String[] paramsData = new String[]{"wormFlap", "automaticDryingTemperatureControl", "afterRainDelayTime", "dryingTemperature", "workingMode", "trapLamp", "dryingTime", "insecticidalTemperature", "trapLampTime", "insectRainBaffle", "runHour", "rainDelay", "dataInterval", "openRainSnow", "dryingTaffle", "moveWormGear", "stopRainDelay", "beginTime", "dryingTemperatureDifference", "insecticidalControl", "illuminationLowerLimit", "insecticidalTemperatureDifference", "url", "photograph", "dryingControl", "port", "photoPort", "openLight", "vibrationDevice", "leadWormTime", "illuminanceLag", "openingTimeCondition", "automaticControlInsecticidalTemperature", "fillLight"};
    private Map<String, DeviceDO> mapCache = new HashMap();

    private CacheManager() {
    }

    public static CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }

        return instance;
    }

    public DeviceDO getDeviceDO(String devAddr) {
        return (DeviceDO)this.mapCache.get(devAddr);
    }

    public void updateDevice(String devAddr, ChannelHandlerContext ctx) {
        if (this.mapCache.containsKey(devAddr)) {
            ((DeviceDO)this.mapCache.get(devAddr)).setCtx(ctx);
        } else {
            DeviceDO deviceDO = new DeviceDO();
            deviceDO.setCtx(ctx);
            deviceDO.setDeviceAddr(devAddr);
            this.mapCache.put(devAddr, deviceDO);
        }

    }
}
