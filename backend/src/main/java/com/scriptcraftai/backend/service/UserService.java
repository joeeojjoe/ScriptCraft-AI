package com.scriptcraftai.backend.service;

import com.scriptcraftai.backend.dto.LoginDTO;
import com.scriptcraftai.backend.dto.UserDTO;
import com.scriptcraftai.backend.request.LoginRequest;
import com.scriptcraftai.backend.request.RegisterRequest;

/**
 * 用户服务接口
 * 
 * @description 定义用户相关业务逻辑
 * @author ScriptCraft AI Team
 */
public interface UserService {
    
    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @return 用户信息
     */
    UserDTO register(RegisterRequest request);
    
    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录信息（包含token）
     */
    LoginDTO login(LoginRequest request);
    
    /**
     * 根据ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    UserDTO getUserById(String userId);
    
    /**
     * 根据邮箱获取用户信息
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    UserDTO getUserByEmail(String email);
}

