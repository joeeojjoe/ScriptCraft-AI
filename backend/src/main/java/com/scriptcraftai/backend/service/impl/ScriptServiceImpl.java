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

    // 重新生成时的超时时间
    private static final long REGENERATION_TIMEOUT_SECONDS = 70L;

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
            // 2. 生成单个脚本方案
            ScriptContentDTO content = aiService.generateScript(
                request.getVideoType(),
                request.getThemeInput(),
                request.getStylePreference()
            ).get(70, TimeUnit.SECONDS); // 70秒超时

            // 3. 保存到数据库
            ScriptVersion version = new ScriptVersion();
            version.setId(IdGenerator.generateUUID());
            version.setSessionId(session.getId());
            version.setVersionIndex(1); // 只有一个版本
            version.setTitle(content.getTitle());
            version.setContentJson(objectMapper.writeValueAsString(content));
            version.setIsSelected(1); // 直接设为选中状态，因为只有一个版本
            version.setWordCount(calculateWordCount(content));
            version.setSceneCount(content.getScenes() != null ? content.getScenes().length : 0);

            versionMapper.insert(version);

            // 构建返回DTO
            List<ScriptVersionBriefDTO> versions = new ArrayList<>();
            ScriptVersionBriefDTO briefDTO = new ScriptVersionBriefDTO();
            briefDTO.setVersionId(version.getId());
            briefDTO.setVersionIndex(version.getVersionIndex());
            briefDTO.setTitle(version.getTitle());
            briefDTO.setIsSelected(1); // 设置为选中状态

            ScriptVersionBriefDTO.PreviewDTO preview = new ScriptVersionBriefDTO.PreviewDTO();
            preview.setFirstScene(content.getScenes() != null && content.getScenes().length > 0 ?
                content.getScenes()[0].getVisualDescription() : "");
            preview.setWordCount(version.getWordCount());
            preview.setSceneCount(version.getSceneCount());
            briefDTO.setPreview(preview);

            versions.add(briefDTO);
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
            dto.setIsSelected(version.getIsSelected());
            
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
    public Map<String, Object> getUserHistory(String userId, Integer page, Integer pageSize, String videoType) {
        page = (page != null && page > 0) ? page : 1;
        pageSize = (pageSize != null && pageSize > 0 && pageSize <= 50) ? pageSize : 10;
        
        int offset = (page - 1) * pageSize;
        int total = sessionMapper.countByUserId(userId);
        List<ScriptSession> sessions = sessionMapper.selectByUserId(userId, offset, pageSize, videoType);
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("sessions", sessions);
        
        return result;
    }

    @Override
    public void deleteSession(String sessionId) {
        ScriptSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(404, "会话不存在");
        }
        sessionMapper.deleteById(sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSceneLock(String versionId, Integer sceneIndex, Boolean locked, String userId) {
        ScriptVersion version = getVersionDetail(versionId, userId);

        try {
            // 解析当前的锁定状态
            Set<Integer> lockedSceneSet = parseLockedScenes(version.getLockedScenes());

            if (locked) {
                lockedSceneSet.add(sceneIndex);
            } else {
                lockedSceneSet.remove(sceneIndex);
            }

            // 保存新的锁定状态
            version.setLockedScenes(formatLockedScenes(lockedSceneSet));
            versionMapper.update(version);

            log.info("更新分镜锁定状态: versionId={}, sceneIndex={}, locked={}", versionId, sceneIndex, locked);
        } catch (Exception e) {
            log.error("更新分镜锁定状态失败: versionId={}, sceneIndex={}", versionId, sceneIndex, e);
            throw new BusinessException("更新分镜锁定状态失败");
        }
    }

    @Override
    public ScriptContentDTO regenerateScript(String versionId, String userId) {
        ScriptVersion version = getVersionDetail(versionId, userId);

        try {
            // 解析锁定状态
            Set<Integer> lockedSceneSet = parseLockedScenes(version.getLockedScenes());

            // 如果没有锁定任何分镜，直接返回原内容
            if (lockedSceneSet.isEmpty()) {
                return objectMapper.readValue(version.getContentJson(), ScriptContentDTO.class);
            }

            // 解析原内容
            ScriptContentDTO originalContent = objectMapper.readValue(version.getContentJson(), ScriptContentDTO.class);

            // 构建新的提示词
            String prompt = buildRegenerationPrompt(originalContent, lockedSceneSet);

            // 调用AI重新生成
            ScriptContentDTO newContent = aiService.generateScript(
                "regeneration", // 特殊类型表示重新生成
                prompt,
                "professional"
            ).get(REGENERATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // 合并锁定分镜和新生成的内容
            ScriptContentDTO mergedContent = mergeContentWithLocks(originalContent, newContent, lockedSceneSet);

            // 更新数据库
            version.setContentJson(objectMapper.writeValueAsString(mergedContent));
            version.setWordCount(calculateWordCount(mergedContent));
            versionMapper.update(version);

            log.info("重新生成脚本成功: versionId={}, lockedScenes={}", versionId, lockedSceneSet);
            return mergedContent;

        } catch (Exception e) {
            log.error("重新生成脚本失败: versionId={}", versionId, e);
            throw new BusinessException("重新生成脚本失败");
        }
    }

    /**
     * 解析锁定分镜的JSON字符串
     *
     * @param lockedScenesJson JSON字符串，如 "[0,2,4]"
     * @return 锁定分镜的索引集合
     */
    private Set<Integer> parseLockedScenes(String lockedScenesJson) {
        Set<Integer> result = new HashSet<>();
        if (lockedScenesJson == null || lockedScenesJson.trim().isEmpty()) {
            return result;
        }

        try {
            List<Integer> list = objectMapper.readValue(lockedScenesJson,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class));
            result.addAll(list);
        } catch (Exception e) {
            log.warn("解析锁定分镜数据失败: {}", lockedScenesJson, e);
        }

        return result;
    }

    /**
     * 格式化锁定分镜为JSON字符串
     *
     * @param lockedScenes 锁定分镜的索引集合
     * @return JSON字符串
     */
    private String formatLockedScenes(Set<Integer> lockedScenes) {
        if (lockedScenes == null || lockedScenes.isEmpty()) {
            return null;
        }

        try {
            List<Integer> sortedList = new ArrayList<>(lockedScenes);
            Collections.sort(sortedList);
            return objectMapper.writeValueAsString(sortedList);
        } catch (Exception e) {
            log.error("格式化锁定分镜数据失败", e);
            return null;
        }
    }

    /**
     * 构建重新生成的提示词
     *
     * @param originalContent 原始内容
     * @param lockedScenes 锁定的分镜索引
     * @return 提示词
     */
    private String buildRegenerationPrompt(ScriptContentDTO originalContent, Set<Integer> lockedScenes) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个专业的短视频脚本编辑专家。请基于现有的脚本，重新生成未锁定的分镜内容。\n\n");

        prompt.append("原始脚本标题：").append(originalContent.getTitle()).append("\n\n");

        prompt.append("分镜情况：\n");
        ScriptContentDTO.SceneDTO[] scenes = originalContent.getScenes();
        if (scenes != null) {
            for (int i = 0; i < scenes.length; i++) {
                if (lockedScenes.contains(i)) {
                    prompt.append("分镜").append(i + 1).append("（已锁定，保持不变）：\n");
                    prompt.append("- 时间范围：").append(scenes[i].getTimeRange()).append("\n");
                    prompt.append("- 画面描述：").append(scenes[i].getVisualDescription()).append("\n");
                    prompt.append("- 文案/旁白：").append(scenes[i].getVoiceover()).append("\n");
                    prompt.append("- 字幕提示：").append(scenes[i].getSubtitle()).append("\n\n");
                } else {
                    prompt.append("分镜").append(i + 1).append("（需要重新生成）：\n");
                    prompt.append("- 时间范围：").append(scenes[i].getTimeRange()).append("\n");
                    prompt.append("- 保持风格一致，但内容要创新\n\n");
                }
            }
        }

        prompt.append("要求：\n");
        prompt.append("1. 保持锁定的分镜完全不变\n");
        prompt.append("2. 只重新生成未锁定的分镜\n");
        prompt.append("3. 整体风格和质量要与原脚本保持一致\n");
        prompt.append("4. 返回完整的脚本JSON格式\n");
        prompt.append("5. 确保返回的是纯JSON格式，不要包含任何markdown标记或其他文字\n\n");

        prompt.append("请按照以下JSON格式返回重新生成的完整脚本内容：\n");
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
        prompt.append("}\n");

        return prompt.toString();
    }

    /**
     * 合并原内容和重新生成的内容
     *
     * @param originalContent 原始内容
     * @param newContent 新生成的内容
     * @param lockedScenes 锁定的分镜索引
     * @return 合并后的内容
     */
    private ScriptContentDTO mergeContentWithLocks(ScriptContentDTO originalContent,
                                                  ScriptContentDTO newContent,
                                                  Set<Integer> lockedScenes) {
        ScriptContentDTO merged = new ScriptContentDTO();

        // 保持标题等基本信息
        merged.setTitle(originalContent.getTitle());
        merged.setAlternativeTitles(originalContent.getAlternativeTitles());
        merged.setVideoElements(originalContent.getVideoElements());
        merged.setEndingCTA(originalContent.getEndingCTA());

        // 合并分镜
        ScriptContentDTO.SceneDTO[] originalScenes = originalContent.getScenes();
        ScriptContentDTO.SceneDTO[] newScenes = newContent.getScenes();

        if (originalScenes != null && newScenes != null && originalScenes.length == newScenes.length) {
            ScriptContentDTO.SceneDTO[] mergedScenes = new ScriptContentDTO.SceneDTO[originalScenes.length];

            for (int i = 0; i < originalScenes.length; i++) {
                if (lockedScenes.contains(i)) {
                    // 保持锁定的分镜不变
                    mergedScenes[i] = originalScenes[i];
                } else {
                    // 使用新生成的分镜
                    mergedScenes[i] = newScenes[i];
                }
            }

            merged.setScenes(mergedScenes);
        } else {
            // 如果分镜数量不匹配，使用新内容
            merged.setScenes(newContent.getScenes());
        }

        return merged;
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

