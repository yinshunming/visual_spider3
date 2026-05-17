## Context

当前 `EmbeddedBrowser.vue` 使用 iframe 方案加载目标页面，通过 `iframe.contentDocument` 直接访问 DOM。设计文档（M4）已注明此方案的已知限制：iframe 加载外部页面存在跨域限制。

**问题现象**：
- 新浪等大型网站使用多层 iframe 嵌套（广告、评论、分享组件）
- `isSameOrigin` 检测失败，`setupClickInterception()` 不执行
- 用户被迫使用手动输入模式，选择器生成功能失效

**约束条件**：
- 选择器生成逻辑保留在前端（`generateCssSelector()` 和 `generateXPath()` 不变）
- 后端仅负责：截图、元素信息返回、选择器验证
- Session 复用 + 3 分钟超时 + 最多 5 个并发
- 不影响现有任务管理 API 和爬虫执行引擎

## Goals / Non-Goals

**Goals:**
- 解决 iframe 跨域限制，支持任意网站的选择器生成
- 通过后端 Playwright 服务控制真实浏览器
- 前端通过 HTTP API 获取截图和元素信息
- 保留现有选择器生成算法（不做改进）
- 完整自动化测试覆盖

**Non-Goals:**
- 不改进选择器生成算法（保持现有逻辑）
- 不支持多标签页同时操作
- 不实现 Playwright 在爬虫执行引擎中（仍用 Jsoup）
- 不修改数据库 schema

## Decisions

### Decision 1: Playwright 部署方式

**选择**：集成到 Spring Boot 应用（同一进程）

**理由**：
- 项目已是单 Spring Boot 应用，无需额外部署
- 服务间通信变成内部方法调用，延迟低
- 代码集中，易维护

**备选方案**：
- 独立服务：需要单独进程/容器，增加部署复杂度

### Decision 2: Session 管理策略

**选择**：浏览器实例复用 + 前端显式关闭 + 3 分钟超时兜底 + 最多 5 个并发

**理由**：
- 用户通常在同一页面点击多个元素配置字段，频繁启动/关闭体验差
- 超时兜底防止资源泄漏（用户意外关闭浏览器）
- 并发限制防止服务器资源耗尽

### Decision 3: 坐标方案

**选择**：用户点击截图时计算相对于图片的坐标 `(x, y)`，后端执行 `page.mouse.click(x, y)`

**理由**：
- 前端可直接从 `event.offsetX / event.offsetY` 获取坐标
- 实现简单，后端无需维护复杂元素状态
- 对于新浪等结构稳定页面足够可靠

**备选方案**：
- 元素句柄方案：后端返回元素列表，前端选择一个后传回 Handle ID → 过于复杂

### Decision 4: 元素信息获取时机

**选择**：点击时一次性获取（hover 不请求）

**理由**：
- 减少 API 请求频率
- 用户体验足够好（点击后立即看到标记结果）
- 简化实现

### Decision 5: 选择器验证位置

**选择**：后端验证选择器唯一性

**理由**：
- 前端无法直接访问 Playwright 浏览器
- 后端在 Playwright 中执行 `querySelectorAll` 验证
- 前端生成选择器 → 后端验证 → 返回结果

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|-------------|
| Playwright 初始化慢 | 首次创建 Session 等待 2-5 秒 | 应用启动时预热 Playwright |
| 浏览器资源占用 | 服务器内存压力 | 限制最大 Session 数 + 超时清理 |
| Linux 无图形环境 | Chrome 无法启动 | 使用 `--headless` + `--no-sandbox` 参数 |
| 选择器验证性能 | 复杂页面查询慢 | 限制返回元素数量（最多 10 个） |
| Session 并发超限 | 新用户无法创建 Session | 返回 409 Conflict 错误，提示关闭其他 Session |

## API Design

### Session 管理

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/playwright/sessions | 创建 Session（启动浏览器，加载 URL） |
| DELETE | /api/playwright/sessions/{id} | 关闭 Session |
| POST | /api/playwright/sessions/{id}/ping | 心跳（更新最后访问时间） |

### 页面操作

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/playwright/sessions/{id}/navigate | 导航到新页面 |
| POST | /api/playwright/sessions/{id}/screenshot | 获取截图 |

### 元素操作

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/playwright/sessions/{id}/element | 获取点击位置元素信息 |
| POST | /api/playwright/sessions/{id}/test-selector | 测试选择器唯一性 |

## Data Structures

### ElementInfo
```java
public class ElementInfo {
    String tagName;       // "DIV"
    String id;            // "article-title"
    String className;     // "article-title font-18"
    String textContent;  // 文本内容（截断至 200 字符）
    BoundingBox boundingBox;  // {x, y, width, height}
}
```

### SelectorTestResult
```java
public class SelectorTestResult {
    boolean unique;       // 是否唯一匹配
    int count;           // 匹配数量
    List<ElementInfo> elements;  // 匹配的元素列表（最多 10 个）
}
```

## Error Handling

| Scenario | HTTP Status | Error Message |
|----------|-------------|---------------|
| Session 不存在 | 404 | "Session not found: {sessionId}" |
| Session 数量超限 | 409 | "Maximum sessions reached" |
| Playwright 初始化失败 | 500 | "Failed to launch browser" |
| 页面加载超时 | 504 | "Page load timeout" |
| 无效选择器 | 400 | "Invalid selector syntax" |

## Open Questions

1. **是否需要预热 Playwright**？应用启动时初始化一次 Playwright 实例可加速首次使用，但会增加启动时间。建议：可选配置项，默认关闭。

2. **截图压缩质量**？Base64 编码的截图可能较大，建议 JPEG 质量 80%。