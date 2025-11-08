package com.scriptcraftai.backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果类
 * 
 * @description 封装所有API接口的统一返回格式
 * @param <T> 数据类型
 * @author ScriptCraft AI Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    
    /**
     * 状态码：200成功，其他失败
     */
    private Integer code;
    
    /**
     * 提示信息
     */
    private String message;
    
    /**
     * 返回数据
     */
    private T data;
    
    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 成功响应（带数据和消息）
     * 
     * @param data 返回数据
     * @param message 提示消息
     * @return Result对象
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(200, message, data, true);
    }

    /**
     * 成功响应（仅数据）
     * 
     * @param data 返回数据
     * @return Result对象
     */
    public static <T> Result<T> success(T data) {
        return success(data, "操作成功");
    }

    /**
     * 成功响应（仅消息）
     * 
     * @param message 提示消息
     * @return Result对象
     */
    public static <T> Result<T> success(String message) {
        return success(null, message);
    }

    /**
     * 失败响应（带状态码和消息）
     * 
     * @param code 错误状态码
     * @param message 错误消息
     * @return Result对象
     */
    public static <T> Result<T> failed(Integer code, String message) {
        return new Result<>(code, message, null, false);
    }

    /**
     * 失败响应（默认500状态码）
     * 
     * @param message 错误消息
     * @return Result对象
     */
    public static <T> Result<T> failed(String message) {
        return failed(500, message);
    }

    /**
     * 参数错误响应（400）
     * 
     * @param message 错误消息
     * @return Result对象
     */
    public static <T> Result<T> badRequest(String message) {
        return failed(400, message);
    }

    /**
     * 未认证响应（401）
     * 
     * @param message 错误消息
     * @return Result对象
     */
    public static <T> Result<T> unauthorized(String message) {
        return failed(401, message);
    }

    /**
     * 无权限响应（403）
     * 
     * @param message 错误消息
     * @return Result对象
     */
    public static <T> Result<T> forbidden(String message) {
        return failed(403, message);
    }

    /**
     * 资源不存在响应（404）
     * 
     * @param message 错误消息
     * @return Result对象
     */
    public static <T> Result<T> notFound(String message) {
        return failed(404, message);
    }
}

