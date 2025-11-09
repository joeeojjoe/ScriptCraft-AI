package com.scriptcraftai.backend.controller;

import com.scriptcraftai.backend.common.Result;
import com.scriptcraftai.backend.dto.LoginDTO;
import com.scriptcraftai.backend.dto.UserDTO;
import com.scriptcraftai.backend.request.LoginRequest;
import com.scriptcraftai.backend.request.RegisterRequest;
import com.scriptcraftai.backend.service.SessionService;
import com.scriptcraftai.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器
 * 
 * @description 处理用户注册、登录、获取用户信息等请求
 * @author ScriptCraft AI Team
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @return 用户信息
     */
    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求: email={}", request.getEmail());
        UserDTO userDTO = userService.register(request);
        return Result.success(userDTO, "注册成功");
    }

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录信息（包含token）
     */
    @PostMapping("/login")
    public Result<LoginDTO> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求: email={}", request.getEmail());
        LoginDTO loginDTO = userService.login(request);
        return Result.success(loginDTO, "登录成功");
    }

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/profile")
    public Result<UserDTO> getProfile() {
        String userId = getCurrentUserId();
        log.info("获取用户信息: userId={}", userId);
        UserDTO userDTO = userService.getUserById(userId);
        return Result.success(userDTO, "获取成功");
    }

    /**
     * 用户登出
     *
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        try {
            String userId = getCurrentUserId();
            // 根据用户ID销毁所有会话（支持多设备登录）
            sessionService.destroyUserSessions(userId);
            log.info("用户登出成功: userId={}", userId);
            return Result.success("登出成功");
        } catch (Exception e) {
            log.error("登出失败", e);
            return Result.failed("登出失败");
        }
    }

    /**
     * 获取当前登录用户ID
     * 
     * @return 用户ID
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (String) authentication.getPrincipal();
    }
}

