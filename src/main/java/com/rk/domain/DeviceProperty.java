package com.rk.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName("dev_device_property")
@Data
public class DeviceProperty implements Serializable {

    private String id;

    @TableField("device_id")
    private String deviceId;

    @TableField("property")
    private String property;

    @TableField("value")
    private String value;

    @TableField("unit")
    private String unit;

    @TableField("`timestamp`")
    private Date timestamp;
}
