package com.scriptcraftai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 脚本生成响应DTO
 * 
 * @description 返回脚本生成结果
 * @author ScriptCraft AI Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateScriptDTO {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 生成的脚本版本列表
     */
    private List<ScriptVersionBriefDTO> versions;
}

