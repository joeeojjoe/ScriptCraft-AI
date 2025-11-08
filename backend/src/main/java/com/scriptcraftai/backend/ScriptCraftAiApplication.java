package com.scriptcraftai.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ScriptCraft AI 应用启动类
 * 
 * @description AI短视频脚本生成器后端服务主入口
 * @author ScriptCraft AI Team
 */
@SpringBootApplication
@MapperScan("com.scriptcraftai.backend.mapper")
public class ScriptCraftAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScriptCraftAiApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  ScriptCraft AI Backend 启动成功！");
        System.out.println("  访问地址: http://localhost:8080/api");
        System.out.println("========================================\n");
    }
}

