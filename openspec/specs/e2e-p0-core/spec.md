# e2e-p0-core

**日期**: 2026-05-11
**状态**: 已实现

---

## ADDED Requirements

### Requirement: E2E-P0-01 完整主链路测试

系统 SHALL 提供完整主链路测试，用例 `e2e-main-flow-complete`：

- 覆盖链路：创建任务 → 配置规则 → 保存任务 → 启用任务 → 手动执行 → 内容入库 → 内容列表查看
- 前置条件：后端服务运行在 :8080，前端服务运行在 :3000
- 测试数据：Mock Target Site 返回静态 HTML
- 操作步骤：
  1. 创建任务（LIST_PAGE 模式）
  2. 配置列表页 URL 和规则
  3. 配置内容页字段（title、content）
  4. 保存配置
  5. 启用任务
  6. 执行任务
  7. 轮询等待执行完成
  8. 验证内容条数 >= 1

### Requirement: E2E-P0-02 内容导出 Excel

系统 SHALL 提供内容导出 Excel 的 E2E 测试，用例 `e2e-content-export-excel`：

- 覆盖链路：内容列表 → 导出 Excel
- 前置条件：数据库中至少存在 3 条 ContentItem
- 操作步骤：
  1. 访问 /contents
  2. 点击导出按钮
  3. 等待下载完成
  4. 解析下载的 .xlsx 文件
- 断言：
  - 下载文件扩展名为 .xlsx
  - Excel 中行数 = ContentItem 条数 + 1(header)
  - 列名包含字段名

### Requirement: E2E-P0-03 内容导出 CSV

系统 SHALL 提供内容导出 CSV 的 E2E 测试，用例 `e2e-content-export-csv`：

- 覆盖链路：内容列表 → 导出 CSV
- 前置条件：数据库中至少存在 3 条 ContentItem
- 操作步骤：
  1. 访问 /contents
  2. 选择 CSV 格式导出
  3. 等待下载完成
  4. 解析下载的 .csv 文件
- 断言：
  - 下载文件扩展名为 .csv
  - CSV 可正常解析（无乱码）

### Requirement: E2E-P0-04 内容预览和编辑

系统 SHALL 提供内容预览和编辑的 E2E 测试，用例 `e2e-content-preview-edit`：

- 覆盖链路：内容列表 → 预览 → 编辑
- 前置条件：数据库中至少存在 1 条 ContentItem
- 操作步骤：
  1. 访问 /contents
  2. 点击预览按钮
  3. 验证预览对话框显示内容
  4. 关闭预览
  5. 点击编辑按钮
  6. 修改 status 为 DELETED
  7. 保存
- 断言：
  - 预览对话框可见且包含内容字段
  - 编辑保存后 status = DELETED

---

## ADDED Scenarios

#### Scenario: E2E-P0-01 完整主链路
- **WHEN** 执行 e2e-main-flow-complete 测试
- **THEN** 任务创建成功，配置保存成功，执行后内容入库

#### Scenario: E2E-P0-02 导出 Excel
- **WHEN** 执行 e2e-content-export-excel 测试
- **THEN** 下载 .xlsx 文件，文件可解析，内容条数正确

#### Scenario: E2E-P0-03 导出 CSV
- **WHEN** 执行 e2e-content-export-csv 测试
- **THEN** 下载 .csv 文件，文件可解析

#### Scenario: E2E-P0-04 预览编辑
- **WHEN** 执行 e2e-content-preview-edit 测试
- **THEN** 预览显示内容，编辑保存后状态正确