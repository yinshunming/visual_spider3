## Why

M4 可视化配置前端已完成，但缺乏端到端（E2E）测试覆盖。前端仅有 API 模块的单元测试（Vitest），无法验证完整的用户交互流程和组件集成。需要引入 Playwright E2E 测试框架，为前端提供完整的自动化测试能力。

## What Changes

- 引入 Playwright 测试框架配置
- 创建任务列表页 E2E 测试
- 创建任务创建页 E2E 测试
- 创建任务配置页 E2E 测试
- 配置 Vite dev server 与 Playwright 集成

## Capabilities

### New Capabilities

- `e2e-testing`: Playwright E2E 测试框架，覆盖任务管理的核心用户流程
- `task-list-e2e`: 任务列表页加载、渲染、数据展示测试
- `task-create-e2e`: 任务创建表单提交和验证测试
- `task-config-e2e`: 任务配置页编辑和保存测试

### Modified Capabilities

- 无

## Impact

- 新增依赖：`@playwright/test`
- 测试文件位置：`frontend/tests/e2e/`
- 配置文件：`playwright.config.ts`
