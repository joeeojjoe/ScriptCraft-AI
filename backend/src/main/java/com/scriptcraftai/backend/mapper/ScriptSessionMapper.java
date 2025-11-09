package com.scriptcraftai.backend.mapper;

import com.scriptcraftai.backend.entity.ScriptSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 脚本会话Mapper接口
 * 
 * @description 脚本会话数据访问层
 * @author ScriptCraft AI Team
 */
@Mapper
public interface ScriptSessionMapper {
    
    /**
     * 插入脚本会话
     * 
     * @param session 会话对象
     * @return 影响行数
     */
    int insert(ScriptSession session);
    
    /**
     * 根据ID查询会话
     * 
     * @param id 会话ID
     * @return 会话对象
     */
    ScriptSession selectById(@Param("id") String id);
    
    /**
     * 查询用户的会话列表（分页）
     * 
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @param videoType 视频类型
     * @return 会话列表
     */
    List<ScriptSession> selectByUserId(@Param("userId") String userId, 
                                      @Param("offset") Integer offset, 
                                      @Param("limit") Integer limit,
                                       @Param("videoType") String videoType);
    
    /**
     * 统计用户的会话总数
     * 
     * @param userId 用户ID
     * @return 总数
     */
    int countByUserId(@Param("userId") String userId);
    
    /**
     * 根据ID删除会话
     * 
     * @param id 会话ID
     * @return 影响行数
     */
    int deleteById(@Param("id") String id);
}

