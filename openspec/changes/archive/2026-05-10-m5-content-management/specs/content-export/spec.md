# Content Export

**日期**: 2026-05-10
**状态**: 新增

## ADDED Requirements

### Requirement: Excel 导出

系统 SHALL 支持将内容导出为 Excel (.xlsx) 格式。

#### Scenario: 导出 Excel 文件
- **WHEN** 调用 `GET /api/contents/export?format=xlsx`
- **THEN** 生成 .xlsx 文件，文件名为 `content_export_{timestamp}.xlsx`

#### Scenario: Excel 文件结构
- **WHEN** 生成 Excel 文件
- **THEN** 第一行为表头（source_url, status, published_at, created_at, 以及 fields 中的所有字段）
- **AND** 数据行从第二行开始

#### Scenario: fields JSON 展开
- **WHEN** fields 值为 `{"title": "标题", "content": "正文"}`
- **THEN** Excel 中展开为独立的 "title" 和 "content" 列

### Requirement: CSV 导出

系统 SHALL 支持将内容导出为 CSV 格式。

#### Scenario: 导出 CSV 文件
- **WHEN** 调用 `GET /api/contents/export?format=csv`
- **THEN** 生成 .csv 文件，文件名为 `content_export_{timestamp}.csv`

#### Scenario: CSV 编码
- **WHEN** 生成 CSV 文件
- **THEN** 使用 UTF-8 编码，带 BOM

#### Scenario: CSV 特殊字符处理
- **WHEN** 字段值包含逗号、引号、换行
- **THEN** 用引号包裹字段值，双引号转义

### Requirement: 导出筛选

系统 SHALL 支持按任务 ID 筛选导出的内容。

#### Scenario: 按任务导出
- **WHEN** 调用 `GET /api/contents/export?taskId=5&format=xlsx`
- **THEN** 只导出 taskId=5 的内容

### Requirement: 导出限制

系统 SHALL 限制单次导出的最大数据量。

#### Scenario: 超过导出限制
- **WHEN** 导出请求超过 10000 条
- **THEN** 只导出前 10000 条
- **AND** 在响应中包含 `X-Export-Truncated: true` 头

### Requirement: 导出进度

系统 SHALL 支持大文件的流式导出。

#### Scenario: 流式导出
- **WHEN** 导出大量数据
- **THEN** 使用流式写入，避免内存溢出

#### Scenario: 导出时数据库连接
- **WHEN** 导出过程中
- **THEN** 使用只读事务，避免长时间锁定
