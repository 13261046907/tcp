package com.rk.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.JDBCType;

@TableName("dev_device_instance")
@Data
public class DeviceInstance implements Serializable {

    private String id;

    @TableField("photo_url")
    private String photoUrl;

    private String name;

    @TableField("device_type")
    private String deviceType;

    @TableField("`describe`")
    private String describe;

    @TableField("product_id")
    private String productId;

    @TableField("product_name")
    private String productName;

    private String configuration;

    @TableField("derive_metadata")
    private String deriveMetadata;

    @TableField("derive_metadata_value")
    private String deriveMetadataValue;

    private String state;

    @TableField("creator_id")
    private String creatorId;

    @TableField("creator_name")
    private String creatorName;

    @TableField("create_time")
    private Long createTime;

    @TableField("registry_time")
    private Long registryTime;

    @TableField("org_id")
    private String orgId;

    @TableField("parent_id")
    private String parentId;

    @TableField("modifier_id")
    private String modifierId;

    @TableField("modifier_id")
    private Long modifyTime;

    @TableField("modifier_name")
    private String modifierName;

}
