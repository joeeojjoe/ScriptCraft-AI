package com.scriptcraftai.backend.service;

import com.scriptcraftai.backend.dto.ScriptContentDTO;

import java.util.concurrent.CompletableFuture;

/**
 * AI服务接口
 * 
 * @description 定义AI相关功能
 * @author ScriptCraft AI Team
 */
public interface AiService {
    
    /**
     * 生成单个脚本内容
     * 
     * @param videoType 视频类型
     * @param themeInput 主题描述
     * @param stylePreference 风格偏好
     * @return 脚本内容（异步）
     */
    CompletableFuture<ScriptContentDTO> generateScript(String videoType, 
                                                       String themeInput, 
                                                       String stylePreference);
}

