# Content Management API Test

**日期**: 2026-05-10
**状态**: 新增

## ADDED Requirements

### Requirement: 内容列表分页查询

系统 SHALL 支持通过 GET /api/contents 分页查询内容列表。

#### Scenario: 分页查询默认第一页
- **WHEN** 调用 `GET /api/contents?page=0&size=20`
- **THEN** 返回 Page，内容条数不超过 pageSize

#### Scenario: 指定分页参数
- **WHEN** 调用 `GET /api/contents?page=1&size=5`
- **THEN** 返回第2页，最多5条内容

#### Scenario: 按 taskId 筛选
- **WHEN** 调用 `GET /api/contents?taskId=5`
- **THEN** 只返回 taskId=5 的内容

#### Scenario: 按 status 筛选
- **WHEN** 调用 `GET /api/contents?status=PENDING`
- **THEN** 只返回 PENDING 状态的内容

#### Scenario: 按 taskId 和 status 组合筛选
- **WHEN** 调用 `GET /api/contents?taskId=5&status=PUBLISHED`
- **THEN** 返回同时满足 taskId=5 和 status=PUBLISHED 的内容

#### Scenario: 查询空结果
- **WHEN** 调用 `GET /api/contents` 但无数据
- **THEN** 返回空 Page，非 404

### Requirement: 内容详情查询

系统 SHALL 支持通过 ID 获取内容详情。

#### Scenario: 获取存在的内容
- **WHEN** 调用 `GET /api/contents/1`
- **THEN** 返回 ContentResponse，fields 解析为 Map<String, Object>

#### Scenario: 获取不存在的内容
- **WHEN** 调用 `GET /api/contents/99999`
- **THEN** 返回 404 Not Found

#### Scenario: fields 为 null 时
- **WHEN** ContentItem.fields 为 null
- **THEN** 返回的 fields 为空 Map 而非 null

### Requirement: 内容更新

系统 SHALL 支持更新内容字段和状态。

#### Scenario: 仅更新 fields
- **WHEN** 调用 `PUT /api/contents/1` with `{"fields": {"title": "新标题"}}`
- **THEN** fields 更新，status 不变，createdAt 不变

#### Scenario: 更新 status PENDING→PUBLISHED
- **WHEN** 调用 `PUT /api/contents/1` with `{"status": "PUBLISHED"}`
- **THEN** status 变更，publishedAt 自动设置为当前时间

#### Scenario: 更新 status PUBLISHED→DELETED
- **WHEN** 调用 `PUT /api/contents/1` with `{"status": "DELETED"}`
- **THEN** status 变更，publishedAt 保持不变

#### Scenario: 同时更新 fields 和 status
- **WHEN** 调用 `PUT /api/contents/1` with `{"fields": {...}, "status": "PUBLISHED"}`
- **THEN** fields 和 status 都更新，publishedAt 按规则设置

#### Scenario: 更新不存在的内容
- **WHEN** 调用 `PUT /api/contents/99999`
- **THEN** 返回 404 Not Found

### Requirement: 内容删除

系统 SHALL 支持删除内容。

#### Scenario: 删除存在的内容
- **WHEN** 调用 `DELETE /api/contents/1`
- **THEN** 返回 204 No Content，内容从数据库物理删除

#### Scenario: 删除不存在的内容
- **WHEN** 调用 `DELETE /api/contents/99999`
- **THEN** 返回 404 Not Found

### Requirement: 内容导出

系统 SHALL 支持导出内容为 Excel 或 CSV 格式。

#### Scenario: 导出 Excel
- **WHEN** 调用 `GET /api/contents/export?format=xlsx`
- **THEN** Content-Type 为 application/vnd.openxmlformats-officedocument.spreadsheetml.sheet

#### Scenario: 导出 CSV
- **WHEN** 调用 `GET /api/contents/export?format=csv`
- **THEN** Content-Type 为 text/csv;charset=UTF-8

#### Scenario: 按 taskId 导出
- **WHEN** 调用 `GET /api/contents/export?taskId=5&format=xlsx`
- **THEN** 只导出 taskId=5 的内容

#### Scenario: 导出空结果
- **WHEN** 调用导出但无数据
- **THEN** 生成带表头的空文件

#### Scenario: CSV 特殊字符转义
- **WHEN** 字段值包含逗号、引号、换行
- **THEN** 字段值用双引号包裹，双引号转义为两个双引号

#### Scenario: Excel fields 展开
- **WHEN** fields 值为 `{"title": "标题", "content": "正文"}`
- **THEN** Excel 中 title 和 content 作为独立列存在
