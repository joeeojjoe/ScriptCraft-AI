package com.scriptcraftai.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient配置类
 * 
 * @description 配置用于调用外部API的WebClient
 * @author ScriptCraft AI Team
 */
@Configuration
public class WebClientConfig {

    /**
     * 创建WebClient实例
     * 
     * @return WebClient实例
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }
}

