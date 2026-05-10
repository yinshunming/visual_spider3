# Content Management UI Test

**日期**: 2026-05-10
**状态**: 新增

## ADDED Requirements

### Requirement: 内容列表页加载

系统 SHALL 提供内容列表页面，显示爬取的内容。

#### Scenario: 页面加载
- **WHEN** 用户访问 /contents
- **THEN** 页面显示内容表格、分页器、任务筛选下拉框、导出按钮

#### Scenario: 显示内容列表
- **WHEN** 页面加载完成
- **THEN** 表格显示 sourceUrl、status、createdAt 列

#### Scenario: 分页切换
- **WHEN** 用户点击"每页 50 条"
- **THEN** 触发 API 重新请求，表格更新

#### Scenario: 按任务筛选
- **WHEN** 用户在任务下拉框选择特定任务
- **THEN** 表格只显示该任务下的内容

#### Scenario: 清除筛选
- **WHEN** 用户点击清除筛选
- **THEN** 恢复显示全部内容

### Requirement: 内容预览

系统 SHALL 支持预览内容详情。

#### Scenario: 点击预览按钮
- **WHEN** 用户点击内容行的"预览"按钮
- **THEN** 弹出预览对话框，显示内容详情

#### Scenario: 显示基本信息
- **WHEN** 预览弹窗打开
- **THEN** 显示 id、status、sourceUrl、createdAt

#### Scenario: 显示字段表格
- **WHEN** 预览弹窗显示 fields
- **THEN** fields 以表格形式展示

#### Scenario: iframe 沙箱渲染
- **WHEN** 内容包含 rawHtml
- **THEN** rawHtml 在带 sandbox 属性的 iframe 内渲染

#### Scenario: 关闭预览弹窗
- **WHEN** 用户点击"关闭"按钮
- **THEN** 预览弹窗关闭

### Requirement: 内容编辑

系统 SHALL 支持编辑内容字段和状态。

#### Scenario: 页面加载
- **WHEN** 用户访问 /contents/:id/edit
- **THEN** 页面显示加载状态，然后显示编辑表单

#### Scenario: 表单字段显示
- **WHEN** 编辑页加载完成
- **THEN** 显示 sourceUrl（只读）、status 下拉、fields 编辑框

#### Scenario: 修改字段值
- **WHEN** 用户修改某 fields 值并保存
- **THEN** 调用 PUT /api/contents/:id 更新内容

#### Scenario: 修改状态
- **WHEN** 用户选择新状态并保存
- **THEN** 调用 PUT /api/contents/:id 更新状态

#### Scenario: 保存成功
- **WHEN** 保存成功
- **THEN** 自动跳转到 /contents 列表页

#### Scenario: 返回列表
- **WHEN** 用户点击"返回"按钮
- **THEN** 跳转到 /contents

### Requirement: 内容删除

系统 SHALL 支持从列表页删除内容。

#### Scenario: 删除确认对话框
- **WHEN** 用户点击"删除"按钮
- **THEN** 弹出确认对话框

#### Scenario: 确认删除
- **WHEN** 用户点击确认对话框的"确定"
- **THEN** 调用 DELETE API，内容从列表移除

#### Scenario: 取消删除
- **WHEN** 用户点击"取消"
- **THEN** 对话框关闭，内容保留

### Requirement: 内容导出

系统 SHALL 支持导出内容。

#### Scenario: 导出 Excel
- **WHEN** 用户点击"导出"按钮
- **THEN** 触发 GET /api/contents/export?format=xlsx 下载

#### Scenario: 导出 CSV
- **WHEN** 用户选择 CSV 格式导出
- **THEN** 触发 GET /api/contents/export?format=csv 下载

#### Scenario: 导出后留在页面
- **WHEN** 导出完成
- **THEN** 用户仍在内容列表页
