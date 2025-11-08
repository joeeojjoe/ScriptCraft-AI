package com.scriptcraftai.backend.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务异常类
 * 
 * @description 自定义业务异常，用于抛出业务逻辑错误
 * @author ScriptCraft AI Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {
    
    /**
     * 错误状态码
     */
    private Integer code;
    
    /**
     * 错误消息
     */
    private String message;

    /**
     * 构造函数（默认500状态码）
     * 
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    /**
     * 构造函数（自定义状态码）
     * 
     * @param code 错误状态码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数（带异常原因）
     * 
     * @param code 错误状态码
     * @param message 错误消息
     * @param cause 异常原因
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}

