package com.scriptcraftai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 * 
 * @description 用于返回登录成功后的信息
 * @author ScriptCraft AI Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    
    /**
     * JWT令牌
     */
    private String token;
    
    /**
     * 用户信息
     */
    private UserDTO user;
}

