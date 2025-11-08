package com.scriptcraftai.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 脚本生成会话实体类
 * 
 * @description 对应script_sessions表
 * @author ScriptCraft AI Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScriptSession {
    
    /**
     * 会话ID（UUID）
     */
    private String id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 视频类型
     */
    private String videoType;
    
    /**
     * 主题描述
     */
    private String themeInput;
    
    /**
     * 风格偏好
     */
    private String stylePreference;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

