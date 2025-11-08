-- ScriptCraft AI 数据库初始化脚本
-- MySQL 8.0+

-- 创建数据库
CREATE DATABASE IF NOT EXISTS scriptcraft_ai 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE scriptcraft_ai;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID主键',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态:1-正常,0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 脚本生成会话表
CREATE TABLE IF NOT EXISTS script_sessions (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID主键',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    video_type VARCHAR(50) NOT NULL COMMENT '视频类型:product_review,knowledge,vlog,comedy,food,makeup,movie,unboxing,skill',
    theme_input TEXT NOT NULL COMMENT '主题描述(最多200字)',
    style_preference VARCHAR(50) COMMENT '风格偏好:humorous,professional,cute,passionate,emotional,suspenseful',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_video_type (video_type),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='脚本生成会话表';

-- 脚本版本表
CREATE TABLE IF NOT EXISTS script_versions (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID主键',
    session_id VARCHAR(36) NOT NULL COMMENT '会话ID',
    version_index INT NOT NULL COMMENT '版本序号:1,2,3',
    title VARCHAR(200) NOT NULL COMMENT '脚本标题',
    content_json TEXT NOT NULL COMMENT '脚本内容(JSON格式)',
    is_selected TINYINT DEFAULT 0 COMMENT '是否被选中:1-是,0-否',
    word_count INT COMMENT '字数统计',
    scene_count INT COMMENT '分镜数量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_session_id (session_id),
    INDEX idx_created_at (created_at),
    INDEX idx_is_selected (is_selected),
    FOREIGN KEY (session_id) REFERENCES script_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='脚本版本表';

-- 脚本收藏表 (V2功能)
CREATE TABLE IF NOT EXISTS favorite_scripts (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID主键',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    version_id VARCHAR(36) NOT NULL COMMENT '脚本版本ID',
    tags VARCHAR(500) COMMENT '标签(逗号分隔)',
    notes TEXT COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_version_id (version_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (version_id) REFERENCES script_versions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='脚本收藏表';

-- 插入初始数据（测试用户）
-- 密码: test123456 (BCrypt加密后的值，实际使用时会由后端生成)
INSERT INTO users (id, email, password, nickname, status) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'test@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '测试用户', 1);

-- 查看表结构
SHOW TABLES;

-- 查看用户表结构
DESCRIBE users;
DESCRIBE script_sessions;
DESCRIBE script_versions;
DESCRIBE favorite_scripts;

-- 显示创建成功信息
SELECT '数据库初始化完成！' AS message;
SELECT CONCAT('数据库: ', DATABASE()) AS current_database;
SELECT COUNT(*) AS user_count FROM users;

