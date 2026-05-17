# embedded-browser-upgrade

**日期**: 2026-05-17
**状态**: 新增

---

## 1. 概述

EmbeddedBrowser 组件从前端 iframe 方案升级为 Playwright Persistent Context，支持跨域页面（如 sina.com.cn）。

## 2. Session 生命周期管理

### 2.1 创建 Session 并加载页面

1. 用户输入 URL 并点击"加载页面"
2. 前端调用 `POST /api/playwright/sessions` 创建 Session
3. 后端启动 Playwright 浏览器，加载页面
4. 前端获取截图并显示

### 2.2 关闭 Session

- 用户关闭配置页面或点击"关闭"按钮
- 前端调用 `DELETE /api/playwright/sessions/{sessionId}` 关闭 Session

### 2.3 Session 超时

- Session 空闲超过 3 分钟
- 后端自动关闭 Session
- 前端显示"Session 已过期，请重新加载页面"

### 2.4 并发超限提示

- 前端尝试创建 Session 时收到 409 错误
- 显示错误提示"已达最大并发数，请关闭其他浏览器窗口后重试"

## 3. 截图显示

### 3.1 显示截图

- 前端成功获取截图后显示在浏览器区域
- 用户可在截图上点击选择元素

### 3.2 加载中状态

- 正在获取截图时显示加载遮罩和"页面加载中..."文字

### 3.3 加载失败

- 网络错误、超时等情况下显示错误提示
- "页面加载失败，请检查 URL 是否可访问"

## 4. 元素选择

### 4.1 点击元素生成选择器

1. 用户点击截图上的某个元素
2. 前端计算点击坐标
3. 调用 `POST /api/playwright/sessions/{sessionId}/element` 获取元素信息
4. 调用 `generateCssSelector()` 和 `generateXPath()` 生成选择器
5. 在选择器面板显示 CSS 和 XPath 选择器

### 4.2 显示元素信息

成功获取元素信息后显示：
- 标签名 (tagName)
- ID
- 类名 (className)
- 文本内容 (textContent)

### 4.3 测试选择器

1. 用户点击"测试"按钮
2. 前端调用 `POST /api/playwright/sessions/{sessionId}/test-selector`
3. 显示测试结果

## 5. 选择器验证

### 5.1 选择器唯一

- 测试返回 `unique=true`
- 显示绿色提示"✓ 选择器唯一，匹配 1 个元素"

### 5.2 选择器不唯一

- 测试返回 `unique=false`
- 显示黄色提示"⚠ 选择器匹配 {count} 个元素，请优化"

### 5.3 选择器无效

- 测试返回 400 错误
- 显示红色提示"✗ 选择器语法错误：{errorMessage}"

## 6. 导航功能

### 6.1 导航到新页面

1. 用户在 URL 输入框输入新 URL
2. 点击"加载页面"
3. 前端调用 `POST /api/playwright/sessions/{sessionId}/navigate`
4. 刷新截图

### 6.2 刷新页面

- 用户点击"刷新"按钮
- 前端重新调用 `/screenshot` 获取最新截图

## 7. 选择器复制

### 7.1 复制 CSS 选择器

- 用户点击"复制 CSS"按钮
- CSS 选择器复制到剪贴板
- 显示成功提示

### 7.2 复制 XPath

- 用户点击"复制 XPath"按钮
- XPath 复制到剪贴板
- 显示成功提示
