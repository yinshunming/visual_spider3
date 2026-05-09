## Why

M4 里程碑需要实现可视化配置能力，允许运营人员通过内嵌浏览器点击页面元素，自动生成 CSS/XPath 选择器，无需手动编写复杂的选择器表达式。这是系统的核心用户体验功能。

根据 visual-spider-spec.md 的架构决策，选择器生成由前端通过 Playwright/CDP 直接完成，后端不参与选择器生成过程。

## What Changes

- 创建 Vue3 前端项目（frontend/ 目录）
- 实现任务管理界面（TaskList.vue、TaskConfig.vue）
- 实现 EmbeddedBrowser.vue 组件（内嵌浏览器 + CDP 选择器生成）
- 前后端集成部署（前端静态文件放入 Spring Boot）

## Capabilities

### New Capabilities

- `visual-config`: 可视化配置界面，通过内嵌浏览器点击生成选择器
- `embedded-browser`: EmbeddedBrowser.vue 组件，支持 URL 加载、元素选择、选择器预览

### Modified Capabilities

- `task-management`: 扩展前端界面，提供可视化配置入口
- `frontend-structure`: 新增 Vue3 + Vite + Element Plus 前端项目

## Impact

- 新增 `frontend/` 目录 - Vue3 前端项目
- 修改 `backend/src/main/resources/static/` - 存放前端构建产物
- 修改 `backend/pom.xml` - 可选：添加前端构建插件
- 新增 `WebConfig.java` - CORS 配置（如果不存在）

## Dependencies

- M2 任务管理（已完成）- 依赖 SpiderTaskController CRUD API
- M3 爬虫核心（已完成）- 依赖内容页解析规则结构
