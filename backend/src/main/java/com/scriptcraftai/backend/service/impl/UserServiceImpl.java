package com.scriptcraftai.backend.service.impl;

import com.scriptcraftai.backend.dto.LoginDTO;
import com.scriptcraftai.backend.dto.UserDTO;
import com.scriptcraftai.backend.entity.User;
import com.scriptcraftai.backend.exception.BusinessException;
import com.scriptcraftai.backend.mapper.UserMapper;
import com.scriptcraftai.backend.request.LoginRequest;
import com.scriptcraftai.backend.request.RegisterRequest;
import com.scriptcraftai.backend.service.UserService;
import com.scriptcraftai.backend.util.IdGenerator;
import com.scriptcraftai.backend.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

/**
 * 用户服务实现类
 * 
 * @description 实现用户相关业务逻辑
 * @author ScriptCraft AI Team
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @return 用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO register(RegisterRequest request) {
        log.info("用户注册请求: email={}", request.getEmail());
        
        // 1. 检查邮箱是否已存在
        User existUser = userMapper.selectByEmail(request.getEmail());
        if (existUser != null) {
            throw new BusinessException(400, "该邮箱已被注册");
        }
        
        // 2. 创建用户对象
        User user = new User();
        user.setId(IdGenerator.generateUUID());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : extractNicknameFromEmail(request.getEmail()));
        user.setStatus(1); // 默认正常状态
        
        // 3. 保存到数据库
        int rows = userMapper.insert(user);
        if (rows == 0) {
            throw new BusinessException("注册失败，请稍后重试");
        }
        
        log.info("用户注册成功: userId={}, email={}", user.getId(), user.getEmail());
        
        // 4. 返回用户信息
        return convertToDTO(user);
    }

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录信息（包含token）
     */
    @Override
    public LoginDTO login(LoginRequest request) {
        log.info("用户登录请求: email={}", request.getEmail());
        
        // 1. 查询用户
        User user = userMapper.selectByEmail(request.getEmail());
        if (user == null) {
            throw new BusinessException(401, "邮箱或密码错误");
        }
        
        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "邮箱或密码错误");
        }
        
        // 3. 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }
        
        // 4. 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        
        log.info("用户登录成功: userId={}, email={}", user.getId(), user.getEmail());
        
        // 5. 返回登录信息
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setToken(token);
        loginDTO.setUser(convertToDTO(user));
        
        return loginDTO;
    }

    /**
     * 根据ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public UserDTO getUserById(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return convertToDTO(user);
    }

    /**
     * 根据邮箱获取用户信息
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return convertToDTO(user);
    }

    /**
     * 将User实体转换为UserDTO
     * 
     * @param user User实体
     * @return UserDTO对象
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setNickname(user.getNickname());
        dto.setAvatarUrl(user.getAvatarUrl());
        if (user.getCreatedAt() != null) {
            dto.setCreatedAt(user.getCreatedAt().format(FORMATTER));
        }
        return dto;
    }

    /**
     * 从邮箱中提取昵称（邮箱@前的部分）
     * 
     * @param email 邮箱
     * @return 昵称
     */
    private String extractNicknameFromEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex > 0) {
            return email.substring(0, atIndex);
        }
        return "用户" + System.currentTimeMillis();
    }
}

