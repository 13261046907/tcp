package com.rk.domain;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceDO {
    private static final Logger logger = LoggerFactory.getLogger(DeviceDO.class);
    private String deviceAddr;
    private ChannelHandlerContext ctx;
    private Long realTimeLastTime = System.currentTimeMillis();

    public DeviceDO() {
    }

    public boolean sendData(Map<String, String> params, String cn) {
        try {
            if (this.ctx == null) {
                logger.error("设备离线：" + params);
                return false;
            } else {
                DataPackage dataPackageDO = new DataPackage();
                UUID uuid = UUID.randomUUID();
                dataPackageDO.setQN(uuid.toString().toLowerCase().replace("-", ""));
                dataPackageDO.setMN(this.deviceAddr);
                dataPackageDO.setAC("Q");
                dataPackageDO.setCN(cn);
                dataPackageDO.setData(params);
                String data = dataPackageDO.toStr();
                ByteBuf bufAck = this.ctx.alloc().buffer();
                bufAck.writeBytes(data.getBytes(StandardCharsets.UTF_8));
                this.ctx.writeAndFlush(bufAck);
                logger.error("指令下发成功：" + data);
                return true;
            }
        } catch (Exception var7) {
            logger.error("指令下发失败：" + params);
            return false;
        }
    }

    public String getDeviceAddr() {
        return this.deviceAddr;
    }

    public void setDeviceAddr(String deviceAddr) {
        this.deviceAddr = deviceAddr;
    }

    public ChannelHandlerContext getCtx() {
        return this.ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.realTimeLastTime = System.currentTimeMillis();
        this.ctx = ctx;
    }

    public Long getRealTimeLastTime() {
        return this.realTimeLastTime;
    }

    public void setRealTimeLastTime(Long realTimeLastTime) {
        this.realTimeLastTime = realTimeLastTime;
    }
}
