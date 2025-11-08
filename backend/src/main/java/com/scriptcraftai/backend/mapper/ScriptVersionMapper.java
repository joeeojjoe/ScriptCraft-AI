package com.scriptcraftai.backend.mapper;

import com.scriptcraftai.backend.entity.ScriptVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 脚本版本Mapper接口
 * 
 * @description 脚本版本数据访问层
 * @author ScriptCraft AI Team
 */
@Mapper
public interface ScriptVersionMapper {
    
    /**
     * 插入脚本版本
     * 
     * @param version 版本对象
     * @return 影响行数
     */
    int insert(ScriptVersion version);
    
    /**
     * 根据ID查询版本
     * 
     * @param id 版本ID
     * @return 版本对象
     */
    ScriptVersion selectById(@Param("id") String id);
    
    /**
     * 根据会话ID查询所有版本
     * 
     * @param sessionId 会话ID
     * @return 版本列表
     */
    List<ScriptVersion> selectBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 更新脚本版本
     * 
     * @param version 版本对象
     * @return 影响行数
     */
    int update(ScriptVersion version);
    
    /**
     * 更新选中状态
     * 
     * @param id 版本ID
     * @param isSelected 是否选中
     * @return 影响行数
     */
    int updateSelectedStatus(@Param("id") String id, @Param("isSelected") Integer isSelected);
    
    /**
     * 取消会话下所有版本的选中状态
     * 
     * @param sessionId 会话ID
     * @return 影响行数
     */
    int unselectAllBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据ID删除版本
     * 
     * @param id 版本ID
     * @return 影响行数
     */
    int deleteById(@Param("id") String id);
}

