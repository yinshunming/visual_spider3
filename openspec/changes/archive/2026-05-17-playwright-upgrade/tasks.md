## 1. 后端依赖与配置

- [x] 1.1 在 `pom.xml` 添加 Playwright 依赖 `com.microsoft.playwright:playwright:1.40.0`
- [x] 1.2 创建 `PlaywrightConfig.java` 配置类（浏览器参数、超时配置）
- [x] 1.3 创建 `PlaywrightProperties.java` 配置属性类（最大 Session 数、超时时间等）

## 2. 后端 DTO

- [x] 2.1 创建 `SessionCreateRequest.java`（url 字段）
- [x] 2.2 创建 `SessionResponse.java`（sessionId、url、viewport）
- [x] 2.3 创建 `NavigateRequest.java`（url 字段）
- [x] 2.4 创建 `NavigateResponse.java`（url、title）
- [x] 2.5 创建 `ScreenshotRequest.java`（selector 可选）
- [x] 2.6 创建 `ScreenshotResponse.java`（base64 编码的图片数据、width、height）
- [x] 2.7 创建 `ElementInfoRequest.java`（x、y 坐标）
- [x] 2.8 创建 `ElementInfoResponse.java`（tagName、id、className、textContent、boundingBox）
- [x] 2.9 创建 `TestSelectorRequest.java`（selector、type）
- [x] 2.10 创建 `TestSelectorResponse.java`（unique、count、elements）
- [x] 2.11 创建 `ErrorResponse.java`（errorCode、message）

## 3. PlaywrightBrowserService

- [x] 3.1 创建 `PlaywrightBrowserService.java` 服务类
- [x] 3.2 实现 Session 管理（创建、关闭，心跳、清理）
- [x] 3.3 实现 `createSession(url)` 方法
- [x] 3.4 实现 `closeSession(sessionId)` 方法
- [x] 3.5 实现 `getScreenshot(sessionId, selector)` 方法
- [x] 3.6 实现 `getElementAt(sessionId, x, y)` 方法
- [x] 3.7 实现 `testSelector(sessionId, selector, type)` 方法
- [x] 3.8 实现 `navigate(sessionId, url)` 方法
- [x] 3.9 实现超时清理调度线程
- [x] 3.10 实现 `@PreDestroy` 关闭所有浏览器实例

## 4. PlaywrightController

- [x] 4.1 创建 `PlaywrightController.java`
- [x] 4.2 实现 `POST /api/playwright/sessions`（创建 Session）
- [x] 4.3 实现 `DELETE /api/playwright/sessions/{id}`（关闭 Session）
- [x] 4.4 实现 `POST /api/playwright/sessions/{id}/ping`（心跳）
- [x] 4.5 实现 `POST /api/playwright/sessions/{id}/navigate`（导航）
- [x] 4.6 实现 `POST /api/playwright/sessions/{id}/screenshot`（截图）
- [x] 4.7 实现 `POST /api/playwright/sessions/{id}/element`（元素信息）
- [x] 4.8 实现 `POST /api/playwright/sessions/{id}/test-selector`（选择器测试）
- [x] 4.9 实现统一异常处理（404、409、400、500、504）

## 5. 前端 API 模块

- [x] 5.1 创建 `frontend/src/api/playwright.js` 文件
- [x] 5.2 实现 `createSession(url)` API 调用
- [x] 5.3 实现 `closeSession(sessionId)` API 调用
- [x] 5.4 实现 `pingSession(sessionId)` API 调用
- [x] 5.5 实现 `navigate(sessionId, url)` API 调用
- [x] 5.6 实现 `getScreenshot(sessionId, selector)` API 调用
- [x] 5.7 实现 `getElementAt(sessionId, x, y)` API 调用
- [x] 5.8 实现 `testSelector(sessionId, selector, type)` API 调用

## 6. EmbeddedBrowser.vue 重构

- [x] 6.1 移除 iframe 相关代码（browserFrame ref、isSameOrigin 状态）
- [x] 6.2 移除 `setupClickInterception()` 和 `handleIframeClick()` 方法
- [x] 6.3 移除 `onIframeLoad()` 和 `onIframeError()` 方法
- [x] 6.4 新增 Session 状态管理（sessionId、isLoading）
- [x] 6.5 实现截图显示（`<img>` 标签 + 坐标计算）
- [x] 6.6 实现点击截图获取元素信息
- [x] 6.7 实现选择器生成（保留现有 `generateCssSelector()` 和 `generateXPath()`）
- [x] 6.8 实现选择器测试（调用后端 API 显示结果）
- [x] 6.9 实现 Session 超时处理（显示错误提示）
- [x] 6.10 实现 `defineExpose()` 暴露的方法（不变）

## 7. 后端单元测试

- [x] 7.1 创建 `PlaywrightBrowserServiceTest.java`
- [x] 7.2 测试 Session 创建和关闭
- [x] 7.3 测试 Session 并发限制（最多 5 个）
- [x] 7.4 测试 Session 超时清理
- [x] 7.5 测试截图获取
- [x] 7.6 测试元素信息获取
- [x] 7.7 测试选择器测试（唯一/非唯一/无效）
- [x] 7.8 测试导航功能

## 8. 后端 API 集成测试

- [x] 8.1 创建 `PlaywrightControllerTest.java`
- [x] 8.2 测试 Session 创建 API（201 Created）
- [x] 8.3 测试 Session 关闭 API（204 No Content）
- [x] 8.4 测试心跳 API（200 OK）
- [x] 8.5 测试导航 API（200 OK）
- [x] 8.6 测试截图 API（200 OK + Base64 图片）
- [x] 8.7 测试元素信息 API（200 OK + 元素数据）
- [x] 8.8 测试选择器测试 API（200 OK + 验证结果）
- [x] 8.9 测试 Session 不存在（404 Not Found）
- [x] 8.10 测试并发超限（409 Conflict）
- [x] 8.11 测试无效选择器（400 Bad Request）

## 9. 前端 E2E 测试

- [x] 9.1 创建 `frontend/tests/e2e/playwright-browser.spec.ts`
- [x] 9.2 测试加载页面创建 Session
- [x] 9.3 测试截图显示
- [x] 9.4 测试点击元素生成选择器
- [x] 9.5 测试选择器复制功能
- [x] 9.6 测试选择器测试功能
- [x] 9.7 测试页面导航
- [x] 9.8 测试刷新页面
- [x] 9.9 测试关闭 Session
- [x] 9.10 测试 Session 超时提示
- [x] 9.11 测试并发超限提示

## 10. 集成测试

- [x] 10.1 创建 `frontend/tests/e2e/playwright-integration.spec.ts`
- [x] 10.2 验证 Playwright 浏览器可以启动
- [x] 10.3 使用新浪页面测试完整流程
- [x] 10.4 验证选择器生成、测试、保存全流程
- [x] 10.5 验证并发 Session 限制
- [x] 10.6 验证超时清理机制