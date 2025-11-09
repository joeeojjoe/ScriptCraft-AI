package com.scriptcraftai.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scriptcraftai.backend.dto.ScriptContentDTO;
import com.scriptcraftai.backend.exception.BusinessException;
import com.scriptcraftai.backend.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AI服务实现类
 * 
 * @description 调用通义千问API生成脚本
 * @author ScriptCraft AI Team
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {
    
    @Value("${tongyi.api-key}")
    private String apiKey;
    
    @Value("${tongyi.api-url}")
    private String apiUrl;
    
    @Value("${tongyi.model}")
    private String model;
    
    @Autowired
    private WebClient webClient;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 生成单个脚本内容
     * 
     * @param videoType 视频类型
     * @param themeInput 主题描述
     * @param stylePreference 风格偏好
     * @return 脚本内容（异步）
     */
    @Override
    @Async
    public CompletableFuture<ScriptContentDTO> generateScript(String videoType, 
                                                              String themeInput, 
                                                              String stylePreference) {
        log.info("开始生成脚本: videoType={}, theme={}", videoType, themeInput);
        
        try {
            // 1. 构建提示词
            String prompt = buildPrompt(videoType, themeInput, stylePreference);
            
            // 2. 调用通义千问API
            String response = callTongyiApi(prompt);
            
            // 3. 解析响应
            ScriptContentDTO content = parseResponse(response);
            
            log.info("脚本生成成功: title={}", content.getTitle());
            return CompletableFuture.completedFuture(content);
            
        } catch (Exception e) {
            log.error("脚本生成失败: {}", e.getMessage(), e);
            throw new BusinessException("AI脚本生成失败，请稍后重试");
        }
    }

    /**
     * 构建提示词
     * 
     * @param videoType 视频类型
     * @param themeInput 主题描述
     * @param stylePreference 风格偏好
     * @return 提示词
     */
    private String buildPrompt(String videoType, String themeInput, String stylePreference) {
        StringBuilder prompt = new StringBuilder();
        // 增强专家设定
        prompt.append("你是一位资深的短视频内容专家，有着丰富的创作经验。\n");
        prompt.append("特别擅长将复杂专业知识转化为通俗易懂的短视频内容。\n");
        prompt.append("你的脚本总是准确、专业、有趣，并且具有很强的实用价值。\n\n");
        // 内容要求
        prompt.append("创作要求：\n");
        prompt.append("1. 深入理解主题的专业内涵\n");
        prompt.append("2. 使用准确的术语和概念\n");
        prompt.append("3. 避免常识性错误\n");
        prompt.append("4. 提供可操作的实用建议\n");
        prompt.append("5. 保持内容有趣性和可看性\n\n");
    
        // 领域分析
        prompt.append("请先分析这个主题涉及的专业领域和关键要点：\n");
        prompt.append("- 核心概念：\n");
        prompt.append("- 实用技巧：\n");
        prompt.append("- 注意事项：\n");
        prompt.append("- 常见误区：\n\n");
        // 脚本要求
        prompt.append("请根据以下要求生成一个完整的短视频脚本：\n\n");
        prompt.append("视频类型：").append(getVideoTypeLabel(videoType)).append("\n");
        prompt.append("主题：").append(themeInput).append("\n");
        
        if (stylePreference != null && !stylePreference.isEmpty()) {
            prompt.append("风格：").append(getStyleLabel(stylePreference)).append("\n");
        }
        
        prompt.append("\n请按照以下JSON格式返回脚本内容（直接返回JSON，不要有任何其他说明文字）：\n");
        prompt.append("{\n");
        prompt.append("  \"title\": \"脚本标题\",\n");
        prompt.append("  \"alternativeTitles\": [\"备选标题1\", \"备选标题2\"],\n");
        prompt.append("  \"scenes\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"timeRange\": \"0-10秒\",\n");
        prompt.append("      \"visualDescription\": \"画面描述\",\n");
        prompt.append("      \"voiceover\": \"文案/旁白\",\n");
        prompt.append("      \"subtitle\": \"字幕提示\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"videoElements\": {\n");
        prompt.append("    \"bgmStyle\": \"BGM风格建议\",\n");
        prompt.append("    \"shootingLocation\": \"拍摄场地建议\",\n");
        prompt.append("    \"effects\": \"特效/转场建议\"\n");
        prompt.append("  },\n");
        prompt.append("  \"endingCTA\": [\"结尾话术1\", \"结尾话术2\", \"结尾话术3\"]\n");
        prompt.append("}\n\n");
        prompt.append("要求：\n");
        prompt.append("1. 脚本时长控制在60秒内\n");
        prompt.append("2. 分镜数量3-6个\n");
        prompt.append("3. 每个分镜的文案简洁有力\n");
        prompt.append("4. 画面描述要具体可执行\n");
        prompt.append("5. 确保返回的是纯JSON格式，不要包含任何markdown标记或其他文字");
        
        return prompt.toString();
    }

    /**
     * 调用通义千问API
     * 
     * @param prompt 提示词
     * @return API响应
     */
    private String callTongyiApi(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        
        Map<String, Object> input = new HashMap<>();
        input.put("messages", new Object[]{
            Map.of("role", "user", "content", prompt)
        });
        requestBody.put("input", input);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("result_format", "message");
        requestBody.put("parameters", parameters);
        
        try {
            String response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();
            
            log.debug("通义千问API响应: {}", response);
            return response;
            
        } catch (Exception e) {
            log.error("调用通义千问API失败: {}", e.getMessage(), e);
            throw new BusinessException("AI服务调用失败，请稍后重试");
        }
    }

    /**
     * 解析API响应
     * 
     * @param response API响应
     * @return 脚本内容
     */
    private ScriptContentDTO parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String content = root.at("/output/choices/0/message/content").asText();
            
            // 清理可能的markdown标记
            content = content.replaceAll("```json\\s*", "")
                           .replaceAll("```\\s*", "")
                           .trim();
            
            return objectMapper.readValue(content, ScriptContentDTO.class);
            
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage(), e);
            throw new BusinessException("解析AI响应失败，请重新生成");
        }
    }

    /**
     * 获取视频类型标签
     * 
     * @param type 类型代码
     * @return 类型标签
     */
    private String getVideoTypeLabel(String type) {
        return switch (type) {
            case "product_review" -> "产品测评";
            case "knowledge" -> "知识科普";
            case "vlog" -> "Vlog日记";
            case "comedy" -> "搞笑剧情";
            case "food" -> "美食制作";
            case "makeup" -> "美妆教程";
            case "movie" -> "影视解说";
            case "unboxing" -> "开箱体验";
            case "skill" -> "技能教学";
            default -> "其他";
        };
    }

    /**
     * 获取风格标签
     * 
     * @param style 风格代码
     * @return 风格标签
     */
    private String getStyleLabel(String style) {
        return switch (style) {
            case "humorous" -> "幽默风趣";
            case "professional" -> "专业严谨";
            case "cute" -> "亲切可爱";
            case "passionate" -> "激情澎湃";
            case "emotional" -> "温情故事";
            case "suspenseful" -> "悬念刺激";
            default -> "";
        };
    }
}

