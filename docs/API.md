# ScriptCraft AI API 文档

## 基础信息

- 基础URL: `http://localhost:8080/api`
- 认证方式: JWT (Bearer Token)
- 内容类型: `application/json`
- 字符编码: `UTF-8`

## 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "success": true
}
```

**状态码说明：**
- `200` - 成功
- `400` - 请求参数错误
- `401` - 未认证
- `403` - 无权限
- `404` - 资源不存在
- `500` - 服务器内部错误

---

## 用户认证模块

### 1. 用户注册

**接口:** `POST /auth/register`

**描述:** 使用邮箱注册新用户

**请求参数:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "用户昵称"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | string | 是 | 邮箱地址，需符合邮箱格式 |
| password | string | 是 | 密码，6-20位字符 |
| nickname | string | 否 | 用户昵称，默认使用邮箱前缀 |

**响应示例:**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "nickname": "用户昵称"
  },
  "success": true
}
```

---

### 2. 用户登录

**接口:** `POST /auth/login`

**描述:** 使用邮箱和密码登录

**请求参数:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com",
      "nickname": "用户昵称",
      "avatarUrl": null
    }
  },
  "success": true
}
```

**说明:** 登录成功后，需要在后续请求中携带token: `Authorization: Bearer <token>`

---

### 3. 获取用户信息

**接口:** `GET /auth/profile`

**描述:** 获取当前登录用户的详细信息

**请求头:**
```
Authorization: Bearer <token>
```

**响应示例:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "nickname": "用户昵称",
    "avatarUrl": null,
    "createdAt": "2025-01-01 10:00:00"
  },
  "success": true
}
```

---

## 脚本生成模块

### 4. 生成脚本

**接口:** `POST /scripts/generate`

**描述:** 根据用户输入生成2-3个脚本方案

**请求头:**
```
Authorization: Bearer <token>
```

**请求参数:**
```json
{
  "videoType": "knowledge",
  "themeInput": "如何在30天内学会弹吉他",
  "stylePreference": "humorous"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoType | string | 是 | 视频类型，见下方枚举 |
| themeInput | string | 是 | 主题描述，1-200字 |
| stylePreference | string | 否 | 风格偏好，见下方枚举 |

**videoType 枚举值:**
- `product_review` - 产品测评
- `knowledge` - 知识科普
- `vlog` - Vlog日记
- `comedy` - 搞笑剧情
- `food` - 美食制作
- `makeup` - 美妆教程
- `movie` - 影视解说
- `unboxing` - 开箱体验
- `skill` - 技能教学

**stylePreference 枚举值:**
- `humorous` - 幽默风趣
- `professional` - 专业严谨
- `cute` - 亲切可爱
- `passionate` - 激情澎湃
- `emotional` - 温情故事
- `suspenseful` - 悬念刺激

**响应示例:**
```json
{
  "code": 200,
  "message": "生成成功",
  "data": {
    "sessionId": "660e8400-e29b-41d4-a716-446655440000",
    "versions": [
      {
        "versionId": "770e8400-e29b-41d4-a716-446655440001",
        "versionIndex": 1,
        "title": "30天吉他速成：从零到英雄",
        "preview": {
          "firstScene": "特写镜头：手指在吉他弦上快速弹奏...",
          "wordCount": 450,
          "sceneCount": 5
        }
      },
      {
        "versionId": "770e8400-e29b-41d4-a716-446655440002",
        "versionIndex": 2,
        "title": "零基础学吉他：我的真实经验",
        "preview": {
          "firstScene": "开场：我拿着吉他坐在镜头前...",
          "wordCount": 480,
          "sceneCount": 6
        }
      }
    ]
  },
  "success": true
}
```

---

### 5. 获取脚本详情

**接口:** `GET /scripts/versions/{versionId}`

**描述:** 获取某个脚本版本的完整内容

**请求头:**
```
Authorization: Bearer <token>
```

**路径参数:**
- `versionId` - 脚本版本ID

**响应示例:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "770e8400-e29b-41d4-a716-446655440001",
    "sessionId": "660e8400-e29b-41d4-a716-446655440000",
    "versionIndex": 1,
    "title": "30天吉他速成：从零到英雄",
    "content": {
      "title": "30天吉他速成：从零到英雄",
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
    },
    "isSelected": false,
    "wordCount": 450,
    "sceneCount": 5,
    "createdAt": "2025-01-01 10:30:00"
  },
  "success": true
}
```

---

### 6. 更新脚本内容

**接口:** `PUT /scripts/versions/{versionId}`

**描述:** 编辑并更新脚本内容

**请求头:**
```
Authorization: Bearer <token>
```

**路径参数:**
- `versionId` - 脚本版本ID

**请求参数:**
```json
{
  "title": "修改后的标题",
  "content": {
    "title": "修改后的标题",
    "alternativeTitles": ["备选标题1", "备选标题2"],
    "scenes": [...],
    "videoElements": {...},
    "endingCTA": [...]
  }
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "versionId": "770e8400-e29b-41d4-a716-446655440001",
    "updatedAt": "2025-01-01 11:00:00"
  },
  "success": true
}
```

---

### 7. 标记选中脚本

**接口:** `POST /scripts/versions/{versionId}/select`

**描述:** 标记某个脚本方案为选中状态

**请求头:**
```
Authorization: Bearer <token>
```

**路径参数:**
- `versionId` - 脚本版本ID

**响应示例:**
```json
{
  "code": 200,
  "message": "标记成功",
  "data": null,
  "success": true
}
```

---

### 8. 获取生成历史

**接口:** `GET /scripts/sessions`

**描述:** 获取用户的脚本生成历史列表

**请求头:**
```
Authorization: Bearer <token>
```

**查询参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页数量，默认10 |
| videoType | string | 否 | 筛选视频类型 |
| keyword | string | 否 | 搜索关键词（搜索主题） |

**响应示例:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 25,
    "page": 1,
    "pageSize": 10,
    "sessions": [
      {
        "sessionId": "660e8400-e29b-41d4-a716-446655440000",
        "videoType": "knowledge",
        "videoTypeLabel": "知识科普",
        "themeInput": "如何在30天内学会弹吉他",
        "stylePreference": "humorous",
        "stylePreferenceLabel": "幽默风趣",
        "versionCount": 3,
        "selectedVersion": {
          "versionId": "770e8400-e29b-41d4-a716-446655440001",
          "title": "30天吉他速成：从零到英雄"
        },
        "createdAt": "2025-01-01 10:30:00"
      }
    ]
  },
  "success": true
}
```

---

### 9. 获取会话详情

**接口:** `GET /scripts/sessions/{sessionId}`

**描述:** 获取某次生成会话的所有脚本方案

**请求头:**
```
Authorization: Bearer <token>
```

**路径参数:**
- `sessionId` - 会话ID

**响应示例:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "sessionId": "660e8400-e29b-41d4-a716-446655440000",
    "videoType": "knowledge",
    "themeInput": "如何在30天内学会弹吉他",
    "stylePreference": "humorous",
    "createdAt": "2025-01-01 10:30:00",
    "versions": [
      {
        "versionId": "770e8400-e29b-41d4-a716-446655440001",
        "versionIndex": 1,
        "title": "30天吉他速成：从零到英雄",
        "isSelected": true,
        "wordCount": 450,
        "sceneCount": 5
      },
      {
        "versionId": "770e8400-e29b-41d4-a716-446655440002",
        "versionIndex": 2,
        "title": "零基础学吉他：我的真实经验",
        "isSelected": false,
        "wordCount": 480,
        "sceneCount": 6
      }
    ]
  },
  "success": true
}
```

---

## 错误响应示例

### 参数校验失败
```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": {
    "errors": {
      "themeInput": "主题描述不能为空"
    }
  },
  "success": false
}
```

### 未认证
```json
{
  "code": 401,
  "message": "未登录或登录已过期，请重新登录",
  "data": null,
  "success": false
}
```

### 资源不存在
```json
{
  "code": 404,
  "message": "脚本不存在或已被删除",
  "data": null,
  "success": false
}
```

### 服务器错误
```json
{
  "code": 500,
  "message": "服务器内部错误，请稍后重试",
  "data": null,
  "success": false
}
```

---

## 附录

### 视频类型标签映射
```javascript
const VIDEO_TYPE_LABELS = {
  'product_review': '产品测评',
  'knowledge': '知识科普',
  'vlog': 'Vlog日记',
  'comedy': '搞笑剧情',
  'food': '美食制作',
  'makeup': '美妆教程',
  'movie': '影视解说',
  'unboxing': '开箱体验',
  'skill': '技能教学'
};
```

### 风格偏好标签映射
```javascript
const STYLE_LABELS = {
  'humorous': '幽默风趣',
  'professional': '专业严谨',
  'cute': '亲切可爱',
  'passionate': '激情澎湃',
  'emotional': '温情故事',
  'suspenseful': '悬念刺激'
};
```

### HTTP状态码说明
| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未认证/认证失败 |
| 403 | 无访问权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 503 | 服务暂时不可用 |

---

**版本历史:**
- v1.0 (2025-01-01) - 初始版本

