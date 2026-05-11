## Why

M1-M6 已完成核心功能实现，但当前 E2E 测试仅覆盖任务列表、导出等零散页面，缺乏完整用户主链路的端到端验证。核心链路（创建任务→配置规则→执行爬虫→内容管理）需要完整的 E2E 测试覆盖，以确保各模块集成正常、API 契约稳定。

## What Changes

- 新增本地 Mock Target Site（静态 HTML 服务器 + Fixtures），避免依赖外部真实网站
- 新增 E2E-P0 核心链路测试：完整主链路（创建→配置→保存→启用→执行→内容查看）、内容导出 Excel/CSV、内容预览编辑
- 新增 E2E-P1 重要功能测试：分页、任务筛选、执行日志、启用/停用、删除
- 新增 E2E-P2 辅助功能测试：状态筛选、表单验证
- 新增 Manual 验收用例清单：可视化配置体验、richText 渲染、Excel 兼容性、真实网站稳定性、UI 美观
- 配置 Playwright 与前端 dev server 集成，测试稳定可重复

## Capabilities

### New Capabilities

- `e2e-main-flow`: 完整用户主链路 E2E 测试套件，覆盖创建→配置→执行→内容管理全流程
- `e2e-p0-core`: P0 级别核心链路自动化测试（4 个用例）
- `e2e-p1-important`: P1 级别重要功能自动化测试（5 个用例）
- `e2e-p2-optional`: P2 级别辅助功能自动化测试（3 个用例）
- `e2e-manual-checklist`: Manual 验收用例清单（5 个用例，由人工执行）
- `mock-target-site`: 本地 Mock HTML 服务器和 Fixtures，提供稳定可控的测试目标网站

### Modified Capabilities

- （无）

## Impact

### 影响的代码模块

- `frontend/tests/e2e/` - 新增 E2E 测试文件
- `frontend/tests/e2e/page-objects/` - 新增页面对象类
- `frontend/tests/e2e/fixtures/` - 新增 Mock Server 和 HTML Fixtures

### 依赖

- M1-M6 已完成（任务管理、内容管理、爬虫执行、调度）
- 前端 Playwright 已配置（`playwright.config.ts`）
- 后端 `mvn test` 通过（单元测试覆盖 Service/Repository 层）

### API 影响

无新增 API。E2E 测试覆盖现有 API 端点：

| 方法 | 路径 | 覆盖场景 |
|------|------|---------|
| GET | /api/tasks | 任务列表 |
| POST | /api/tasks | 创建任务 |
| PUT | /api/tasks/{id} | 更新任务配置 |
| DELETE | /api/tasks/{id} | 删除任务 |
| POST | /api/tasks/{id}/enable | 启用任务 |
| POST | /api/tasks/{id}/disable | 停用任务 |
| POST | /api/tasks/{id}/run | 手动执行 |
| GET | /api/contents | 内容列表 |
| PUT | /api/contents/{id} | 更新内容 |
| DELETE | /api/contents/{id} | 删除内容 |
| GET | /api/contents/export | 导出内容 |
| GET | /api/executions | 执行日志 |

### 不影响

- 不新增 API 端点
- 不修改业务逻辑
- 不覆盖可视化配置页的 CDP 选择器生成（前端单元测试范围）
- 不覆盖 UI 视觉美观验收
