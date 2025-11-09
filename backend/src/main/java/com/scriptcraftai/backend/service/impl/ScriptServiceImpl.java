package com.scriptcraftai.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scriptcraftai.backend.dto.*;
import com.scriptcraftai.backend.entity.ScriptSession;
import com.scriptcraftai.backend.entity.ScriptVersion;
import com.scriptcraftai.backend.exception.BusinessException;
import com.scriptcraftai.backend.mapper.ScriptSessionMapper;
import com.scriptcraftai.backend.mapper.ScriptVersionMapper;
import com.scriptcraftai.backend.request.GenerateScriptRequest;
import com.scriptcraftai.backend.service.AiService;
import com.scriptcraftai.backend.service.ScriptService;
import com.scriptcraftai.backend.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 脚本服务实现类
 * 
 * @description 实现脚本生成和管理业务逻辑
 * @author ScriptCraft AI Team
 */
@Slf4j
@Service
public class ScriptServiceImpl implements ScriptService {
    
    @Autowired
    private AiService aiService;
    
    @Autowired
    private ScriptSessionMapper sessionMapper;
    
    @Autowired
    private ScriptVersionMapper versionMapper;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<GenerateScriptDTO> generateScripts(GenerateScriptRequest request, String userId) {
        
        try {
            // 1. 创建会话
            ScriptSession session = new ScriptSession();
            session.setId(IdGenerator.generateUUID());
            session.setUserId(userId);
            session.setVideoType(request.getVideoType());
            session.setThemeInput(request.getThemeInput());
            session.setStylePreference(request.getStylePreference());
            sessionMapper.insert(session);
            //记录开始时间
            long startTime = System.currentTimeMillis();
            // 2. 并行生成3个脚本方案
            List<CompletableFuture<ScriptContentDTO>> futures = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                futures.add(aiService.generateScript(
                    request.getVideoType(),
                    request.getThemeInput(),
                    request.getStylePreference()
                ));
            }
            // 3. 等待所有生成完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // 4. 保存到数据库
            List<ScriptVersionBriefDTO> versions = new ArrayList<>();
            for (int i = 0; i < futures.size(); i++) {
                ScriptContentDTO content = futures.get(i).get();
                
                ScriptVersion version = new ScriptVersion();
                version.setId(IdGenerator.generateUUID());
                version.setSessionId(session.getId());
                version.setVersionIndex(i + 1);
                version.setTitle(content.getTitle());
                version.setContentJson(objectMapper.writeValueAsString(content));
                version.setIsSelected(0);
                version.setWordCount(calculateWordCount(content));
                version.setSceneCount(content.getScenes() != null ? content.getScenes().length : 0);
                
                versionMapper.insert(version);
                
                // 构建返回DTO
                ScriptVersionBriefDTO briefDTO = new ScriptVersionBriefDTO();
                briefDTO.setVersionId(version.getId());
                briefDTO.setVersionIndex(version.getVersionIndex());
                briefDTO.setTitle(version.getTitle());
                
                ScriptVersionBriefDTO.PreviewDTO preview = new ScriptVersionBriefDTO.PreviewDTO();
                preview.setFirstScene(content.getScenes() != null && content.getScenes().length > 0 ? 
                    content.getScenes()[0].getVisualDescription() : "");
                preview.setWordCount(version.getWordCount());
                preview.setSceneCount(version.getSceneCount());
                briefDTO.setPreview(preview);
                
                versions.add(briefDTO);
            }
            //记录结束时间
            long endTime = System.currentTimeMillis();
            log.info("脚本生成完成，所消耗的时间为{}",(endTime - startTime) / 1000.0);
            
            GenerateScriptDTO result = new GenerateScriptDTO();
            result.setSessionId(session.getId());
            result.setVersions(versions);
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            log.error("生成脚本失败: {}", e.getMessage(), e);
            throw new BusinessException("生成脚本失败，请稍后重试");
        }
    }

    @Override
    public ScriptVersion getVersionDetail(String versionId, String userId) {
        ScriptVersion version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException(404, "脚本不存在");
        }
        
        // 验证权限
        ScriptSession session = sessionMapper.selectById(version.getSessionId());
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权访问此脚本");
        }
        
        return version;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScriptVersion updateVersion(String versionId, ScriptContentDTO content, String userId) {
        ScriptVersion version = getVersionDetail(versionId, userId);
        
        try {
            version.setTitle(content.getTitle());
            version.setContentJson(objectMapper.writeValueAsString(content));
            version.setWordCount(calculateWordCount(content));
            version.setSceneCount(content.getScenes() != null ? content.getScenes().length : 0);
            
            versionMapper.update(version);
            
            log.info("脚本更新成功: versionId={}", versionId);
            return version;
            
        } catch (Exception e) {
            log.error("更新脚本失败: {}", e.getMessage(), e);
            throw new BusinessException("更新脚本失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selectVersion(String versionId, String userId) {
        ScriptVersion version = getVersionDetail(versionId, userId);
        
        // 取消同会话下所有版本的选中状态
        versionMapper.unselectAllBySessionId(version.getSessionId());
        
        // 标记当前版本为选中
        versionMapper.updateSelectedStatus(versionId, 1);
        
        log.info("标记选中脚本: versionId={}", versionId);
    }

    @Override
    public List<ScriptVersionBriefDTO> getSessionVersions(String sessionId, String userId) {
        ScriptSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(404, "会话不存在");
        }
        
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权访问此会话");
        }
        
        List<ScriptVersion> versions = versionMapper.selectBySessionId(sessionId);
        List<ScriptVersionBriefDTO> result = new ArrayList<>();
        
        for (ScriptVersion version : versions) {
            ScriptVersionBriefDTO dto = new ScriptVersionBriefDTO();
            dto.setVersionId(version.getId());
            dto.setVersionIndex(version.getVersionIndex());
            dto.setTitle(version.getTitle());
            
            ScriptVersionBriefDTO.PreviewDTO preview = new ScriptVersionBriefDTO.PreviewDTO();
            preview.setWordCount(version.getWordCount());
            preview.setSceneCount(version.getSceneCount());
            
            try {
                ScriptContentDTO content = objectMapper.readValue(version.getContentJson(), ScriptContentDTO.class);
                preview.setFirstScene(content.getScenes() != null && content.getScenes().length > 0 ?
                    content.getScenes()[0].getVisualDescription() : "");
            } catch (Exception e) {
                preview.setFirstScene("");
            }
            
            dto.setPreview(preview);
            result.add(dto);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getUserHistory(String userId, Integer page, Integer pageSize) {
        page = (page != null && page > 0) ? page : 1;
        pageSize = (pageSize != null && pageSize > 0 && pageSize <= 50) ? pageSize : 10;
        
        int offset = (page - 1) * pageSize;
        int total = sessionMapper.countByUserId(userId);
        List<ScriptSession> sessions = sessionMapper.selectByUserId(userId, offset, pageSize);
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("sessions", sessions);
        
        return result;
    }
    
    private int calculateWordCount(ScriptContentDTO content) {
        int count = 0;
        if (content.getScenes() != null) {
            for (ScriptContentDTO.SceneDTO scene : content.getScenes()) {
                if (scene.getVoiceover() != null) {
                    count += scene.getVoiceover().length();
                }
            }
        }
        return count;
    }
}

