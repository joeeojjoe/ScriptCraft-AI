package com.scriptcraftai.backend.security;

import com.scriptcraftai.backend.dto.UserDTO;
import com.scriptcraftai.backend.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 会话认证过滤器
 *
 * @description 拦截请求，验证Redis会话
 * @author ScriptCraft AI Team
 */
@Slf4j
@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private SessionService sessionService;

    /**
     * 过滤器核心方法
     *
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 从请求头中获取token
            String token = extractTokenFromRequest(request);

            // 2. 验证会话
            if (token != null && sessionService.isSessionValid(token)) {
                // 3. 从会话中获取用户信息
                UserDTO userDTO = sessionService.getSessionUser(token);

                if (userDTO != null) {
                    // 4. 续期会话（滑动过期）
                    sessionService.refreshSession(token);

                    // 5. 创建认证对象
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDTO.getId(), null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6. 设置到Security上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("会话认证成功: userId={}", userDTO.getId());
                }
            }
        } catch (Exception e) {
            log.error("会话认证失败: {}", e.getMessage());
        }

        // 7. 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取token
     *
     * @param request HTTP请求
     * @return token字符串
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

