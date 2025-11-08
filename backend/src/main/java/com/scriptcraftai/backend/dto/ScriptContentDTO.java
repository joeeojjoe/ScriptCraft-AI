package com.scriptcraftai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 脚本内容DTO
 * 
 * @description 脚本的结构化内容
 * @author ScriptCraft AI Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScriptContentDTO {
    
    /**
     * 主标题
     */
    private String title;
    
    /**
     * 备选标题
     */
    private String[] alternativeTitles;
    
    /**
     * 分镜列表
     */
    private SceneDTO[] scenes;
    
    /**
     * 视频元素建议
     */
    private VideoElementsDTO videoElements;
    
    /**
     * 结尾互动话术
     */
    private String[] endingCTA;
    
    /**
     * 分镜DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SceneDTO {
        /**
         * 时间范围
         */
        private String timeRange;
        
        /**
         * 画面描述
         */
        private String visualDescription;
        
        /**
         * 文案/旁白
         */
        private String voiceover;
        
        /**
         * 字幕提示
         */
        private String subtitle;
    }
    
    /**
     * 视频元素DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoElementsDTO {
        /**
         * BGM风格建议
         */
        private String bgmStyle;
        
        /**
         * 拍摄场地建议
         */
        private String shootingLocation;
        
        /**
         * 特效/转场建议
         */
        private String effects;
    }
}

