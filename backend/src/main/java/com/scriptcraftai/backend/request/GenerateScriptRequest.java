package com.scriptcraftai.backend.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 脚本生成请求对象
 * 
 * @description 用于接收脚本生成时提交的数据
 * @author ScriptCraft AI Team
 */
@Data
public class GenerateScriptRequest {
    
    /**
     * 视频类型
     */
    @NotBlank(message = "视频类型不能为空")
    private String videoType;
    
    /**
     * 主题描述
     */
    @NotBlank(message = "主题描述不能为空")
    @Size(min = 1, max = 200, message = "主题描述长度必须在1-200字之间")
    private String themeInput;
    
    /**
     * 风格偏好（可选）
     */
    private String stylePreference;
}

