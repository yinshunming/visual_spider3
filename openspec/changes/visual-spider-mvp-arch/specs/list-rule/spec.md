# list-rule

## ADDED Requirements

### Requirement: Page render and screenshot
系统 SHALL 使用 Playwright 渲染目标页面并生成截图。

#### Scenario: Render page
- **WHEN** 用户输入目标URL
- **THEN** Playwright 加载页面并返回截图

### Requirement: Container selector
系统 SHALL 支持用户通过点击选择列表容器区域。

#### Scenario: Click container
- **WHEN** 用户在截图上点击列表容器区域
- **THEN** 系统生成列表容器的候选选择器

### Requirement: Pagination selector
系统 SHALL 支持用户定义分页选择器。

#### Scenario: Define pagination
- **WHEN** 用户点击分页元素并定义选择器
- **THEN** 系统保存分页选择器

### Requirement: Item field selector
系统 SHALL 支持用户点击列表项中的字段（标题、链接、时间），系统生成候选选择器。

#### Scenario: Click item field
- **WHEN** 用户点击列表项中的标题/链接/时间
- **THEN** 系统生成该字段的候选选择器列表

### Requirement: Selector preview
系统 SHALL 支持预览列表数据提取结果。

#### Scenario: Preview list data
- **WHEN** 用户选择候选选择器后点击预览
- **THEN** 系统提取列表数据并展示预览结果

### Requirement: Save list rule
系统 SHALL 支持保存列表规则配置。

#### Scenario: Save rule
- **WHEN** 用户确认选择器配置
- **THEN** 系统保存列表规则并返回规则ID
