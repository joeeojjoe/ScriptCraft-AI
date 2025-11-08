package com.scriptcraftai.backend.service;

import com.scriptcraftai.backend.dto.GenerateScriptDTO;
import com.scriptcraftai.backend.dto.ScriptContentDTO;
import com.scriptcraftai.backend.dto.ScriptVersionBriefDTO;
import com.scriptcraftai.backend.entity.ScriptVersion;
import com.scriptcraftai.backend.request.GenerateScriptRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 脚本服务接口
 * 
 * @description 定义脚本生成和管理相关业务逻辑
 * @author ScriptCraft AI Team
 */
public interface ScriptService {
    
    /**
     * 生成脚本（2-3个方案）
     * 
     * @param request 生成请求
     * @param userId 用户ID
     * @return 生成结果（异步）
     */
    CompletableFuture<GenerateScriptDTO> generateScripts(GenerateScriptRequest request, String userId);
    
    /**
     * 获取脚本版本详情
     * 
     * @param versionId 版本ID
     * @param userId 用户ID
     * @return 脚本版本对象
     */
    ScriptVersion getVersionDetail(String versionId, String userId);
    
    /**
     * 更新脚本内容
     * 
     * @param versionId 版本ID
     * @param content 脚本内容
     * @param userId 用户ID
     * @return 更新后的版本对象
     */
    ScriptVersion updateVersion(String versionId, ScriptContentDTO content, String userId);
    
    /**
     * 标记选中脚本
     * 
     * @param versionId 版本ID
     * @param userId 用户ID
     */
    void selectVersion(String versionId, String userId);
    
    /**
     * 获取会话详情（包含所有版本）
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 版本列表
     */
    List<ScriptVersionBriefDTO> getSessionVersions(String sessionId, String userId);
    
    /**
     * 获取用户的生成历史
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 历史记录
     */
    Map<String, Object> getUserHistory(String userId, Integer page, Integer pageSize);
}

