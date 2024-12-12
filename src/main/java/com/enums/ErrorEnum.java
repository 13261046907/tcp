package com.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorEnum  implements ResponseCode{
    /**
     * 额度限制
     */
    QUOTA_LIMIT(402, "error.quota_limit","额度限制"),
    QUOTA_LIMIT_TOTAL_TOKENS(402, "error.quota_limit_total_tokens","总tokens已到上限"),
    QUOTA_LIMIT_TOTAL_MESSAGES(402, "error.quota_limit_total_messages","每日对话已达上限"),
    QUOTA_LIMIT_TOTAL_TOKENS_DAY(402, "error.quota_limit_total_tokens_day","每日tokens已到上限"),
    QUOTA_LIMIT_CONVERSATION_MESSAGE_COUNT(402, "error.quota_limit_conversation_message_count","当前对话已达上限，点击右下角开启新会话"),
    QUOTA_LIMIT_CONVERSATION_TOKENS(402, "error.quota_limit_conversation_tokens","当前会话Tokens已达上限，点击右下角开启新会话"),
    QUOTA_LIMIT_CONVERSATION_COUNT_DAY(402, "error.quota_limit_conversation_count_day","每日会话次数已到上限"),
    /**
     * 参数错误
     */
    DATE_PARSE_ERROR(250400, "error.date_parse_error","日期转换错误"),
    /**
     * 禁止访问
     */
    AUTH_ERROR(250401, "error.auth_error","认证失败，禁止访问"),
    /**
     * 操作失败
     */
    OPT_ERROR(250402, "error.opt_error","操作失败"),

    /**
     * 系统错误
     */
    SYSTEM_ERROR(250500, "error.system_error","系统错误"),
    /**
     * 签名错误
     */
    INVALID_SIGNATURE(250401, "error.invalid_signature","签名错误"),
    /**
     * HEADER参数错误
     */
    INVALID_HEADER(250402, "error.invalid_header","HEADER参数错误"),
    /**
     * 参数错误
     */
    INVALID_PARAMS(250402, "error.invalid_params","参数错误"),
    /**
     * 视频评论不足
     */
    VIDEO_COMMENT_NOT_ENOUGH(250501, "error.video_comment_not_enough","视频评论不足"),
    USER_NOT_FOUND(250404, "error.user_not_found","用户不存在"),
    ;

    private int code;

    private String i18nKey;

    private String message;


}
