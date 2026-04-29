## ADDED Requirements

### Requirement: 内容页字段提取
ContentPageExtractor SHALL extract field values from content page HTML based on SpiderField configurations.

For each SpiderField, the system SHALL use selector to locate element and extract value using extractType.

#### Scenario: 提取文本内容
- **WHEN** extractType is "text"
- **THEN** extract text content from element matching selector
- **AND** return trimmed text value

#### Scenario: 提取属性值
- **WHEN** extractType is "attr"
- **THEN** extract attribute value specified by attrName
- **AND** return attribute value (e.g., href for links, src for images)

#### Scenario: 提取HTML内容
- **WHEN** extractType is "html"
- **THEN** extract inner HTML of element matching selector
- **AND** return raw HTML string

#### Scenario: 选择器未匹配
- **WHEN** selector matches no elements
- **THEN** return defaultValue if configured
- **AND** return null if no defaultValue

### Requirement: XPath选择器支持
The system SHALL support XPath selectors when selectorType is XPATH.

#### Scenario: XPath选择器解析
- **WHEN** selectorType is XPATH
- **THEN** parse selector using XPath
- **AND** evaluate against document
- **AND** return matched value

### Requirement: 字段提取顺序
The system SHALL extract fields in display_order sequence.

#### Scenario: 按顺序提取字段
- **WHEN** extracting multiple fields from content page
- **THEN** process fields sorted by displayOrder ascending
- **AND** maintain field order in extracted result
