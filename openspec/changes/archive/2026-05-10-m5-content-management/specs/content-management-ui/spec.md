# Content Management UI

**日期**: 2026-05-10
**状态**: 新增

## ADDED Requirements

### Requirement: 内容列表页面

系统 SHALL 提供内容列表页面，显示爬取的内容。

#### Scenario: 页面加载显示内容列表
- **WHEN** 用户访问 ContentList.vue
- **THEN** 显示内容列表表格，包含来源URL、状态、创建时间
- **AND** 支持分页（每页 20 条）

#### Scenario: 按任务筛选
- **WHEN** 用户在任务下拉框选择特定任务
- **THEN** 表格只显示该任务下的内容

#### Scenario: 查看任务下拉列表
- **WHEN** 页面加载时
- **THEN** 从 `/api/tasks` 获取任务列表填充下拉框

### Requirement: 内容预览

系统 SHALL 支持预览内容详情（渲染后的 HTML）。

#### Scenario: 点击预览按钮
- **WHEN** 用户点击内容行的"预览"按钮
- **THEN** 弹出 ContentPreview.vue 对话框，显示渲染后的 rawHtml

#### Scenario: 预览使用 iframe 沙箱
- **WHEN** 预览对话框渲染 rawHtml
- **THEN** 使用 iframe sandbox 属性，禁止脚本执行

### Requirement: 内容编辑

系统 SHALL 支持编辑内容字段和状态。

#### Scenario: 跳转到编辑页面
- **WHEN** 用户点击内容行的"编辑"按钮
- **THEN** 跳转到 ContentEdit.vue?id=xxx

#### Scenario: 编辑页面加载内容
- **WHEN** ContentEdit.vue 加载时
- **THEN** 调用 `GET /api/contents/{id}` 获取内容详情

#### Scenario: 编辑内容字段
- **WHEN** 用户修改字段值并点击保存
- **THEN** 调用 `PUT /api/contents/{id}` 更新内容

#### Scenario: 修改内容状态
- **WHEN** 用户在编辑页面修改状态下拉框
- **THEN** 保存时将新状态一并提交

#### Scenario: 返回列表页
- **WHEN** 用户点击"返回"按钮
- **THEN** 跳转到内容列表页，刷新列表数据

### Requirement: 内容删除

系统 SHALL 支持从列表页删除内容。

#### Scenario: 删除内容
- **WHEN** 用户点击内容行的"删除"按钮
- **THEN** 调用 `DELETE /api/contents/{id}`
- **AND** 成功后刷新列表

#### Scenario: 删除确认
- **WHEN** 用户点击删除按钮
- **THEN** 弹出确认对话框，确认后执行删除
