# embedded-browser-upgrade

**日期**: 2026-05-17
**状态**: 新增

---

## ADDED Requirements

### Requirement: Session 生命周期管理

前端 EmbeddedBrowser SHALL 管理 Playwright Session 的完整生命周期。

#### Scenario: 创建 Session 并加载页面
- **WHEN** 用户输入 URL 并点击"加载页面"
- **THEN** 前端调用 `POST /api/playwright/sessions` 创建 Session，获取截图并显示

#### Scenario: 关闭 Session
- **WHEN** 用户关闭配置页面或点击"关闭"按钮
- **THEN** 前端调用 `DELETE /api/playwright/sessions/{sessionId}` 关闭 Session

#### Scenario: Session 超时
- **WHEN** Session 空闲超过 3 分钟
- **THEN** 后端自动关闭 Session，前端显示"Session 已过期，请重新加载页面"

#### Scenario: 并发超限提示
- **WHEN** 前端尝试创建 Session 时收到 409 错误
- **THEN** 前端显示错误提示"已达最大并发数，请关闭其他浏览器窗口后重试"

### Requirement: 截图显示

前端 EmbeddedBrowser SHALL 显示后端返回的截图。

#### Scenario: 显示截图
- **WHEN** 前端成功获取截图
- **THEN** 截图显示在浏览器区域，用户可在截图上点击

#### Scenario: 加载中状态
- **WHEN** 前端正在获取截图
- **THEN** 显示加载遮罩和"页面加载中..."文字

#### Scenario: 加载失败
- **WHEN** 页面加载失败（网络错误、超时等）
- **THEN** 显示错误提示"页面加载失败，请检查 URL 是否可访问"

### Requirement: 元素选择

前端 EmbeddedBrowser SHALL 支持用户点击截图选择元素。

#### Scenario: 点击元素生成选择器
- **WHEN** 用户点击截图上的某个元素
- **THEN** 前端计算点击坐标，调用 `POST /api/playwright/sessions/{sessionId}/element` 获取元素信息，然后调用 `generateCssSelector()` 和 `generateXPath()` 生成选择器

#### Scenario: 显示最近点击元素信息
- **WHEN** 用户成功获取元素信息
- **THEN** 在选择器面板显示元素信息（标签、ID、类名、文本内容）

#### Scenario: 测试选择器
- **WHEN** 用户点击"测试"按钮
- **THEN** 前端调用 `POST /api/playwright/sessions/{sessionId}/test-selector`，显示测试结果（唯一/非唯一，匹配数量）

### Requirement: 选择器验证

前端 EmbeddedBrowser SHALL 验证选择器的唯一性。

#### Scenario: 选择器唯一
- **WHEN** 选择器测试返回 `unique=true`
- **THEN** 前端显示绿色提示"✓ 选择器唯一，匹配 1 个元素"

#### Scenario: 选择器不唯一
- **WHEN** 选择器测试返回 `unique=false`
- **THEN** 前端显示黄色提示"⚠ 选择器匹配 {count} 个元素，请优化"

#### Scenario: 选择器无效
- **WHEN** 选择器测试返回 400 错误
- **THEN** 前端显示红色提示"✗ 选择器语法错误：{errorMessage}"

### Requirement: 导航功能

前端 EmbeddedBrowser SHALL 支持页面导航。

#### Scenario: 导航到新页面
- **WHEN** 用户在 URL 输入框输入新 URL 并点击"加载页面"
- **THEN** 前端调用 `POST /api/playwright/sessions/{sessionId}/navigate`，刷新截图

#### Scenario: 刷新页面
- **WHEN** 用户点击"刷新"按钮
- **THEN** 前端重新调用 `/screenshot` 获取最新截图

### Requirement: 选择器复制

前端 EmbeddedBrowser SHALL 支持复制生成的选择器。

#### Scenario: 复制 CSS 选择器
- **WHEN** 用户点击"复制 CSS"按钮
- **THEN** 选择器复制到剪贴板，显示成功提示

#### Scenario: 复制 XPath
- **WHEN** 用户点击"复制 XPath"按钮
- **THEN** XPath 复制到剪贴板，显示成功提示