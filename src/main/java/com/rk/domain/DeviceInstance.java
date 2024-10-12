package com.rk.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@TableName("dev_device_instance")
@Data
public class DeviceInstance implements Serializable {

    private String id;

    private String photoUrl;

    private String name;

    private String deviceType;

    private String describe;

    private String productId;

    private String productName;

    private String configuration;

    private String deriveMetadata;

    private String state;

    private String creatorId;

    private String creatorName;

    private Long createTime;

    private Long registryTime;

    private String orgId;

    private String parentId;

    private String modifierId;

    private Long modifyTime;

    private String modifierName;

}
