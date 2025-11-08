package com.scriptcraftai.backend.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求对象
 * 
 * @description 用于接收用户登录时提交的数据
 * @author ScriptCraft AI Team
 */
@Data
public class LoginRequest {
    
    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}

