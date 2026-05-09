## Context

M4 可视化配置需要在 M2 任务管理的基础上，提供可视化配置界面。根据 visual-spider-spec.md，核心架构决策是：

- **选择器生成由前端 EmbeddedBrowser.vue 通过 Playwright/CDP 直接完成，后端不参与**
- 后端只需存储最终配置（通过 M2 的 SpiderTaskController CRUD 接口）
- 前端独立运行（开发时 Vite 端口 3000，代理到后端 8080）

## Goals / Non-Goals

**Goals:**
- 创建 Vue3 前端项目结构
- 实现任务列表页面（TaskList.vue）
- 实现任务配置页面（TaskConfig.vue）
- 实现 EmbeddedBrowser.vue 组件（内嵌浏览器 + 选择器生成）
- 实现前后端集成部署

**Non-Goals:**
- 不实现内容管理页面（ContentManage.vue - M5）
- 不实现定时调度界面（M6）
- 不升级选择器生成算法（保持简单 CSS/XPath 路径算法）
- 不实现生产级 Playwright 集成（使用 iframe 方案，有跨域限制）

## Technical Architecture

### Frontend Stack

| 组件 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 | 3.4+ |
| 构建 | Vite | 5.x |
| UI 库 | Element Plus | 2.5.x |
| 路由 | Vue Router | 4.x |
| HTTP | Axios | 1.6.x |
| 内嵌浏览器 | iframe + CDP | - |

### Directory Structure

```
frontend/
├── index.html
├── vite.config.js          # 代理配置到 localhost:8080
├── package.json
└── src/
    ├── main.js             # Vue 入口
    ├── App.vue             # 根组件
    ├── router/
    │   └── index.js        # 路由配置
    ├── api/
    │   └── index.js        # 后端 API 调用
    ├── views/
    │   ├── TaskList.vue    # 任务列表页
    │   └── TaskConfig.vue  # 任务配置页
    └── components/
        └── EmbeddedBrowser.vue  # 内嵌浏览器组件
```

### Backend Integration

- **开发模式**: Vite dev server（端口 3000）代理 `/api` 到 `localhost:8080`
- **生产模式**: `npm run build` 输出到 `backend/src/main/resources/static/`
- **CORS**: 如果 `backend/src/main/java/com/example/visualspider/config/WebConfig.java` 不存在则创建

### EmbeddedBrowser Component Design

**状态管理:**
```javascript
{
  currentUrl: '',        // 当前加载的 URL
  isLoading: false,      // 加载状态
  selector: '',          // 生成的选择器
  selectorType: 'CSS',  // CSS | XPATH
  enableClickMode: false // 是否启用点击选择模式
}
```

**CDP 选择器生成流程:**
1. 用户点击 iframe 内的页面元素
2. 事件冒泡到 document，拦截 click 事件
3. 生成 CSS 选择器（基于 ID > 类名 > 标签路径）
4. 生成 XPath（基于 DOM 树路径）
5. 显示在选择器预览面板

**已知限制:**
- iframe 加载外部页面存在跨域限制
- 选择器生成算法较简单（仅支持基础路径算法）
- 后续可升级为 Playwright Persistent Context 方案

## Data Flow

### 任务创建/更新流程

```
用户操作 → TaskConfig.vue → API 调用 → SpiderTaskController
                                              ↓
                                      SpiderTaskService
                                              ↓
                                       PostgreSQL
```

### 选择器生成流程

```
用户点击页面元素 → EmbeddedBrowser → 生成选择器 → TaskConfig.vue
                                                       ↓
                                                  API 存储
```

## API Contract

后端 API（M2 已实现）:

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/tasks | 分页查询任务列表 |
| GET | /api/tasks/{id} | 获取任务详情 |
| POST | /api/tasks | 创建任务（含字段配置） |
| PUT | /api/tasks/{id} | 更新任务配置 |

**关键**: 列表页规则和内容页规则通过 `list_page_rule` 和 `content_page_rule` JSONB 字段存储。

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| iframe 跨域限制 | 生产环境使用后端代理或 Playwright Persistent Context |
| 选择器生成不精准 | 保持简单算法，提供手动编辑能力 |
| 前端构建复杂度 | 保持最小化配置，使用标准 Vite 模板 |
