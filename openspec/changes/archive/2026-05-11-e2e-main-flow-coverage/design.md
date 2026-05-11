## Context

Visual Spider 项目已完成 M1-M6 核心功能，包括任务管理、爬虫执行、内容管理、调度执行。当前 E2E 测试仅覆盖部分页面，缺少完整主链路的端到端验证。

**现状**：
- `frontend/tests/e2e/` 已存在部分测试文件（task-list.spec.ts、content-export.spec.ts 等）
- 现有测试未覆盖：完整主链路串联、任务执行后状态变化、ExecutionLog 查看、内容状态筛选
- 测试数据依赖外部真实网站，不稳定

**约束**：
- E2E 测试需使用 Playwright（前端已配置）
- 不修改后端业务逻辑
- 不新增 API
- 测试需稳定可重复，不依赖外部网站

## Goals / Non-Goals

**Goals:**
- 实现 P0 核心链路完整自动化（4 个用例）
- 实现 P1 重要功能自动化（5 个用例）
- 实现 P2 辅助功能自动化（3 个用例）
- 提供 Manual 验收用例清单（5 个用例）
- 建立 Mock Target Site，确保测试稳定可重复

**Non-Goals:**
- 不覆盖可视化配置页的 CDP 选择器生成（前端单元测试范围）
- 不覆盖真实第三方网站的爬取稳定性
- 不覆盖 UI 视觉美观验收
- 不覆盖 Excel/WPS 打开体验（Manual 验收）
- 不覆盖性能/压力测试

## Decisions

### Decision 1: Mock Target Site 架构

**选择**: 本地 Mock HTTP Server + 静态 HTML Fixtures

**理由**:
- 完全可控，不受外部网站变更影响
- 可模拟各种 HTML 结构（正常、异常、边界）
- 测试稳定，可重复执行

**实现**:
```
frontend/tests/e2e/fixtures/
├── mock-server/
│   ├── server.js          # Express Mock HTTP Server
│   ├── list-page.html     # 列表页 HTML
│   ├── content-page-1.html # 内容页 1
│   └── content-page-2.html # 内容页 2
```

**替代方案考虑**:
- 使用 real website（如新浪体育）→ 缺点：不稳定，可能被封禁
- 使用 file:// 协议直接加载 HTML → 缺点：无法模拟完整 HTTP 请求上下文

### Decision 2: 测试数据准备策略

**选择**: Database Seed + Mock Server 组合

**理由**:
- 部分测试（如导出、编辑）需要预先存在数据
- E2E-P0-01 完整链路会自己创建数据
- Seed data 供其他测试复用

**实现**:
- 创建 `tests/e2e/fixtures/seed.ts`，提供 `seedDatabase()` 函数
- 在 `beforeAll` 中调用，确保测试数据就绪
- 测试结束后清理数据

### Decision 3: 测试文件结构

**选择**: Page Object 模式 + Spec 文件分离

**理由**:
- 页面元素定位集中管理，UI 变化时易维护
- Spec 文件专注于测试逻辑，易读
- 复用现有 `page-objects/` 目录结构

**实现**:
```
frontend/tests/e2e/
├── fixtures/
│   ├── mock-server/       # Mock 服务器
│   └── seed.ts            # 测试数据 seed
├── page-objects/
│   ├── BasePage.ts        # 已存在
│   ├── TaskListPage.ts    # 已存在
│   ├── TaskConfigPage.ts  # 已存在
│   ├── ContentListPage.ts # 已存在
│   ├── ContentEditPage.ts # 已存在
│   ├── ExecutionLogPage.ts # 新增
│   └── MockSitePage.ts    # 新增
├── specs/
│   ├── p0/                # 新增目录
│   │   ├── e2e-main-flow.spec.ts
│   │   ├── e2e-export.spec.ts
│   │   └── e2e-preview-edit.spec.ts
│   ├── p1/
│   │   ├── e2e-pagination.spec.ts
│   │   ├── e2e-filter.spec.ts
│   │   └── e2e-task-ops.spec.ts
│   └── p2/
│       └── e2e-auxiliary.spec.ts
└── manual/
    └── checklist.md       # Manual 验收清单
```

### Decision 4: 主链路测试设计

**E2E-P0-01 完整主链路测试流程**:

```
1. 启动 Mock Server（listen on random available port）
2. 创建任务（POST /api/tasks）
   → 验证 status = DRAFT
3. 更新任务配置（PUT /api/tasks/:id）
   → 配置 listPageUrl = mockServerUrl + /list.html
   → 配置 listPageRule: containerSelector, itemUrlSelector
   → 配置 contentPageRule: title, content 字段
4. 启用任务（POST /api/tasks/:id/enable）
   → 验证 status = ENABLED
5. 执行任务（POST /api/tasks/:id/run）
   → 验证立即返回 200
6. 轮询任务状态（GET /api/tasks/:id）
   → 等待 status 从 RUNNING 变为 ENABLED
   → 验证 crawler 执行完成
7. 验证内容（GET /api/contents?taskId=:id）
   → 验证内容条数 >= 2
   → 验证字段包含 title, content
8. 清理 Mock Server
```

**超时设置**: 任务执行轮询最多 30 秒（10 次 × 3 秒间隔）

### Decision 5: 导出测试设计

**E2E-P0-02/03 导出测试**:

```typescript
test('E2E-P0-02 导出 Excel', async ({ page }) => {
  // 1. 确保有测试数据（seed 或依赖 E2E-P0-01）
  // 2. 访问 /contents
  // 3. 点击导出按钮
  const downloadPromise = page.waitForEvent('download')
  await page.getByRole('button', { name: /导出/i }).click()
  const download = await downloadPromise

  // 4. 验证文件
  expect(download.suggestedFilename()).toMatch(/\.xlsx$/)
  const path = await download.path()

  // 5. 解析 Excel 验证内容（使用 xlsx 库）
  const workbook = XLSX.readFile(path)
  const sheet = workbook.Sheets[workbook.SheetNames[0]]
  const data = XLSX.utils.sheet_to_json(sheet)

  expect(data.length).toBeGreaterThan(0)
  // 验证列名包含关键字段
  expect(Object.keys(data[0])).toContain('title')
})
```

**依赖**: `xlsx` npm 包（用于解析 Excel 文件）

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Mock Server 端口被占用 | 使用 `0` port 动态分配，测试前通过 `fetch(mockUrl)` 验证可用 |
| 任务执行时间不确定导致超时 | 轮询间隔 3 秒，最多 10 次（30 秒超时） |
| 导出文件解析复杂 | 使用成熟库 `xlsx`，仅验证行数列名，不验证格式美观 |
| 测试数据残留影响其他测试 | `afterEach` 清理创建的数据，或使用独立 taskId |
| Playwright 与 dev server 启动竞争 | `playwright.config.ts` 已配置 `webServer.reuseExistingServer` |

## Open Questions

1. **ExecutionLog 前端页面是否存在？** 当前 `ExecutionController` 已实现，但前端是否有执行日志查看页面待确认
2. **内容状态筛选前端是否有 UI？** ContentList.vue 目前只有 taskId 筛选，无 status 筛选（需确认是否需要实现）
3. **Mock Server 是否需要启动脚本？** 考虑使用 `globalSetup` 在所有测试前启动一次

---

## Manual 验收清单

以下用例不适合自动化，由人工执行验收：

| 编号 | 场景 | 验收要点 |
|-----|------|---------|
| M-01 | 可视化配置体验 | CDP 选择器生成准确性、选择器手动调整体验 |
| M-02 | richText 预览渲染 | HTML 内容在预览对话框中的样式、中文显示 |
| M-03 | Excel 打开体验 | 不同 Office 版本（WPS/Office 2016/2019/365）打开兼容性 |
| M-04 | 真实网站稳定性 | 爬取真实网站（如新浪体育）的选择器是否失效、频率限制应对 |
| M-05 | UI 美观度 | 页面布局、颜色、字体是否符合设计规范 |
