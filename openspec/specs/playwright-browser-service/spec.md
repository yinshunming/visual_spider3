# playwright-browser-service

**日期**: 2026-05-17
**状态**: 新增

---

## 1. 概述

Playwright Browser Service 提供基于 Playwright 的浏览器自动化服务，支持会话管理、页面截图、元素信息获取和选择器测试。

## 2. Session 管理

### 2.1 创建 Session

**POST** `/api/playwright/sessions`

Request:
```json
{
  "url": "https://example.com"
}
```

Response:
```json
{
  "sessionId": "uuid",
  "url": "https://example.com",
  "viewport": {
    "width": 1280,
    "height": 720
  }
}
```

### 2.2 关闭 Session

**DELETE** `/api/playwright/sessions/{sessionId}`

Response: 204 No Content

### 2.3 心跳更新

**POST** `/api/playwright/sessions/{sessionId}/ping`

Response: 200 OK

### 2.4 Session 超时清理

Session 空闲超过 3 分钟无任何操作时，后端自动关闭浏览器实例。

### 2.5 Session 并发限制

最多同时存在 5 个活跃 Session。超过时返回 409 Conflict。

## 3. 页面导航

**POST** `/api/playwright/sessions/{sessionId}/navigate`

Request:
```json
{
  "url": "https://newpage.com"
}
```

Response:
```json
{
  "url": "https://newpage.com",
  "title": "Page Title"
}
```

页面加载超过 30 秒时返回 504 Gateway Timeout。

## 4. 截图获取

**POST** `/api/playwright/sessions/{sessionId}/screenshot`

Request:
```json
{
  "selector": "div.content"
}
```

Response:
```json
{
  "data": "base64-encoded-png",
  "selector": "div.content"
}
```

无 selector 参数时返回全屏截图。

## 5. 元素信息获取

**POST** `/api/playwright/sessions/{sessionId}/element`

Request:
```json
{
  "x": 100,
  "y": 200
}
```

Response:
```json
{
  "tagName": "DIV",
  "id": "content",
  "className": "main container",
  "textContent": "Hello World",
  "boundingBox": {
    "x": 100,
    "y": 200,
    "width": 500,
    "height": 300
  }
}
```

## 6. 选择器测试

**POST** `/api/playwright/sessions/{sessionId}/test-selector`

Request:
```json
{
  "selector": "#main h1",
  "type": "CSS"
}
```

Response:
```json
{
  "unique": true,
  "count": 1,
  "elements": [
    {
      "tagName": "H1",
      "id": "title",
      "className": "main-title",
      "textContent": "Welcome"
    }
  ]
}
```

- `unique=true`: 选择器匹配 1 个元素
- `unique=false`: 选择器匹配多个元素，`count>1`
- 语法错误时返回 400 Bad Request

## 7. 错误处理

| 状态码 | 说明 |
|-------|------|
| 400 | 选择器语法错误 |
| 404 | Session 不存在 |
| 409 | 并发超限 |
| 504 | 页面加载超时 |
