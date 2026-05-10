## Why

M5 内容管理里程碑已完成前后端实现，但缺少完整的自动化测试覆盖。当前仅有 Service 层单元测试和部分手动测试，无法保障 CRUD、导出等核心功能的回归质量。需要通过后端 API 集成测试和前端 Playwright E2E 测试构建完整的自动化测试体系。

## What Changes

### Backend API 集成测试
- 新建 `ContentControllerTest.java`，覆盖所有 REST 端点
- 覆盖列表分页、筛选、详情查询、更新、删除、导出场景
- 验证 `publishedAt` 在状态变更为 PUBLISHED 时自动设置

### Frontend E2E 测试
- 新建 4 个 Playwright spec 文件：`content-list`、`content-preview`、`content-edit`、`content-delete`、`content-export`
- 新建 3 个 Page Object：`ContentListPage`、`ContentPreviewPage`、`ContentEditPage`
- 覆盖从列表到编辑/预览/删除/导出的完整用户流程

### 测试基础设施增强
- 复用现有 Playwright 配置和 BasePage
- 测试数据通过 `@BeforeEach` 自行准备，不依赖外部数据

## Capabilities

### New Capabilities
- `content-management-api-test`: ContentController API 集成测试能力
- `content-management-ui-test`: 内容管理前端 Playwright E2E 测试能力

### Modified Capabilities
- `m5-content-management`: 补充完整的自动化测试覆盖

## Impact

### 影响的代码
- `backend/src/test/java/com/example/visualspider/controller/ContentControllerTest.java` (新建)
- `frontend/tests/e2e/page-objects/ContentListPage.ts` (新建)
- `frontend/tests/e2e/page-objects/ContentPreviewPage.ts` (新建)
- `frontend/tests/e2e/page-objects/ContentEditPage.ts` (新建)
- `frontend/tests/e2e/content-list.spec.ts` (新建)
- `frontend/tests/e2e/content-preview.spec.ts` (新建)
- `frontend/tests/e2e/content-edit.spec.ts` (新建)
- `frontend/tests/e2e/content-delete.spec.ts` (新建)
- `frontend/tests/e2e/content-export.spec.ts` (新建)

### 测试执行方式
- 后端 API 测试：`mvn test -Dtest=ContentControllerTest`
- 前端 E2E 测试：`npm run test:e2e`
- 前端专项测试：`npx playwright test content`

### 依赖项
- Spring Boot Test + MockMvc (已存在)
- Playwright (已存在)
- 测试数据库 H2 (application-test.yml 已配置)
