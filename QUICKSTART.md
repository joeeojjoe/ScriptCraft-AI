# ScriptCraft AI - 快速启动指南

## 🎯 项目简介

ScriptCraft AI 是一个基于AI的短视频脚本生成工具，帮助创作者快速生成专业的视频脚本。

**核心功能：**
- ✅ 用户注册/登录
- ✅ AI脚本生成（一次生成2-3个方案）
- ✅ 脚本编辑和管理
- ✅ 历史记录查询
- ✅ 一键复制脚本

## 🚀 5分钟快速启动

### 前置条件

确保已安装：
- Java 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+
- Maven 3.8+

### 第一步：克隆项目

```bash
git clone https://github.com/your-repo/ScriptCraft-AI.git
cd ScriptCraft-AI
```

### 第二步：初始化数据库

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source database/init.sql

# 或直接执行
mysql -u root -p < database/init.sql
```

### 第三步：启动Redis

```bash
# Linux/Mac
redis-server

# Windows
redis-server.exe
```

### 第四步：启动后端

```bash
cd backend

# 配置环境变量（必须）
export DB_PASSWORD=root  # 你的MySQL密码
export TONGYI_API_KEY=your_tongyi_api_key  # 你的通义千问API Key

# 启动后端
mvn spring-boot:run
```

✅ 后端启动成功：`http://localhost:8080/api`

### 第五步：启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动前端
npm run dev
```

✅ 前端启动成功：`http://localhost:5173`

## 🎉 开始使用

1. 打开浏览器访问 `http://localhost:5173`
2. 点击「立即注册」创建账号
3. 登录后点击「开始创作」
4. 填写视频主题，选择类型和风格
5. 点击「一键生成脚本」等待AI生成
6. 选择喜欢的脚本方案查看详情
7. 编辑、复制脚本内容用于拍摄

## 📝 获取通义千问API Key

1. 访问 [阿里云控制台](https://dashscope.console.aliyun.com/)
2. 注册/登录阿里云账号
3. 开通「通义千问」服务
4. 进入控制台创建API Key
5. 复制API Key配置到环境变量

**注意：** 通义千问需要实名认证，新用户有免费额度。

## 🛠️ 开发工具推荐

### 后端开发
- IDE: IntelliJ IDEA
- 数据库工具: DataGrip / Navicat
- API测试: Postman / Apifox

### 前端开发
- IDE: VS Code
- 浏览器: Chrome（推荐）
- Vue DevTools扩展

## 📦 项目结构

```
ScriptCraft-AI/
├── backend/             # Spring Boot后端
│   ├── src/
│   └── pom.xml
├── frontend/            # Vue3前端
│   ├── src/
│   └── package.json
├── database/            # 数据库脚本
│   └── init.sql
└── README.md
```

## 🐛 常见问题

### 1. 后端启动失败

**问题：** 数据库连接失败
```
解决：检查MySQL是否启动，密码是否正确
```

**问题：** 端口8080被占用
```
解决：修改application.yml中的server.port
```

### 2. 前端启动失败

**问题：** 依赖安装失败
```bash
# 清除npm缓存
npm cache clean --force
# 删除node_modules重新安装
rm -rf node_modules
npm install
```

**问题：** 端口5173被占用
```
解决：修改vite.config.js中的server.port
```

### 3. AI生成失败

**问题：** 通义千问API调用失败
```
解决：
1. 检查API Key是否正确
2. 确认账号余额充足
3. 查看API调用限制
4. 检查网络连接
```

## 🎨 技术栈

### 后端
- Spring Boot 3.2.0
- Spring Security
- MyBatis 3.0.3
- MySQL 8.0
- Redis 7.0

### 前端
- Vue 3.4
- Element Plus 2.5
- Tailwind CSS 3.4
- Vite 5.0
- Pinia 2.1
- Axios 1.6

