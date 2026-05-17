# playwright-browser-service

**日期**: 2026-05-17
**状态**: 新增

---

## ADDED Requirements

### Requirement: Session 管理

系统 SHALL 提供 Playwright Session 管理功能，支持创建、关闭、心跳更新。

#### Scenario: 创建 Session
- **WHEN** 前端调用 `POST /api/playwright/sessions` 并传入 URL
- **THEN** 后端启动 Playwright 浏览器实例，加载页面，返回 sessionId

#### Scenario: 关闭 Session
- **WHEN** 前端调用 `DELETE /api/playwright/sessions/{sessionId}`
- **THEN** 后端关闭浏览器实例，移除 Session，返回 204

#### Scenario: 心跳更新
- **WHEN** 前端调用 `POST /api/playwright/sessions/{sessionId}/ping`
- **THEN** 后端更新 Session 最后访问时间，返回 200

#### Scenario: Session 超时清理
- **WHEN** Session 空闲超过 3 分钟无任何操作
- **THEN** 后端自动关闭浏览器实例，移除 Session

#### Scenario: Session 并发限制
- **WHEN** 已存在 5 个活跃 Session 时，前端尝试创建新 Session
- **THEN** 后端返回 409 Conflict，错误信息 "Maximum sessions reached"

### Requirement: 页面导航

系统 SHALL 支持通过 API 导航到指定页面。

#### Scenario: 导航到新页面
- **WHEN** 前端调用 `POST /api/playwright/sessions/{sessionId}/navigate` 并传入新 URL
- **THEN** 后端在 Playwright 浏览器中加载新页面，返回 URL 和页面标题

#### Scenario: 页面加载超时
- **WHEN** 页面加载超过 30 秒未完成
- **THEN** 后端返回 504 Gateway Timeout，错误信息 "Page load timeout"

### Requirement: 截图获取

系统 SHALL 支持获取页面截图。

#### Scenario: 获取全屏截图
- **WHEN** 前端调用 `POST /api/playwright/sessions/{sessionId}/screenshot`（无 selector 参数）
- **THEN** 后端返回当前视口截图，格式为 Base64 编码的 PNG

#### Scenario: 获取元素截图
- **WHEN** 前端调用 `POST /api/playwright/sessions/{sessionId}/screenshot` 并传入 selector
- **THEN** 后端返回指定元素的截图，格式为 Base64 编码的 PNG

### Requirement: 元素信息获取

系统 SHALL 支持获取指定坐标下的元素信息。

#### Scenario: 获取点击位置元素信息
- **WHEN** 前端调用 `POST /api/playwright/sessions/{sessionId}/element` 并传入坐标 (x, y)
- **THEN** 后端移动鼠标到该坐标，返回元素信息（tagName、id、className、textContent、boundingBox）

### Requirement: 选择器测试

系统 SHALL 支持测试 CSS/XPath 选择器的唯一性。

#### Scenario: 测试唯一选择器
- **WHEN** 前端调用 `POST /api/playwright/sessions/{sessionId}/test-selector` 并传入唯一选择器
- **THEN** 后端返回 `unique=true`，`count=1`，以及匹配的元素信息

#### Scenario: 测试非唯一选择器
- **WHEN** 前端调用 `POST /api/playwright/sessions/{sessionId}/test-selector` 并传入匹配多个元素的选择器
- **THEN** 后端返回 `unique=false`，`count>1`，以及最多 10 个匹配元素信息

#### Scenario: 测试无效选择器
- **WHEN** 前端调用 `POST /api/playwright/sessions/{sessionId}/test-selector` 并传入语法错误的 selector
- **THEN** 后端返回 400 Bad Request，错误信息 "Invalid selector syntax"

### Requirement: Session 不存在处理

系统 SHALL 正确处理 Session 不存在的情况。

#### Scenario: Session 不存在
- **WHEN** 前端调用任何 Session 相关 API 时传入不存在的 sessionId
- **THEN** 后端返回 404 Not Found，错误信息 "Session not found: {sessionId}"