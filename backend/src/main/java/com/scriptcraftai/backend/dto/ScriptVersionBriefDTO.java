package com.scriptcraftai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 脚本版本简要信息DTO
 * 
 * @description 用于列表展示的脚本版本信息
 * @author ScriptCraft AI Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScriptVersionBriefDTO {
    
    /**
     * 版本ID
     */
    private String versionId;
    
    /**
     * 版本序号
     */
    private Integer versionIndex;
    
    /**
     * 脚本标题
     */
    private String title;
    
    /**
     * 预览信息
     */
    private PreviewDTO preview;

    /**
     * 是否被选中：1-是，0-否
     */
    private Integer isSelected;
    
    /**
     * 预览信息DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreviewDTO {
        /**
         * 第一个分镜内容
         */
        private String firstScene;
        
        /**
         * 字数统计
         */
        private Integer wordCount;
        
        /**
         * 分镜数量
         */
        private Integer sceneCount;
    }
}

