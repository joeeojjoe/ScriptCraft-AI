# ScriptCraft AI - AI短视频脚本生成器

> 面向短视频创作者的AI辅助脚本生成工具，通过大语言模型快速生成多个结构化、可直接使用的短视频脚本方案

## 📋 项目概述

### 核心价值
- ⚡ **效率提升**：将数小时的脚本构思过程缩短至1分钟内
- 💡 **创意激发**：提供同一主题的多种创作角度和风格
- 🎯 **专业赋能**：让新手也能产出结构专业的视频内容
- 📊 **决策支持**：多方案对比，选择最优创作方向

### 目标用户
- 短视频博主（抖音、快手、B站、小红书等）
- 社交媒体运营人员
- 内容营销团队
- 电商直播主
- 个人内容创作者

## 🏗️ 技术架构

### 技术栈
```
前端：Vue 3 + JavaScript + Element Plus + Tailwind CSS + Pinia + Vue Router
后端：Spring Boot 3.x + Spring Security + JWT + MyBatis + MySQL 8.0 + Redis
AI服务：阿里云通义千问API
```

### 系统架构图
```
┌─────────────┐
│  用户浏览器  │
└──────┬──────┘
       │
┌──────▼──────────────────────────┐
│      Vue 3 前端应用              │
│  ┌──────────┬──────────────┐   │
│  │ 登录注册  │  脚本生成     │   │
│  ├──────────┼──────────────┤   │
│  │ 多方案展示│  脚本编辑     │   │
│  ├──────────┼──────────────┤   │
│  │ 历史记录  │  个人中心     │   │
│  └──────────┴──────────────┘   │
└──────┬──────────────────────────┘
       │ HTTP/HTTPS
┌──────▼──────────────────────────┐
│   Spring Boot 后端服务           │
│  ┌──────────────────────────┐  │
│  │   Controller 层           │  │
│  │   (处理HTTP请求)          │  │
│  └──────────┬────────────────┘  │
│  ┌──────────▼────────────────┐  │
│  │   Service 层              │  │
│  │   (业务逻辑+AI调用)       │  │
│  └──────────┬────────────────┘  │
│  ┌──────────▼────────────────┐  │
│  │   Mapper 层               │  │
│  │   (数据访问)              │  │
│  └──────────┬────────────────┘  │
└─────────────┼───────────────────┘
              │
     ┌────────┴────────┐
     │                 │
┌────▼─────┐    ┌─────▼──────┐
│  MySQL   │    │   Redis    │
│ (持久化)  │    │  (缓存)    │
└──────────┘    └────────────┘
     │
┌────▼─────────────────┐
│  阿里云通义千问API    │
│  (AI脚本生成)         │
└──────────────────────┘
```

### 数据库设计

#### 用户表 (users)
```sql
CREATE TABLE users (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

#### 脚本生成会话表 (script_sessions)
```sql
CREATE TABLE script_sessions (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID主键',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    video_type VARCHAR(50) NOT NULL COMMENT '视频类型',
    theme_input TEXT NOT NULL COMMENT '主题描述',
    style_preference VARCHAR(50) COMMENT '风格偏好',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='脚本生成会话表';
```

#### 脚本版本表 (script_versions)
```sql
CREATE TABLE script_versions (
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
    FOREIGN KEY (session_id) REFERENCES script_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='脚本版本表';
```

#### 脚本收藏表 (favorite_scripts) - V2功能
```sql
CREATE TABLE favorite_scripts (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='脚本收藏表';
```

### 脚本内容JSON结构示例
```json
{
  "title": "如何在30天内学会弹吉他",
  "alternativeTitles": [
    "零基础吉他速成指南",
    "30天吉他挑战：从小白到高手"
  ],
  "scenes": [
    {
      "timeRange": "0-10秒",
      "visualDescription": "特写镜头：手指在吉他弦上快速弹奏，配合节奏感强的背景",
      "voiceover": "你是否梦想过在朋友聚会上秀一把吉他技巧？",
      "subtitle": "30天速成计划"
    },
    {
      "timeRange": "11-25秒",
      "visualDescription": "展示学习资料：吉他教材、APP界面、练习视频",
      "voiceover": "今天我要分享的方法，让我在一个月内从零基础到能弹5首歌",
      "subtitle": "真实经验分享"
    }
  ],
  "videoElements": {
    "bgmStyle": "轻快的流行音乐",
    "shootingLocation": "温馨的家庭客厅或音乐工作室",
    "effects": "快速剪辑、节奏卡点、字幕动效"
  },
  "endingCTA": [
    "关注我，下期教你第一首歌！",
    "评论区说出你想学的歌，我来教你！",
    "点赞收藏，开始你的吉他之旅！"
  ]
}
```

## 🚀 MVP功能范围 (v1.0)

### ✅ 包含功能
1. **用户认证**
   - 邮箱注册/登录
   - JWT会话管理
   - 基础用户信息存储

2. **脚本生成**
   - 视频类型选择（9种预设类型）
   - 主题描述输入（200字内）
   - 风格偏好选择（6种风格）
   - 一键生成2-3个脚本方案

3. **脚本展示与选择**
   - 标签页展示多个方案
   - 脚本预览（标题+前2个分镜）
   - 选择并进入详情页

4. **脚本详情与编辑**
   - 结构化展示（标题、分镜、视频元素、结尾CTA）
   - 文本内容编辑
   - 复制全文到剪贴板
   - 保存编辑后的内容

5. **历史记录**
   - 查看生成历史（按时间倒序）
   - 搜索和筛选
   - 快速访问历史脚本

### ⏳ V2功能（暂缓）
- 局部重新生成
- 文件导出（PDF/Word）
- 收藏标签系统
- 分享链接功能
- 创作偏好设置
- API使用统计

## 📦 项目结构

```
ScriptCraft-AI/
├── frontend/                    # Vue3前端项目
│   ├── src/
│   │   ├── api/                # API接口封装
│   │   ├── assets/             # 静态资源
│   │   ├── components/         # 公共组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia状态管理
│   │   ├── utils/              # 工具函数
│   │   ├── views/              # 页面组件
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
│
├── backend/                     # Spring Boot后端项目
│   ├── src/main/java/com/scriptcraftai/backend/
│   │   ├── controller/         # 控制器层
│   │   ├── service/            # 服务接口
│   │   │   └── impl/          # 服务实现
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── entity/             # 实体类
│   │   ├── dto/                # 数据传输对象
│   │   ├── request/            # 请求对象
│   │   ├── config/             # 配置类
│   │   ├── security/           # 安全相关
│   │   ├── exception/          # 异常处理
│   │   ├── common/             # 公共类
│   │   └── util/               # 工具类
│   ├── src/main/resources/
│   │   ├── mapper/             # MyBatis XML
│   │   ├── application.yml     # 配置文件
│   │   └── application-dev.yml
│   └── pom.xml
│
├── database/                    # 数据库脚本
│   ├── init.sql               # 初始化脚本
│   └── sample_data.sql        # 示例数据
│
├── docs/                        # 项目文档
│   ├── API.md                 # API文档
│   └── DEPLOYMENT.md          # 部署文档
│
├── .gitignore
└── README.md
```

## 🚀 快速开始

详细启动步骤请查看：[快速启动指南](./QUICKSTART.md)

### 简要步骤

**1. 初始化数据库**
```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source database/init.sql
# 或
mysql -u root -p < database/init.sql
```

**2. 启动Redis**
```bash
redis-server
```

**3. 启动后端**
```bash
cd backend

# 配置环境变量
export DB_PASSWORD=root  # MySQL密码
export JWT_SECRET=ScriptCraftAI2025SecretKeyForJWTTokenGenerationAndValidation
export TONGYI_API_KEY=your_tongyi_api_key  # 通义千问API Key

# 启动应用
mvn clean install
mvn spring-boot:run
```

后端将在 `http://localhost:8080/api` 启动

**4. 启动前端**
```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将在 `http://localhost:5173` 启动

**5. 访问应用**

打开浏览器访问 `http://localhost:5173`

### 获取通义千问API Key

1. 访问 [阿里云控制台](https://dashscope.console.aliyun.com/)
2. 注册/登录阿里云账号
3. 开通「通义千问」服务
4. 进入控制台创建API Key
5. 复制API Key配置到环境变量

**注意：** 通义千问需要实名认证，新用户有免费额度。

## 🔧 开发指南

### 环境要求
- **Java** 17+
- **Node.js** 18+
- **MySQL** 8.0+
- **Redis** 7.0+
- **Maven** 3.8+

### 后端开发 (Spring Boot)

#### 技术栈
- Spring Boot 3.2.0
- Spring Security + JWT
- MyBatis 3.0.3
- MySQL 8.0
- Redis 7.0
- 通义千问API

#### 项目结构
```
backend/src/main/java/com/scriptcraftai/backend/
├── controller/          # 控制器层
│   ├── AuthController.java      # 用户认证
│   └── ScriptController.java    # 脚本管理
├── service/            # 服务接口
│   ├── UserService.java
│   ├── AiService.java
│   └── ScriptService.java
├── service/impl/       # 服务实现
│   ├── UserServiceImpl.java
│   ├── AiServiceImpl.java       # AI脚本生成
│   └── ScriptServiceImpl.java
├── mapper/             # MyBatis Mapper
│   ├── UserMapper.java
│   ├── ScriptSessionMapper.java
│   └── ScriptVersionMapper.java
├── entity/             # 实体类
│   ├── User.java
│   ├── ScriptSession.java
│   └── ScriptVersion.java
├── dto/                # 数据传输对象
├── request/            # 请求对象
├── config/             # 配置类
├── security/           # 安全相关
├── exception/          # 异常处理
├── common/             # 公共类
└── util/               # 工具类
```

#### 核心API
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/profile` - 获取用户信息
- `POST /api/scripts/generate` - 生成脚本
- `GET /api/scripts/versions/{id}` - 获取脚本详情
- `PUT /api/scripts/versions/{id}` - 更新脚本
- `GET /api/scripts/sessions` - 获取历史记录

#### 开发规范
- 使用UUID作为主键
- 时间字段使用LocalDateTime
- 统一返回Result<T>格式
- Service层使用接口+实现类
- 所有SQL使用MyBatis XML
- 使用@Slf4j记录日志
- 使用@Transactional管理事务

#### 配置文件
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/scriptcraft_ai
    username: root
    password: ${DB_PASSWORD}
  
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: ${JWT_SECRET}
  expiration: 604800000  # 7天

tongyi:
  api-key: ${TONGYI_API_KEY}
  model: qwen-plus
```

### 前端开发 (Vue 3)

#### 技术栈
- Vue 3 + JavaScript
- Vite 5
- Element Plus (UI组件库)
- Tailwind CSS (样式框架)
- Vue Router 4 (路由管理)
- Pinia (状态管理)
- Axios (HTTP请求)

#### 项目结构
```
frontend/src/
├── api/                # API接口封装
│   ├── auth.js        # 用户认证接口
│   └── script.js      # 脚本相关接口
├── stores/             # Pinia状态管理
│   ├── user.js        # 用户状态
│   └── script.js      # 脚本状态
├── router/             # 路由配置
│   └── index.js
├── utils/              # 工具函数
│   ├── request.js     # Axios封装
│   └── constants.js   # 常量定义
├── views/              # 页面组件
│   ├── Login.vue      # 登录页
│   ├── Register.vue   # 注册页
│   ├── Home.vue       # 首页
│   ├── Generate.vue   # 脚本生成页
│   ├── ScriptList.vue # 脚本列表页
│   ├── ScriptDetail.vue # 脚本详情页
│   └── History.vue    # 历史记录页
├── App.vue            # 根组件
├── main.js            # 应用入口
└── style.css          # 全局样式
```

#### 页面路由
- `/login` - 登录页
- `/register` - 注册页
- `/home` - 首页
- `/generate` - 脚本生成页
- `/script/:sessionId` - 脚本方案列表页
- `/script/detail/:versionId` - 脚本详情页
- `/history` - 历史记录页

#### 开发规范
- 使用Vue 3 Composition API
- 使用setup语法糖
- 组件命名使用PascalCase
- 合理使用ref、reactive
- 统一使用@/api目录的接口
- 使用Pinia管理全局状态

#### 构建命令
```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览构建产物
npm run preview
```

## 🔧 开发指南

### 环境要求
- **Java** 17+
- **Node.js** 18+
- **MySQL** 8.0+
- **Redis** 7.0+
- **Maven** 3.8+

### 后端启动步骤
1. 创建数据库并执行初始化脚本
```bash
mysql -u root -p < database/init.sql
```

2. 配置环境变量（或修改application-dev.yml）
```bash
export DB_PASSWORD=your_db_password
export REDIS_PASSWORD=your_redis_password
export JWT_SECRET=your_jwt_secret_key
export TONGYI_API_KEY=your_tongyi_api_key
```

3. 启动后端服务
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 前端启动步骤
1. 安装依赖
```bash
cd frontend
npm install
```

2. 启动开发服务器
```bash
npm run dev
```

3. 访问 http://localhost:5173

## 📡 核心API接口

### 用户认证
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/profile` - 获取用户信息

### 脚本生成
- `POST /api/scripts/generate` - 生成脚本（返回2-3个方案）
- `GET /api/scripts/sessions` - 获取生成历史
- `GET /api/scripts/sessions/{sessionId}` - 获取会话详情
- `PUT /api/scripts/versions/{versionId}` - 更新脚本内容
- `POST /api/scripts/versions/{versionId}/select` - 标记选中脚本

详细API文档请查看 [docs/API.md](docs/API.md)

## 🎯 开发规范

### 后端规范
- ✅ 使用UUID作为主键（VARCHAR(36)）
- ✅ 时间字段使用LocalDateTime和DATETIME
- ✅ 统一返回Result<T>格式
- ✅ Service层使用接口+实现类
- ✅ 所有SQL使用MyBatis XML编写
- ✅ 使用@Transactional管理事务
- ❌ 禁止在Controller中直接注入Mapper
- ❌ 禁止使用Map作为请求参数或返回值

### 前端规范
- ✅ 使用Vue 3 Composition API
- ✅ 使用setup语法糖
- ✅ 接口调用统一使用@/api目录下的封装
- ✅ 组件化开发，注重复用性
- ✅ 使用Pinia进行状态管理
- ✅ 响应式设计，适配移动端

## 📝 Git提交规范
- `feat: 新功能`
- `fix: 修复Bug`
- `docs: 文档更新`
- `style: 代码格式调整`
- `refactor: 代码重构`
- `test: 测试相关`
- `chore: 构建/配置相关`

## 🤝 贡献指南
欢迎提交Issue和Pull Request！

## 📄 开源协议
MIT License

## 👥 联系方式
- 项目维护者：[Your Name]
- 邮箱：[Your Email]

---
**注意**：本项目使用了阿里云通义千问API，需要申请API Key才能使用。请访问[阿里云官网](https://www.aliyun.com/product/tongyi)申请。

