package com.scriptcraftai.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 脚本版本实体类
 * 
 * @description 对应script_versions表
 * @author ScriptCraft AI Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScriptVersion {
    
    /**
     * 版本ID（UUID）
     */
    private String id;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 版本序号（1,2,3）
     */
    private Integer versionIndex;
    
    /**
     * 脚本标题
     */
    private String title;
    
    /**
     * 脚本内容（JSON格式）
     */
    private String contentJson;
    
    /**
     * 是否被选中：1-是，0-否
     */
    private Integer isSelected;
    
    /**
     * 字数统计
     */
    private Integer wordCount;
    
    /**
     * 分镜数量
     */
    private Integer sceneCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

