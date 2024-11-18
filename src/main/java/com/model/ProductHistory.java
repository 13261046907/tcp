package com.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

@Data
public class ProductHistory  implements Serializable {
    @TableField("id")
    private Integer id;
    @TableField("product_id")
    private String productId;
    @TableField("acquisition_time")
    private String acquisitionTime;
    @TableField("data")
    private String data;
    @TableField("size")
    private Integer size;
}
