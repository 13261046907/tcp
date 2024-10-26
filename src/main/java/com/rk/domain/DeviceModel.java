package com.rk.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@TableName("dev_device_model")
@Data
public class DeviceModel implements Serializable {

    private String id;

    @TableField("model_id")
    private String modelId;

    @TableField("device_address")
    private String deviceAddress;

    @TableField("`channel`")
    private String channel;

    @TableField("`is_prefix`")
    private String isPrefix;
}
