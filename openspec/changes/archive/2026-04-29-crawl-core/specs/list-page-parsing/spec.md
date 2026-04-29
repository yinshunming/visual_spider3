## ADDED Requirements

### Requirement: 列表页解析器
CrawlerEngine SHALL support LIST_PAGE mode which parses a list page to extract content page URLs.

The system SHALL use containerSelector to find container elements, then extract URLs from each item using itemUrlSelector.

#### Scenario: 解析列表页提取内容页URL
- **WHEN** CrawlerEngine executes a task in LIST_PAGE mode
- **THEN** it SHALL fetch the list_page_url and parse using containerSelector
- **AND** extract href attributes from elements matching itemUrlSelector
- **AND** return a list of content page URLs

#### Scenario: 容器选择器未匹配到元素
- **WHEN** containerSelector matches no elements
- **THEN** return an empty list of URLs
- **AND** log a warning message

#### Scenario: URL选择器未匹配到元素
- **WHEN** itemUrlSelector matches no elements within a container
- **THEN** skip that container element
- **AND** continue processing other containers

### Requirement: 列表页HTML获取
The system SHALL fetch list page HTML using Jsoup with proper request configuration.

#### Scenario: 成功获取列表页
- **WHEN** making HTTP request to list_page_url
- **THEN** set User-Agent header to avoid blocking
- **AND** set connection timeout to 30 seconds
- **AND** return parsed Jsoup Document

#### Scenario: 列表页请求失败
- **WHEN** HTTP request to list_page_url fails (network error, 4xx, 5xx)
- **THEN** throw CrawlException with descriptive message
- **AND** log the error details
