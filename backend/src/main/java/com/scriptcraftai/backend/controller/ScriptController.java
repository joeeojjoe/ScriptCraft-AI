package com.scriptcraftai.backend.controller;

import com.scriptcraftai.backend.common.Result;
import com.scriptcraftai.backend.dto.GenerateScriptDTO;
import com.scriptcraftai.backend.dto.ScriptContentDTO;
import com.scriptcraftai.backend.dto.ScriptVersionBriefDTO;
import com.scriptcraftai.backend.entity.ScriptVersion;
import com.scriptcraftai.backend.request.GenerateScriptRequest;
import com.scriptcraftai.backend.service.ScriptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 脚本控制器
 * 
 * @description 处理脚本生成、查询、更新等请求
 * @author ScriptCraft AI Team
 */
@Slf4j
@RestController
@RequestMapping("/scripts")
public class ScriptController {
    
    @Autowired
    private ScriptService scriptService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 生成脚本
     *
     * @param request 生成请求
     * @return 生成结果
     */
    @PostMapping("/generate")
    public Result<GenerateScriptDTO> generateScripts(@Valid @RequestBody GenerateScriptRequest request) {
        String userId = getCurrentUserId();
        log.info("收到脚本生成请求: userId={}, videoType={}", userId, request.getVideoType());

        try {
            // 同步调用，避免异步线程安全上下文问题
            GenerateScriptDTO result = scriptService.generateScripts(request, userId)
                    .get(180, java.util.concurrent.TimeUnit.SECONDS); // 3分钟超时

            log.info("脚本生成成功，返回结果: sessionId={}", result.getSessionId());
            return Result.success(result, "生成成功");
        } catch (Exception e) {
            log.error("生成脚本失败", e);
            return Result.failed("生成失败，请稍后重试");
        }
    }

    /**
     * 获取脚本版本详情
     * 
     * @param versionId 版本ID
     * @return 脚本详情
     */
    @GetMapping("/versions/{versionId}")
    public Result<Map<String, Object>> getVersionDetail(@PathVariable String versionId) {
        String userId = getCurrentUserId();
        ScriptVersion version = scriptService.getVersionDetail(versionId, userId);
        
        try {
            ScriptContentDTO content = objectMapper.readValue(version.getContentJson(), ScriptContentDTO.class);
            
            Map<String, Object> result = Map.of(
                "id", version.getId(),
                "sessionId", version.getSessionId(),
                "versionIndex", version.getVersionIndex(),
                "title", version.getTitle(),
                "content", content,
                "isSelected", version.getIsSelected() == 1,
                "wordCount", version.getWordCount(),
                "sceneCount", version.getSceneCount(),
                "createdAt", version.getCreatedAt()
            );
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("解析脚本内容失败", e);
            return Result.failed("获取脚本详情失败");
        }
    }

    /**
     * 更新脚本内容
     * 
     * @param versionId 版本ID
     * @param request 更新请求
     * @return 更新结果
     */
    @PutMapping("/versions/{versionId}")
    public Result<Map<String, Object>> updateVersion(@PathVariable String versionId,
                                                     @RequestBody Map<String, Object> request) {
        String userId = getCurrentUserId();
        
        try {
            ScriptContentDTO content = objectMapper.convertValue(request.get("content"), ScriptContentDTO.class);
            ScriptVersion version = scriptService.updateVersion(versionId, content, userId);
            
            return Result.success(Map.of(
                "versionId", version.getId(),
                "updatedAt", version.getUpdatedAt()
            ), "更新成功");
        } catch (Exception e) {
            log.error("更新脚本失败", e);
            return Result.failed("更新失败");
        }
    }

    /**
     * 标记选中脚本
     * 
     * @param versionId 版本ID
     * @return 操作结果
     */
    @PostMapping("/versions/{versionId}/select")
    public Result<Void> selectVersion(@PathVariable String versionId) {
        String userId = getCurrentUserId();
        scriptService.selectVersion(versionId, userId);
        return Result.success("标记成功");
    }

    /**
     * 获取会话详情
     * 
     * @param sessionId 会话ID
     * @return 会话详情
     */
    @GetMapping("/sessions/{sessionId}")
    public Result<List<ScriptVersionBriefDTO>> getSessionVersions(@PathVariable String sessionId) {
        String userId = getCurrentUserId();
        List<ScriptVersionBriefDTO> versions = scriptService.getSessionVersions(sessionId, userId);
        return Result.success(versions);
    }

    /**
     * 获取生成历史
     * 
     * @param page 页码
     * @param pageSize 每页数量
     * @return 历史记录
     */
    @GetMapping("/sessions")
    public Result<Map<String, Object>> getUserHistory(@RequestParam(required = false) Integer page,
                                                       @RequestParam(required = false) Integer pageSize) {
        String userId = getCurrentUserId();
        Map<String, Object> history = scriptService.getUserHistory(userId, page, pageSize);
        return Result.success(history);
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

