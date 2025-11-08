package com.scriptcraftai.backend.mapper;

import com.scriptcraftai.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper接口
 * 
 * @description 用户数据访问层
 * @author ScriptCraft AI Team
 */
@Mapper
public interface UserMapper {
    
    /**
     * 插入用户
     * 
     * @param user 用户对象
     * @return 影响行数
     */
    int insert(User user);
    
    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户对象
     */
    User selectById(@Param("id") String id);
    
    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return 用户对象
     */
    User selectByEmail(@Param("email") String email);
    
    /**
     * 更新用户信息
     * 
     * @param user 用户对象
     * @return 影响行数
     */
    int update(User user);
    
    /**
     * 根据ID删除用户
     * 
     * @param id 用户ID
     * @return 影响行数
     */
    int deleteById(@Param("id") String id);
}

