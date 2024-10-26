package com.rk.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author fc
 * @since 2023-04-30
 */
@Data
@TableName("dev_device_instance_tcp_template")
public class DeviceInstancesTcpTemplateEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 设备类型
     */
    @TableField( "device_type")
    private Integer deviceType;

    @TableField( "device_id")
    private String deviceId;

    /**
     * 设备地址
     */
    @TableField( "device_address")
    private String deviceAddress;

    /**
     * 功能码
     */
    @TableField( "function_code")
    private String functionCode;

    /**
     * 波特率
     */
    @TableField( "baud_rate")
    private String baudRate;

    /**
     * 寄存器起始地址
     */
    @TableField( "register_address")
    private String registerAddress;

    /**
     * 数据长度
     */
    @TableField( "data_length")
    private String dataLength;

    @TableField( "instruction_crc")
    private String instructionCrc;

    /**
     * 是否有发送指令前缀
     */
    @TableField( "is_prefix")
    private String isPrefix;

    /**
     * 开指令
     */
    private String openInstruction;

    @TableField( "open_instruction_crc")
    private String openInstructionCrc;

    /**
     * 关指令
     */
    private String closeInstruction;

    @TableField( "close_instruction_crc")
    private String closeInstructionCrc;

    private String status;

    /**
     * 采集公式
     */
    @TableField( "collect_formula")
    private String collectFormula;

    /**
     * 控制公式
     */
    @TableField( "control_formula")
    private String controlFormula;

    /**
     * 采集频率
     */
    @TableField( "sampling_frequency")
    private String samplingFrequency;

    /**
     * 标题
     */
    @TableField( "title")
    private String title;

    @TableField( "num")
    private String num;

    @TableField( "create_time")
    private String createTime;

    @TableField( "create_userId")
    private String createUserId;


}
