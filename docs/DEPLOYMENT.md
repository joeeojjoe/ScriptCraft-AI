# ScriptCraft AI 部署指南

## 环境要求

### 后端
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+

### 前端
- Node.js 18+
- npm 9+

## 部署步骤

### 1. 数据库配置

#### 创建数据库
```bash
mysql -u root -p
```

```sql
CREATE DATABASE scriptcraft_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 执行初始化脚本
```bash
mysql -u root -p scriptcraft_ai < database/init.sql
```

### 2. Redis配置

启动Redis服务：
```bash
# Linux/Mac
redis-server

# Windows
redis-server.exe
```

### 3. 后端部署

#### 开发环境

1. 配置环境变量
```bash
cd backend

# Linux/Mac
export DB_PASSWORD=your_mysql_password
export REDIS_PASSWORD=your_redis_password
export JWT_SECRET=your_jwt_secret_key_at_least_64_chars
export TONGYI_API_KEY=your_tongyi_api_key

# Windows PowerShell
$env:DB_PASSWORD="your_mysql_password"
$env:REDIS_PASSWORD="your_redis_password"
$env:JWT_SECRET="your_jwt_secret_key_at_least_64_chars"
$env:TONGYI_API_KEY="your_tongyi_api_key"
```

2. 启动应用
```bash
mvn clean install
mvn spring-boot:run
```

后端将在 `http://localhost:8080/api` 启动

#### 生产环境

1. 打包应用
```bash
mvn clean package -DskipTests
```

2. 运行JAR包
```bash
java -jar target/scriptcraft-ai-backend.jar \
  --spring.datasource.password=your_mysql_password \
  --spring.data.redis.password=your_redis_password \
  --jwt.secret=your_jwt_secret \
  --tongyi.api-key=your_tongyi_api_key
```

或使用环境变量：
```bash
export DB_PASSWORD=your_mysql_password
export REDIS_PASSWORD=your_redis_password
export JWT_SECRET=your_jwt_secret
export TONGYI_API_KEY=your_tongyi_api_key

java -jar target/scriptcraft-ai-backend.jar
```

### 4. 前端部署

#### 开发环境

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将在 `http://localhost:5173` 启动

#### 生产环境

1. 构建应用
```bash
npm run build
```

2. 使用Nginx部署

创建Nginx配置文件：
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端API代理
    location /api {
        proxy_pass http://localhost:8080/api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

3. 重启Nginx
```bash
sudo nginx -t
sudo nginx -s reload
```

## Docker部署（推荐）

### 1. 创建Docker Compose文件

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: your_password
      MYSQL_DATABASE: scriptcraft_ai
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build: ./backend
    depends_on:
      - mysql
      - redis
    environment:
      DB_PASSWORD: your_password
      JWT_SECRET: your_jwt_secret
      TONGYI_API_KEY: your_api_key
    ports:
      - "8080:8080"

  frontend:
    build: ./frontend
    depends_on:
      - backend
    ports:
      - "80:80"

volumes:
  mysql_data:
```

### 2. 启动服务

```bash
docker-compose up -d
```

## 环境变量说明

### 后端环境变量

| 变量名 | 说明 | 必填 | 示例 |
|--------|------|------|------|
| DB_PASSWORD | MySQL密码 | 是 | root |
| REDIS_PASSWORD | Redis密码 | 否 | - |
| JWT_SECRET | JWT密钥 | 是 | 至少64位随机字符串 |
| TONGYI_API_KEY | 通义千问API Key | 是 | sk-xxx |

### 前端环境变量

| 变量名 | 说明 | 必填 | 默认值 |
|--------|------|------|--------|
| VITE_API_BASE_URL | API基础URL | 否 | /api |

## 获取通义千问API Key

1. 访问 [阿里云控制台](https://dashscope.console.aliyun.com/)
2. 开通通义千问服务
3. 创建API Key
4. 复制API Key并配置到环境变量

## 健康检查

### 后端健康检查
```bash
curl http://localhost:8080/api/auth/login
```

### 前端访问测试
```bash
curl http://localhost:5173
```

## 常见问题

### 1. 数据库连接失败
- 检查MySQL是否启动
- 确认数据库名称和密码正确
- 检查防火墙设置

### 2. Redis连接失败
- 检查Redis是否启动
- 确认端口6379未被占用

### 3. 前端无法访问后端API
- 检查后端是否启动
- 确认代理配置正确
- 检查CORS配置

### 4. 通义千问API调用失败
- 确认API Key正确
- 检查账号余额
- 查看API调用限制

## 性能优化

### 后端优化
- 启用Redis缓存
- 配置数据库连接池
- 调整JVM参数

### 前端优化
- 启用Nginx Gzip压缩
- 配置静态资源缓存
- 使用CDN加速

## 安全建议

1. **生产环境必须修改默认密码**
2. **使用HTTPS协议**
3. **定期备份数据库**
4. **配置防火墙规则**
5. **限制API调用频率**

## 监控和日志

### 后端日志
```bash
tail -f logs/spring-boot-application.log
```

### 前端日志
查看浏览器控制台

## 技术支持

- 问题反馈：[GitHub Issues](https://github.com/your-repo/issues)
- 邮箱：support@scriptcraft.ai

