# Content Management API

**日期**: 2026-05-10
**状态**: 新增

## ADDED Requirements

### Requirement: 内容列表查询

系统 SHALL 支持分页查询内容列表，支持按 taskId 筛选。

#### Scenario: 分页查询所有内容
- **WHEN** 调用 `GET /api/contents?page=0&size=20`
- **THEN** 返回 Page<ContentItem>，包含分页元数据（totalElements, totalPages）

#### Scenario: 按任务筛选内容
- **WHEN** 调用 `GET /api/contents?taskId=5&page=0&size=20`
- **THEN** 只返回 taskId=5 的内容

#### Scenario: 组合筛选
- **WHEN** 调用 `GET /api/contents?taskId=5&page=0&size=10`
- **THEN** 返回 taskId=5 的内容，分页大小为 10

### Requirement: 内容详情查询

系统 SHALL 支持通过 ID 获取内容详情。

#### Scenario: 获取存在的内容
- **WHEN** 调用 `GET /api/contents/123`
- **THEN** 返回完整的 ContentItem，包括 fields JSON 解析后的 Map

#### Scenario: 获取不存在的内容
- **WHEN** 调用 `GET /api/contents/99999`
- **THEN** 返回 404 Not Found

### Requirement: 内容更新

系统 SHALL 支持更新内容字段和状态。

#### Scenario: 更新内容字段
- **WHEN** 调用 `PUT /api/contents/123` with `{"fields": {"title": "新标题"}}`
- **THEN** ContentItem.fields 更新为新值，createdAt 不变

#### Scenario: 更新内容状态
- **WHEN** 调用 `PUT /api/contents/123` with `{"status": "PUBLISHED"}`
- **THEN** ContentItem.status 更新为 PUBLISHED，publishedAt 更新为当前时间

### Requirement: 内容删除

系统 SHALL 支持删除内容。

#### Scenario: 删除存在的内容
- **WHEN** 调用 `DELETE /api/contents/123`
- **THEN** 内容从数据库移除，返回 204 No Content

#### Scenario: 删除不存在的内容
- **WHEN** 调用 `DELETE /api/contents/99999`
- **THEN** 返回 404 Not Found

### Requirement: 导出内容

系统 SHALL 支持导出内容为 Excel 或 CSV 格式。

#### Scenario: 导出 Excel
- **WHEN** 调用 `GET /api/contents/export?taskId=5&format=xlsx`
- **THEN** 返回 .xlsx 文件，Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet

#### Scenario: 导出 CSV
- **WHEN** 调用 `GET /api/contents/export?taskId=5&format=csv`
- **THEN** 返回 .csv 文件，Content-Type: text/csv

#### Scenario: 无 taskId 导出全部
- **WHEN** 调用 `GET /api/contents/export?format=xlsx`
- **THEN** 导出所有内容

#### Scenario: 导出超过限制
- **WHEN** 导出请求超过 10000 条
- **THEN** 返回前 10000 条，并提示截断
