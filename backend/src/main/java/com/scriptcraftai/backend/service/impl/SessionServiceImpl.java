package com.scriptcraftai.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scriptcraftai.backend.dto.UserDTO;
import com.scriptcraftai.backend.service.SessionService;
import com.scriptcraftai.backend.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

/**
 * 会话服务实现类
 *
 * @description Redis会话管理实现
 * @author ScriptCraft AI Team
 */
@Slf4j
@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 会话过期时间：7天
    private static final Duration SESSION_TIMEOUT = Duration.ofSeconds(604800);

    // Redis key前缀
    private static final String SESSION_KEY_PREFIX = "session:";
    private static final String USER_SESSIONS_KEY_PREFIX = "user_sessions:";

    /**
     * 创建用户会话
     *
     * @param userId 用户ID
     * @param userDTO 用户信息
     * @return 会话token
     */
    @Override
    public String createSession(String userId, UserDTO userDTO) {
        // 生成唯一token
        String token = IdGenerator.generateUUID();

        try {
            // 存储会话信息：token -> 用户信息（序列化为JSON）
            String sessionKey = SESSION_KEY_PREFIX + token;
            String userJson = objectMapper.writeValueAsString(userDTO);
            redisTemplate.opsForValue().set(sessionKey, userJson, SESSION_TIMEOUT);

            // 存储用户会话关系：userId -> token集合
            String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userId;
            redisTemplate.opsForSet().add(userSessionsKey, token);
            redisTemplate.expire(userSessionsKey, SESSION_TIMEOUT);

            log.info("创建用户会话成功: userId={}, token={}", userId, token);
            return token;

        } catch (JsonProcessingException e) {
            log.error("序列化用户信息失败: userId={}", userId, e);
            throw new RuntimeException("创建会话失败");
        }
    }

    /**
     * 获取会话用户信息
     *
     * @param token 会话token
     * @return 用户信息
     */
    @Override
    public UserDTO getSessionUser(String token) {
        try {
            String sessionKey = SESSION_KEY_PREFIX + token;
            Object userObj = redisTemplate.opsForValue().get(sessionKey);

            if (userObj instanceof String) {
                // 如果存储的是JSON字符串，反序列化为UserDTO
                return objectMapper.readValue((String) userObj, UserDTO.class);
            } else if (userObj instanceof UserDTO) {
                return (UserDTO) userObj;
            }

            return null;
        } catch (Exception e) {
            log.error("获取会话用户信息失败: token={}", token, e);
            return null;
        }
    }

    /**
     * 验证会话是否有效
     *
     * @param token 会话token
     * @return true-有效，false-无效
     */
    @Override
    public boolean isSessionValid(String token) {
        try {
            String sessionKey = SESSION_KEY_PREFIX + token;
            return redisTemplate.hasKey(sessionKey);
        } catch (Exception e) {
            log.error("验证会话失败: token={}", token, e);
            return false;
        }
    }

    /**
     * 销毁会话
     *
     * @param token 会话token
     */
    @Override
    public void destroySession(String token) {
        try {
            String sessionKey = SESSION_KEY_PREFIX + token;

            // 获取用户信息以清理用户会话关系
            UserDTO userDTO = getSessionUser(token);
            if (userDTO != null) {
                String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userDTO.getId();
                redisTemplate.opsForSet().remove(userSessionsKey, token);
            }

            // 删除会话
            redisTemplate.delete(sessionKey);

            log.info("销毁会话成功: token={}", token);
        } catch (Exception e) {
            log.error("销毁会话失败: token={}", token, e);
        }
    }

    /**
     * 续期会话
     *
     * @param token 会话token
     */
    @Override
    public void refreshSession(String token) {
        try {
            String sessionKey = SESSION_KEY_PREFIX + token;
            redisTemplate.expire(sessionKey, SESSION_TIMEOUT);
            log.debug("续期会话成功: token={}", token);
        } catch (Exception e) {
            log.error("续期会话失败: token={}", token, e);
        }
    }

    /**
     * 根据用户ID销毁所有会话
     *
     * @param userId 用户ID
     */
    @Override
    public void destroyUserSessions(String userId) {
        try {
            String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userId;
            Set<Object> tokens = redisTemplate.opsForSet().members(userSessionsKey);

            if (tokens != null && !tokens.isEmpty()) {
                // 删除所有会话
                for (Object token : tokens) {
                    String sessionKey = SESSION_KEY_PREFIX + token.toString();
                    redisTemplate.delete(sessionKey);
                }

                // 删除用户会话关系
                redisTemplate.delete(userSessionsKey);

                log.info("销毁用户所有会话成功: userId={}, sessionCount={}", userId, tokens.size());
            }
        } catch (Exception e) {
            log.error("销毁用户会话失败: userId={}", userId, e);
        }
    }

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数
     */
    @Override
    public long getOnlineUserCount() {
        try {
            // 获取所有session开头的key数量
            Set<String> keys = redisTemplate.keys(SESSION_KEY_PREFIX + "*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            log.error("获取在线用户数量失败", e);
            return 0;
        }
    }
}

