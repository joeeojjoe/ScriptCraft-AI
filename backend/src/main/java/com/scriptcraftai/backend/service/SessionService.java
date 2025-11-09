package com.scriptcraftai.backend.service;

import com.scriptcraftai.backend.dto.UserDTO;

/**
 * 会话服务接口
 *
 * @description Redis会话管理
 * @author ScriptCraft AI Team
 */
public interface SessionService {

    /**
     * 创建用户会话
     *
     * @param userId 用户ID
     * @param userDTO 用户信息
     * @return 会话token
     */
    String createSession(String userId, UserDTO userDTO);

    /**
     * 获取会话用户信息
     *
     * @param token 会话token
     * @return 用户信息
     */
    UserDTO getSessionUser(String token);

    /**
     * 验证会话是否有效
     *
     * @param token 会话token
     * @return true-有效，false-无效
     */
    boolean isSessionValid(String token);

    /**
     * 销毁会话
     *
     * @param token 会话token
     */
    void destroySession(String token);

    /**
     * 续期会话
     *
     * @param token 会话token
     */
    void refreshSession(String token);

    /**
     * 根据用户ID销毁所有会话（用于封禁用户等）
     *
     * @param userId 用户ID
     */
    void destroyUserSessions(String userId);

    /**
     * 获取在线用户数量（可选功能）
     *
     * @return 在线用户数
     */
    long getOnlineUserCount();
}

