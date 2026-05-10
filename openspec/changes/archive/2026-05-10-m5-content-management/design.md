## Context

当前状态：
- `ContentItem` 实体已完成，支持 JSONB 存储动态字段
- `ContentItemRepository` 已实现分页查询方法
- `ContentService` 有部分 CRUD 方法，导出方法为空
- `ContentController` 骨架存在，所有端点返回 null
- 前端无内容管理页面

利益相关者：运营同学，需要查看、编辑、导出爬取的内容

## Goals / Non-Goals

**Goals:**
- 实现完整的内容 CRUD API（列表、详情、更新、删除）
- 支持按 taskId 筛选内容列表
- 支持内容状态变更（PENDING/PUBLISHED/DELETED）
- 实现 Excel (.xlsx) 和 CSV 两种导出格式
- 前端内容列表、预览、编辑页面

**Non-Goals:**
- 不实现内容导入功能
- 不实现内容版本管理
- 不实现内容全文搜索（后续 M6 可考虑）
- 不实现自动内容去重

## Decisions

### Decision 1: 导出格式 - 同时支持 Excel 和 CSV

**选择**: 两者都实现，通过 `format` 参数切换

**理由**:
- CSV 简单、通用、文件小，适合数据量大的场景
- Excel 格式支持多 sheet、格式化、公式，适合运营做二次分析
- 用户可以在前端选择导出格式

**替代方案**:
- 只做 CSV：简单但功能受限
- 只做 Excel：需要引入较大依赖 (Apache POI ~10MB)

### Decision 2: 导出实现 - Apache POI vs OpenCSV

**选择**: Apache POI (Excel) + 手写 CSV

**理由**:
- POI 成熟稳定，支持 .xlsx 格式
- CSV 生成简单，不需要额外库

**替代方案**:
- 全部用 POI 的 CSV 导出：功能足够但需要学习 POI CSV API
- 全部用 OpenCSV：轻量但 POI 更通用

### Decision 3: 状态变更方式 - PUT 时通过请求体传入 status

**选择**: 在 ContentItemRequest 中包含 status 字段，更新时一起修改

**理由**:
- 减少 API 端点数量
- 编辑内容时通常也会改状态
- 符合 RESTful 设计

**替代方案**:
- 单独端点 `PUT /api/contents/{id}/status`：更清晰但多一次请求
- 单独端点 `PATCH /api/contents/{id}/status`：介于两者之间

### Decision 4: 前端页面路由结构

**选择**: ContentList -> ContentPreview/ContentEdit（跳转新页面）

**理由**:
- 列表页保持简洁
- 预览和编辑需要更多空间
- 符合现有前端模式（TaskList -> TaskConfig）

**替代方案**:
- 全部在一个页面用 Tab 切换：复杂，不适合编辑操作
- 模态框编辑：数据量大时体验差

### Decision 5: 前端使用 Element Plus 组件

**选择**: 复用现有 Element Plus

**理由**:
- 项目已使用 Element Plus
- el-table 分页、筛选功能完善
- el-dialog 用于预览/编辑页面

## Risks / Trade-offs

[风险] 导出大文件时内存占用高
→ 缓解：使用 SXSSF 流式写入，限制单次导出数量（如 10000 条）

[风险] JSONB 字段直接暴露给前端，格式可能变化
→ 缓解：ContentResponse 中解析 fields 为 Map<String, Object>，提供稳定的 API 视图

[风险] rawHtml 渲染可能存在 XSS 风险
→ 缓解：预览时使用 iframe 沙箱渲染，禁止脚本执行

[权衡] 内容编辑不支持字段 schema 验证（因为字段是动态的）
→ 接受：运营已知字段含义，错误由人工检查

## Migration Plan

1. 部署后端 API（向后兼容，无破坏性变更）
2. 部署前端（ContentList 入口可暂时隐藏，待功能完成）
3. 逐步开放功能入口

回滚：删除前端页面文件即可，前端 API 调用自动 404

## Open Questions

1. 导出是否需要支持自定义列选择？（当前实现导出所有字段）
2. 内容列表默认排序方式？按 createdAt 倒序还是 publishedAt 倒序？
3. 删除是软删除（改为 DELETED）还是硬删除？

这三个问题不影响基本实现，可以后续补充。
