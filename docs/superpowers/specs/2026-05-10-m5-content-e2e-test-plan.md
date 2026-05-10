# M5 内容管理 E2E 测试计划

**日期**: 2026-05-10
**状态**: 计划阶段
**覆盖范围**: m5-content-management 里程碑的端到端测试

---

## 1. 测试分层架构

```
┌─────────────────────────────────────────────────────────────┐
│                    E2E 测试层 (Playwright)                   │
│  前端页面交互 → HTTP 请求 → 后端 Controller → Service → DB   │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                   API 集成测试层 (Spring Boot Test)          │
│        直接调用 Controller → Service → Repository           │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. 第一层：后端 API 集成测试

### 2.1 测试文件位置
```
backend/src/test/java/com/example/visualspider/controller/
├── ContentControllerTest.java      # 新建
```

### 2.2 测试用例清单

#### 2.2.1 内容列表查询 `GET /api/contents`

| 用例 ID | 场景 | 预期结果 | 数据库验证 |
|---------|------|----------|------------|
| API-001 | 分页查询默认第一页 | 返回 Page，内容条数=size | - |
| API-002 | 指定 page=1&size=5 | 返回第2页，最多5条 | - |
| API-003 | 按 taskId 筛选 | 只返回该 taskId 的内容 | SQL: WHERE task_id=? |
| API-004 | 按 status 筛选 | 只返回该状态的内容 | SQL: WHERE status=? |
| API-005 | 按 taskId+status 组合筛选 | 返回同时满足条件的内容 | SQL: WHERE task_id=? AND status=? |
| API-006 | 查询不存在的内容 | 返回空 Page（非 404） | - |

#### 2.2.2 内容详情查询 `GET /api/contents/{id}`

| 用例 ID | 场景 | 预期结果 | 数据库验证 |
|---------|------|----------|------------|
| API-010 | 获取存在的内容 | 返回 ContentResponse，fields 解析为 Map | - |
| API-011 | 获取不存在的内容 | 抛出 RuntimeException → 404 | - |
| API-012 | fields 为 null 时 | 返回空 Map 而非 null | - |

#### 2.2.3 内容更新 `PUT /api/contents/{id}`

| 用例 ID | 场景 | 预期结果 | 数据库验证 |
|---------|------|----------|------------|
| API-020 | 仅更新 fields | fields 更新，status 不变，createdAt 不变 | fields 字段更新 |
| API-021 | 仅更新 status PENDING→PUBLISHED | status 变更，publishedAt 自动设置为当前时间 | published_at = NOW() |
| API-022 | 仅更新 status PUBLISHED→DELETED | status 变更，publishedAt 不变 | - |
| API-023 | 同时更新 fields + status | 两者都更新，publishedAt 按规则设置 | - |
| API-024 | 更新不存在的内容 | 抛出 RuntimeException → 404 | - |

#### 2.2.4 内容删除 `DELETE /api/contents/{id}`

| 用例 ID | 场景 | 预期结果 | 数据库验证 |
|---------|------|----------|------------|
| API-030 | 删除存在的内容 | 返回 204 No Content | 物理删除，SELECT 返回空 |
| API-031 | 删除不存在的内容 | 抛出 RuntimeException → 404 | - |

#### 2.2.5 内容导出 `GET /api/contents/export`

| 用例 ID | 场景 | 预期结果 | 文件验证 |
|---------|------|----------|----------|
| API-040 | 导出 Excel (format=xlsx) | Content-Type: application/vnd.openxmlformats-... | 文件扩展名 .xlsx |
| API-041 | 导出 CSV (format=csv) | Content-Type: text/csv;charset=UTF-8 | 文件扩展名 .csv |
| API-042 | 按 taskId 导出 | 只导出该 taskId 的内容 | 筛选正确 |
| API-043 | 导出空结果 | 生成空文件（带表头） | 表头存在，无数据行 |
| API-044 | CSV 特殊字符 | 字段含逗号、引号、换行 | 双引号转义正确 |
| API-045 | Excel fields 展开 | fields JSON 展开为独立列 | 列名=fields 的 key |

### 2.3 测试数据准备

每个测试方法使用 `@BeforeEach` 准备数据：

```java
@BeforeEach
void setUp() {
    // 1. 创建 SpiderTask (taskId=1)
    SpiderTask task = new SpiderTask();
    task.setName("测试任务");
    task.setEnabled(true);
    task = taskRepository.save(task);

    // 2. 创建 ContentItem (id=1, taskId=1, status=PENDING, fields={"title":"原始标题"})
    ContentItem content = new ContentItem();
    content.setTaskId(task.getId());
    content.setSourceUrl("https://example.com/article/1");
    content.setStatus(ContentStatus.PENDING);
    content.setFields(Map.of("title", "原始标题", "content", "正文内容"));
    content.setRawHtml("<html>...</html>");
    content = contentRepository.save(content);
}
```

### 2.4 关键技术实现

```java
@SpringBootTest
@AutoConfigureMockMvc
class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void updateStatus_shouldSetPublishedAt() throws Exception {
        // Given: 已存在的 PENDING 内容
        ContentItem content = contentRepository.findAll().get(0);
        assertEquals(ContentStatus.PENDING, content.getPublishedAt());

        // When: 更新状态为 PUBLISHED
        mockMvc.perform(put("/api/contents/{id}", content.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PUBLISHED\"}"))

        // Then: publishedAt 被自动设置
        ContentItem updated = contentRepository.findById(content.getId()).orElseThrow();
        assertEquals(ContentStatus.PUBLISHED, updated.getStatus());
        assertNotNull(updated.getPublishedAt());
    }
}
```

---

## 3. 第二层：前端 E2E 测试 (Playwright)

### 3.1 测试文件结构

```
frontend/tests/e2e/
├── page-objects/
│   ├── ContentListPage.ts      # 新建
│   ├── ContentPreviewPage.ts   # 新建
│   └── ContentEditPage.ts      # 新建
├── content-list.spec.ts        # 新建
├── content-preview.spec.ts     # 新建
├── content-edit.spec.ts        # 新建
├── content-delete.spec.ts      # 新建
└── content-export.spec.ts      # 新建
```

### 3.2 内容列表页测试 `content-list.spec.ts`

#### 测试场景

| 用例 ID | 场景 | 操作步骤 | 验证点 |
|---------|------|----------|--------|
| E2E-001 | 页面加载 | 访问 `/contents` | 表格可见，分页器可见 |
| E2E-002 | 显示内容列表 | 页面加载完成 | 显示 sourceUrl、status、createdAt 列 |
| E2E-003 | 分页切换 | 点击"每页 50 条" | URL 参数变化，重新请求 API |
| E2E-004 | 按任务筛选 | 下拉框选择 taskId | GET /api/contents?taskId=X 被调用 |
| E2E-005 | 筛选后重置 | 点击清除筛选 | 恢复显示全部内容 |
| E2E-006 | 点击预览按钮 | 点击某行预览 | 预览弹窗出现 |
| E2E-007 | 点击编辑按钮 | 点击某行编辑 | 跳转到 /contents/:id/edit |
| E2E-008 | 点击删除按钮 | 点击某行删除 | 确认对话框出现 |
| E2E-009 | 确认删除 | 点击确定 | DELETE API 被调用，列表刷新 |
| E2E-010 | 取消删除 | 点击取消 | 无 API 调用，对话框关闭 |

### 3.3 内容预览页测试 `content-preview.spec.ts`

#### 测试场景

| 用例 ID | 场景 | 操作步骤 | 验证点 |
|---------|------|----------|--------|
| E2E-020 | 预览弹窗打开 | 从列表页点击预览 | 弹窗 visible |
| E2E-021 | 显示基本信息 | 弹窗内 | 显示 id、status、sourceUrl、createdAt |
| E2E-022 | 显示字段表格 | 弹窗内 | fields 以表格形式展示 |
| E2E-023 | iframe 沙箱渲染 | rawHtml 存在 | iframe 带 sandbox 属性 |
| E2E-024 | 关闭弹窗 | 点击关闭按钮 | 弹窗关闭 |
| E2E-025 | 点击空白关闭 | 点击遮罩层 | 弹窗关闭 |

### 3.4 内容编辑页测试 `content-edit.spec.ts`

#### 测试场景

| 用例 ID | 场景 | 操作步骤 | 验证点 |
|---------|------|----------|--------|
| E2E-030 | 页面加载 | 访问 `/contents/1/edit` | 显示加载状态，然后显示表单 |
| E2E-031 | 表单字段显示 | 页面加载完成 | 显示 sourceUrl（只读）、status 下拉、fields 编辑框 |
| E2E-032 | 修改字段值 | 修改某 fields 值 | 表单值更新 |
| E2E-033 | 修改状态 | 选择"已发布" | status 变为 PUBLISHED |
| E2E-034 | 保存成功 | 点击保存按钮 | PUT /api/contents/1 调用成功 |
| E2E-035 | 保存后跳转 | 保存成功后 | 自动跳转到 /contents |
| E2E-036 | 返回列表 | 点击返回按钮 | 跳转到 /contents |

### 3.5 内容删除测试 `content-delete.spec.ts`

#### 测试场景

| 用例 ID | 场景 | 操作步骤 | 验证点 |
|---------|------|----------|--------|
| E2E-040 | 删除确认对话框 | 点击删除按钮 | 显示确认对话框 |
| E2E-041 | 确认删除 | 点击确定 | DELETE API 调用，内容从列表移除 |
| E2E-042 | 取消删除 | 点击取消 | 对话框关闭，内容保留 |
| E2E-043 | 删除成功提示 | 删除成功 | 显示成功消息 |

### 3.6 导出功能测试 `content-export.spec.ts`

#### 测试场景

| 用例 ID | 场景 | 操作步骤 | 验证点 |
|---------|------|----------|--------|
| E2E-050 | 导出 Excel | 点击导出按钮(xlsx) | 下载触发，文件名含 .xlsx |
| E2E-051 | 导出 CSV | 选择 CSV 格式导出 | 下载触发，文件名含 .csv |
| E2E-052 | 导出后留在页面 | 导出完成 | 仍在内容列表页 |

### 3.7 Page Object 设计

#### ContentListPage.ts

```typescript
export class ContentListPage extends BasePage {
  readonly url = '/contents'
  readonly table: Locator
  readonly pagination: Locator
  readonly taskFilter: Locator
  readonly exportButton: Locator

  constructor(page: Page) {
    super(page)
    this.table = page.locator('.el-table')
    this.pagination = page.locator('.el-pagination')
    this.taskFilter = page.locator('.el-select').first()
    this.exportButton = page.locator('button:has-text("导出")')
  }

  async getRowCount(): Promise<number> {
    return await this.page.locator('.el-table__body tr').count()
  }

  async clickPreview(contentId: number): Promise<void> {
    const row = this.page.locator('.el-table__body tr').filter({ hasText: String(contentId) })
    await row.locator('button:has-text("预览")').click()
  }

  async clickEdit(contentId: number): Promise<void> {
    const row = this.page.locator('.el-table__body tr').filter({ hasText: String(contentId) })
    await row.locator('button:has-text("编辑")').click()
  }

  async clickDelete(contentId: number): Promise<void> {
    const row = this.page.locator('.el-table__body tr').filter({ hasText: String(contentId) })
    await row.locator('button:has-text("删除")').click()
  }
}
```

---

## 4. 测试数据准备方案

### 4.1 后端测试数据准备

使用 `@BeforeEach` 在每个测试方法前：

```java
class ContentControllerTest {
    @Autowired ContentItemRepository contentRepository;
    @Autowired SpiderTaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        contentRepository.deleteAll();
        taskRepository.deleteAll();

        SpiderTask task = taskRepository.save(SpiderTask.builder()
            .name("E2E 测试任务")
            .enabled(true)
            .build());

        contentRepository.save(ContentItem.builder()
            .taskId(task.getId())
            .sourceUrl("https://example.com/1")
            .status(ContentStatus.PENDING)
            .fields(Map.of("title", "测试标题", "content", "测试内容"))
            .build());
    }
}
```

### 4.2 前端 E2E 数据准备

通过后端 API 准备数据（测试 `beforeAll` 阶段）：

```typescript
test.beforeAll(async ({ request }) => {
  // 清理并准备测试数据
  await request.post(`${baseURL}/api/test/reset-data`)
})
```

**或** 在测试中直接调用业务代码准备数据（推荐）。

---

## 5. 执行顺序与依赖

```
1. API 集成测试 (ContentControllerTest)
   ├── API-001 ~ API-045
   └── 无外部依赖，独立运行

2. 前端 E2E 测试
   ├── E2E-001 ~ E2E-010 (列表页)
   ├── E2E-020 ~ E2E-025 (预览页)
   ├── E2E-030 ~ E2E-036 (编辑页)
   ├── E2E-040 ~ E2E-043 (删除)
   └── E2E-050 ~ E2E-052 (导出)
   └── 依赖: API 测试通过 + 前端服务运行
```

---

## 6. 关键验证点汇总

### 6.1 后端验证

| 功能 | 验证点 |
|------|--------|
| 列表分页 | page/size 参数正确传递，返回数据结构正确 |
| 内容更新 | fields 部分更新不影响其他字段 |
| 状态变更 PUBLISHED | publishedAt 自动设置为当前时间 |
| 状态变更 DELETED | publishedAt 保持不变 |
| 删除 | 物理删除，非软删除 |
| 导出 CSV | 特殊字符（逗号、引号、换行）正确转义 |
| 导出 Excel | fields JSON 展开为独立列 |

### 6.2 前端验证

| 功能 | 验证点 |
|------|--------|
| 列表分页 | 分页器操作触发 API 重新请求 |
| 筛选 | taskId 筛选只显示对应内容 |
| 预览弹窗 | iframe sandbox 属性正确设置 |
| 编辑保存 | 保存后跳转到列表页 |
| 删除确认 | 确认对话框出现，确认后删除 |

---

## 7. 测试运行方式

### 7.1 后端 API 测试

```bash
cd backend
mvn test -Dtest=ContentControllerTest
```

### 7.2 前端 E2E 测试

```bash
cd frontend
npm run test:e2e
# 或只运行 content 相关测试
npx playwright test content
```

---

## 8. 文件清单

### 8.1 新建文件

| 文件路径 | 说明 |
|----------|------|
| `backend/src/test/java/com/example/visualspider/controller/ContentControllerTest.java` | 后端 API 集成测试 |
| `frontend/tests/e2e/page-objects/ContentListPage.ts` | 列表页 Page Object |
| `frontend/tests/e2e/page-objects/ContentPreviewPage.ts` | 预览页 Page Object |
| `frontend/tests/e2e/page-objects/ContentEditPage.ts` | 编辑页 Page Object |
| `frontend/tests/e2e/content-list.spec.ts` | 列表页 E2E 测试 |
| `frontend/tests/e2e/content-preview.spec.ts` | 预览页 E2E 测试 |
| `frontend/tests/e2e/content-edit.spec.ts` | 编辑页 E2E 测试 |
| `frontend/tests/e2e/content-delete.spec.ts` | 删除功能 E2E 测试 |
| `frontend/tests/e2e/content-export.spec.ts` | 导出功能 E2E 测试 |

---

## 9. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 测试数据被其他测试污染 | 测试结果不稳定 | 每个测试前清理数据 |
| 导出文件下载路径不确定 | 无法验证文件内容 | 使用 `page.waitForEvent('download')` |
| iframe 内容加载异步 | 无法直接验证 iframe 内容 | 等待 iframe load 事件后再验证 |
| API 响应时间不稳定 | 测试超时 | 设置合理的 timeout |
