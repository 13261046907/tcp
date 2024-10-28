package com.rk.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DeriveMetadataValueVo implements Serializable {

    private String id;

    private String name;

    private String type;

    private String value;

    private Date updateTime;
}
