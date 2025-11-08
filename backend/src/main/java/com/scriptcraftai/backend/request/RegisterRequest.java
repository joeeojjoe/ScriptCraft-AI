package com.scriptcraftai.backend.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求对象
 * 
 * @description 用于接收用户注册时提交的数据
 * @author ScriptCraft AI Team
 */
@Data
public class RegisterRequest {
    
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
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;
    
    /**
     * 昵称（可选）
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
}

