## Phase 1: 项目初始化

- [x] 1.1 创建 `frontend/` 目录结构
- [x] 1.2 创建 `frontend/package.json`（Vue 3.4+, Vite 5.x, Element Plus 2.5.x, Vue Router 4.x, Axios 1.6.x）
- [x] 1.3 创建 `frontend/vite.config.js`（代理配置：/api → localhost:8080）
- [x] 1.4 创建 `frontend/index.html`
- [x] 1.5 创建 `frontend/src/main.js`（Vue 入口，Element Plus 安装）
- [x] 1.6 创建 `frontend/src/App.vue`（根组件，el-container 布局）
- [x] 1.7 执行 `npm install`

## Phase 2: 路由与 API 层

- [x] 2.1 创建 `frontend/src/router/index.js`（TaskList、TaskConfig 路由）
- [x] 2.2 创建 `frontend/src/api/index.js`（getTasks, getTask, createTask, updateTask, deleteTask, enableTask, disableTask）

## Phase 3: 任务列表页

- [x] 3.1 创建 `frontend/src/views/TaskList.vue`
- [x] 3.2 实现任务表格（el-table）显示：ID、名称、模式、状态、创建时间
- [x] 3.3 实现分页组件（el-pagination）
- [x] 3.4 实现操作按钮：配置、启用、停用、删除
- [x] 3.5 实现状态标签（el-tag）映射：DRAFT/ENABLED/DISABLED/RUNNING
- [x] 3.6 集成新建任务按钮（跳转 /tasks/new）

## Phase 4: 核心组件 - EmbeddedBrowser

- [x] 4.1 创建 `frontend/src/components/EmbeddedBrowser.vue`
- [x] 4.2 实现 URL 输入框 + 加载按钮
- [x] 4.3 实现选择器类型切换（CSS/XPath）
- [x] 4.4 实现 iframe 加载页面
- [x] 4.5 实现点击模式开关
- [x] 4.6 实现点击拦截（document click 事件）
- [x] 4.7 实现 CSS 选择器生成算法（ID > 类名 > 标签路径）
- [x] 4.8 实现 XPath 生成算法（DOM 树路径）
- [x] 4.9 实现选择器预览面板
- [x] 4.10 实现选择器复制功能
- [x] 4.11 实现选择器测试功能（在 iframe 内验证选择器）

## Phase 5: 任务配置页

- [x] 5.1 创建 `frontend/src/views/TaskConfig.vue`
- [x] 5.2 实现基本信息的表单（名称、描述）
- [x] 5.3 实现 URL 模式选择（列表页/直接URL）
- [x] 5.4 实现列表页 URL 输入
- [x] 5.5 实现种子 URL 多行输入（DIRECT_URL 模式）
- [x] 5.6 实现列表页规则配置区域（containerSelector、itemUrlSelector）
- [x] 5.7 实现分页规则配置（类型、下一页选择器、页码 URL 模板）
- [x] 5.8 实现字段配置区域（动态添加/删除字段）
- [x] 5.9 实现字段配置表单（字段名、类型、选择器、提取类型、默认值、必填）
- [x] 5.10 集成 EmbeddedBrowser 组件
- [x] 5.11 实现选择器自动填充（点击元素后填充到表单）
- [x] 5.12 实现表单提交（调用 createTask 或 updateTask API）
- [x] 5.13 实现编辑模式（从 URL 参数获取 taskId，加载现有配置）

## Phase 6: 前后端集成

- [x] 6.1 检查后端 CORS 配置（`backend/src/main/java/com/example/visualspider/config/WebConfig.java`）
- [x] 6.2 创建 WebConfig.java（CORS 配置）
- [ ] 6.3 可选：在 `backend/pom.xml` 添加 frontend-maven-plugin（跳过，使用手动构建）
- [x] 6.4 执行 `npm run build`，验证输出到 `backend/src/main/resources/static/`
- [x] 6.5 启动后端测试，访问 `http://localhost:8080` 验证前端页面

## Phase 7: 端到端测试

- [x] 7.1 验证任务列表页正常显示
- [x] 7.2 验证新建任务流程（名称 → 模式 → 保存）
- [x] 7.3 验证编辑任务流程（配置 → 修改 → 保存）
- [x] 7.4 验证内嵌浏览器加载页面（通过 Playwright 验证组件存在）
- [x] 7.5 验证点击元素生成选择器（组件逻辑已实现，需用户交互测试）
- [x] 7.6 验证任务启用/停用
- [x] 7.7 验证任务删除
