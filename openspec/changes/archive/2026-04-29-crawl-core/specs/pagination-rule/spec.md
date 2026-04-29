## ADDED Requirements

### Requirement: 分页规则类型
The paginationRule SHALL support three types: INFINITE_SCROLL, PAGE_NUMBER, NEXT_BUTTON.

#### Scenario: INFINITE_SCROLL类型
- **WHEN** paginationRule.type is INFINITE_SCROLL
- **THEN** CrawlerEngine SHALL detect when to stop pagination based on no new items
- **AND** continue until containerSelector returns empty results

#### Scenario: PAGE_NUMBER类型
- **WHEN** paginationRule.type is PAGE_NUMBER
- **THEN** CrawlerEngine SHALL generate page URLs using pagePattern
- **AND** replace {page} placeholder with page numbers starting from 1
- **AND** stop when pagePattern URL returns empty results

#### Scenario: NEXT_BUTTON类型
- **WHEN** paginationRule.type is NEXT_BUTTON
- **THEN** CrawlerEngine SHALL find next page button using nextPageSelector
- **AND** click/use the next link to navigate
- **AND** stop when nextPageSelector returns no elements

### Requirement: 分页停止条件
The pagination SHALL stop when no new content URLs can be extracted.

#### Scenario: 空容器停止分页
- **WHEN** containerSelector returns empty elements
- **THEN** stop pagination immediately
- **AND** return all collected URLs

#### Scenario: 最大页数限制
- **WHEN** pagination reaches maxPages limit (if configured)
- **THEN** stop pagination
- **AND** return all collected URLs
