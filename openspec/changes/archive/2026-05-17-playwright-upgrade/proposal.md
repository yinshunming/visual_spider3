## Why

当前 `EmbeddedBrowser.vue` 使用 iframe 方案存在跨域限制，当目标网站（如新浪 k.sina.com.cn）使用多层 iframe 嵌套（广告、评论、分享组件等）时，`iframe.contentDocument` 访问失败，导致选择器生成功能完全失效。用户只能被迫使用粗糙的手动输入模式，无法完成可视化配置。

升级为 Playwright Persistent Context 方案可彻底解决跨域问题，通过后端 Playwright 服务控制真实浏览器，前端通过 HTTP API 获取截图和元素信息，实现对任意网站的选择器生成支持。

## What Changes

**新增功能**：
- 后端 Playwright 浏览器服务，支持创建/关闭 Session、页面导航、截图、元素信息获取、选择器测试
- 前端 EmbeddedBrowser.vue 重构，移除 iframe，改用截图 + 坐标点击模式
- Session 管理：浏览器实例复用、3 分钟超时、最多 5 个并发
- 完整自动化测试覆盖（后端单元测试 + 前端 E2E 测试）

**修改功能**：
- `EmbeddedBrowser.vue`：重构核心交互逻辑，从 iframe 直接访问改为 API 调用模式

**测试覆盖**：
- PlaywrightBrowserService 单元测试
- PlaywrightController API 测试
- EmbeddedBrowser E2E 测试（完整主链路）

## Capabilities

### New Capabilities

- `playwright-browser-service`: 后端 Playwright 浏览器服务，负责浏览器实例生命周期管理、页面操作 API、元素查询与选择器验证
- `embedded-browser-upgrade`: 前端 EmbeddedBrowser 重构，截图显示 + 坐标点击交互模式，Session 状态管理

### Modified Capabilities

- `visual-config`: 可视化配置流程的交互方式改变（iframe → 截图+API），但核心功能不变

## Impact

**后端**：
- 新增 `PlaywrightBrowserService.java`（~250 行）
- 新增 `PlaywrightController.java`（~150 行）
- 新增 DTO 类（~80 行）
- 新增 `pom.xml` 依赖：`com.microsoft.playwright:playwright:1.40.0`

**前端**：
- 重构 `EmbeddedBrowser.vue`（~600 行）
- 新增 `api/playwright.js`（~100 行）

**测试**：
- 新增后端单元测试：PlaywrightBrowserServiceTest.java
- 新增后端集成测试：PlaywrightControllerTest.java
- 新增前端 E2E 测试：playwright-browser.spec.ts

**不影响**：
- 任务管理 API（SpiderTaskController）
- 爬虫执行引擎（CrawlerEngine）
- 内容管理 API（ContentController）
- 数据库 schema