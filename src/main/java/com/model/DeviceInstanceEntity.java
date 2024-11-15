package com.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

@Data
public class DeviceInstanceEntity implements Serializable {
    private String id;
    private String productId;
    private String productName;
    private String samplingFrequency;
    @TableField("device_id")
    private String deviceId;
    @TableField("photo_url")
    private String photoUrl;
    @TableField("name")
    private String name;
    private String title;
    @TableField("device_type")
    private String deviceType;
    @TableField("derive_metadata")
    private String deriveMetadata;
    @TableField("register_address")
    private String registerAddress;
    @TableField("function_code")
    private String functionCode;
    @TableField("device_address")
    private String deviceAddress;
    @TableField("data_length")
    private String dataLength;
    @TableField("instruction_crc")
    private String instructionCrc;
    @TableField("is_prefix")
    private String isPrefix;
    @TableField("model_id")
    private String modelId;
    @TableField("state")
    private String state;
    @TableField("create_time")
    private String createTime;


}
