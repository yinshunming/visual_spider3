## Context

M5 内容管理里程碑已完成前后端实现（ContentController、ContentService、前端 Vue 组件），需要补充完整的自动化测试。

**当前状态**：
- Service 层单元测试 `ContentServiceTest` 已存在
- Repository 层测试 `ContentItemRepositoryTest` 已存在
- 前端已有 Task 相关 Playwright E2E 测试作为参考
- ContentController 所有端点已实现（列表、详情、更新、删除、导出）

**测试分层**：
1. **API 集成测试**：直接调用 Controller 层，验证 Service→Repository→DB 链路
2. **E2E 测试**：通过 Playwright 模拟浏览器，验证前端→API→DB 完整链路

**利益相关者**：开发团队，需要快速回归验证内容管理功能

## Goals / Non-Goals

**Goals:**
- 覆盖 ContentController 所有 REST 端点的 API 集成测试
- 覆盖内容管理前端完整用户流程的 E2E 测试
- 验证 `publishedAt` 状态变更自动设置的核心业务逻辑
- 验证导出功能（Excel/CSV）文件格式正确性

**Non-Goals:**
- 不实现性能/压力测试
- 不实现安全渗透测试
- 不修改现有 Service/Repository 单元测试（已存在）
- 不覆盖大数据量截断场景（>10000条）

## Decisions

### Decision 1: 测试数据准备方式 - @BeforeEach 内联准备

**选择**: 在每个测试方法内使用 `@BeforeEach` 创建测试数据

**理由**:
- 数据量小（1-2条），内联创建简单直观
- 测试间完全隔离，无数据污染风险
- 不需要额外的测试数据准备接口

**替代方案**:
- `@BeforeAll` 准备共享数据：存在测试间数据依赖风险
- 外部 SQL 文件导入：增加维护成本

### Decision 2: 前端 E2E 测试架构 - Page Object 模式

**选择**: 复用现有 `BasePage`，为每个页面创建专属 Page Object

**理由**:
- 符合项目现有 Playwright 测试架构
- 页面元素定位集中管理，便于维护
- 测试用例与页面结构解耦

**替代方案**:
- 直接在 spec 内定位元素：定位逻辑分散，难以复用

### Decision 3: API 测试框架 - MockMvc

**选择**: 使用 `@SpringBootTest` + `@AutoConfigureMockMvc`

**理由**:
- 项目已有此配置（参考 TaskControllerTest 如存在）
- 无需启动真实 HTTP 服务器
- 可直接验证数据库状态

**替代方案**:
- `@WebMvcTest`：仅加载 Controller，轻量但需要额外 Mock Repository 配置

### Decision 4: 删除操作验证 - 物理删除

**选择**: 验证删除后数据库无记录

**理由**:
- ContentItemRepository 使用 JPA `deleteById`，默认物理删除
- 删除后 `findById` 应返回 empty

**替代方案**:
- 软删除验证：需修改现有 Repository 实现

### Decision 5: 导出文件验证 - Content-Type + 文件扩展名

**选择**: 通过响应头 Content-Type 和文件名验证

**理由**:
- 不依赖实际文件写入磁盘
- 验证 Content-Type 和 Content-Disposition 头即可确认格式正确
- E2E 测试使用 `page.waitForEvent('download')` 验证下载触发

## Risks / Trade-offs

[风险] E2E 测试依赖后端服务运行
→ 缓解：Playwright 配置 `webServer` 启动前后端服务

[风险] 导出测试生成临时文件
→ 缓解：Playwright download API 不实际保存文件到磁盘

[风险] iframe 内容加载异步
→ 缓解：等待 `iframe.load` 事件后再验证

[风险] 分页测试依赖数据库数据量
→ 缓解：`@BeforeEach` 确保测试前数据状态已知

## Migration Plan

1. 创建 `ContentControllerTest.java`（API 集成测试）
2. 创建 Page Object 文件（ContentListPage、ContentPreviewPage、ContentEditPage）
3. 创建 E2E spec 文件
4. 运行 `mvn test -Dtest=ContentControllerTest` 验证 API 测试
5. 运行 `npm run test:e2e` 验证前端 E2E 测试

**回滚**：删除测试文件，前端/后端代码不受影响

## Open Questions

1. ~~导出是否需要验证文件内容~~ → 不验证，仅验证下载触发
2. ~~删除是软删除还是硬删除~~ → 物理删除（已确认 Repository 实现）
3. ~~E2E 测试数据准备方式~~ → `@BeforeEach` 内联准备
