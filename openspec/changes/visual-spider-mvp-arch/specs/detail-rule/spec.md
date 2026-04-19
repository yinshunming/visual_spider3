# detail-rule

## ADDED Requirements

### Requirement: Navigate to detail page
系统 SHALL 支持用户选择一个列表项后跳转到详情页。

#### Scenario: Click list item
- **WHEN** 用户点击某个列表项
- **THEN** 系统打开该链接对应的详情页

### Requirement: Detail page render
系统 SHALL 使用 Playwright 渲染详情页并生成截图。

#### Scenario: Render detail page
- **WHEN** 详情页加载完成
- **THEN** Playwright 返回页面截图

### Requirement: Field selector
系统 SHALL 支持用户点击详情页中的各字段区域（标题、正文、作者、标签），系统生成候选选择器。

#### Scenario: Click field area
- **WHEN** 用户点击详情页中的字段区域
- **THEN** 系统生成该字段的候选选择器列表

### Requirement: Detail preview
系统 SHALL 支持预览详情数据提取结果。

#### Scenario: Preview detail data
- **WHEN** 用户选择候选选择器后点击预览
- **THEN** 系统提取详情数据并展示预览结果

### Requirement: Save detail rule
系统 SHALL 支持保存详情规则配置。

#### Scenario: Save detail rule
- **WHEN** 用户确认所有字段选择器配置
- **THEN** 系统保存详情规则并返回规则ID
