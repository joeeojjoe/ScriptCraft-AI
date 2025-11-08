package com.scriptcraftai.backend.util;

import java.util.UUID;

/**
 * ID生成器工具类
 * 
 * @description 提供UUID生成功能
 * @author ScriptCraft AI Team
 */
public class IdGenerator {

    /**
     * 生成UUID（去掉横线）
     * 
     * @return 32位UUID字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成标准UUID（保留横线）
     * 
     * @return 36位UUID字符串
     */
    public static String generateStandardUUID() {
        return UUID.randomUUID().toString();
    }
}

