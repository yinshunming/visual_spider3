# e2e-main-flow-coverage Tasks

## 1. Mock Target Site 搭建

- [x] 1.1 创建 `frontend/tests/e2e/fixtures/mock-server/` 目录结构
- [x] 1.2 创建 `list-page.html` Fixture（包含 div.article-list > div.item 和 a.title）
- [x] 1.3 创建 `content-page-1.html` Fixture（包含 h1.title 和 div.article-content）
- [x] 1.4 创建 `content-page-2.html` Fixture
- [x] 1.5 创建 `server.js` Mock HTTP Server（Node.js built-in http）
- [x] 1.6 配置 Mock Server 动态端口分配
- [x] 1.7 验证 Mock Server 可正常启动并响应请求

## 2. 测试数据 Seed 基础设施

- [x] 2.1 创建 `frontend/tests/e2e/fixtures/seed.ts`
- [x] 2.2 实现 `createTask()` 函数（通过 API 创建测试任务）
- [x] 2.3 实现 `deleteTask()` 函数（清理测试任务）
- [x] 2.4 注意：ContentItem 无 POST API，内容由爬虫执行后生成

## 3. P0 核心链路测试实现

- [x] 3.1 实现 `specs/p0/e2e-main-flow.spec.ts` 完整主链路测试
- [x] 3.2 实现 `specs/p0/e2e-export.spec.ts` 导出测试
- [x] 3.3 实现 `specs/p0/e2e-preview-edit.spec.ts` 预览编辑测试

## 4. P1 重要功能测试实现

- [x] 4.1 实现 `specs/p1/e2e-pagination.spec.ts` - E2E-P1-01 分页测试
- [x] 4.2 实现 `specs/p1/e2e-filter.spec.ts` - E2E-P1-02 任务筛选测试
- [x] 4.3 实现 `specs/p1/e2e-execution-log.spec.ts` - E2E-P1-03 执行日志测试
- [x] 4.4 实现 `specs/p1/e2e-task-ops.spec.ts` - E2E-P1-04/05 启用停用删除测试

## 5. P2 辅助功能测试实现

- [x] 5.1 实现 `specs/p2/e2e-auxiliary.spec.ts`
  - E2E-P2-01 内容删除测试
  - E2E-P2-02 表单验证测试
  - E2E-P2-03 任务创建必填验证

## 6. 新增 Page Objects

- [x] 6.1 无需新增 ExecutionLogPage（使用 API 直接验证）
- [x] 6.2 现有 TaskConfigPage、ContentListPage 足以支持测试
- [x] 6.3 ContentListPage 已支持 selectTaskFilter、clearTaskFilter 方法

## 7. Manual 验收清单

- [x] 7.1 创建 `frontend/tests/e2e/manual/checklist.md`
- [x] 7.2 列出 M-01 ~ M-05 人工验收用例（可视化配置、richText渲染、Excel兼容性、真实网站稳定性、UI美观）
- [x] 7.3 提供验收步骤和预期结果

## 8. 测试配置和 CI

- [x] 8.1 更新 `playwright.config.ts` 配置 testMatch 排除 manual 目录
- [x] 8.2 创建 `.github/workflows/e2e.yml` GitHub Actions workflow
- [ ] 8.3 验证 `npm run test:e2e` 可正常运行（需要 backend 运行）

## 9. 验收确认

- [ ] 9.1 所有 P0 测试通过（需要 backend + frontend 运行）
- [ ] 9.2 所有 P1 测试通过（需要 backend + frontend 运行）
- [ ] 9.3 所有 P2 测试通过（需要 backend + frontend 运行）
- [x] 9.4 Manual 验收清单完整（已创建 checklist.md）
- [ ] 9.5 E2E 测试稳定可重复（需要多次运行验证）
