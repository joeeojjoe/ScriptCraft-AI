package com.scriptcraftai.backend.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 *
 * @description 配置RedisTemplate和序列化方式
 * @author ScriptCraft AI Team
 */
@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate
     * 1. Spring Data Redis Starter 根据配置创建 RedisConnectionFactory
     * 2. 注入RedisConnectionFactory来连接Redis服务器
     * 3. 使用StringRedisSerializer来序列化和反序列化redis的key值
     * 4. 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate实例
     */

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 配置 ObjectMapper
        ObjectMapper om = new ObjectMapper();
        // 配置 ObjectMapper 可以访问所有属性（包括 private 字段），用于 JSON 序列化/反序列化
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 启用默认类型推断，允许在序列化时保存对象的具体类型信息
        // 使用 LaissezFaireSubTypeValidator.instance 作为类型验证器，对非 final 类型进行类型信息存储
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        // 使用构造函数方式传入 ObjectMapper（避免弃用警告）
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer<>(om, Object.class);

        // 使用 StringRedisSerializer 来序列化 key
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key 采用 String 的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // value 序列化方式采用 Jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        // 初始化 RedisTemplate，确保 RedisTemplate 的属性已经设置
        template.afterPropertiesSet();
        return template;
    }

}

