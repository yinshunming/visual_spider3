## ADDED Requirements

### Requirement: 任务执行入口
POST /api/tasks/{id}/run SHALL initiate asynchronous crawling for the specified task.

#### Scenario: 启用状态任务可以执行
- **WHEN** POST /api/tasks/{id}/run is called
- **AND** task status is ENABLED
- **THEN** return 200 OK immediately
- **AND** start crawling asynchronously
- **AND** task status changes to RUNNING during execution

#### Scenario: 非启用状态任务不能执行
- **WHEN** POST /api/tasks/{id}/run is called
- **AND** task status is DRAFT or DISABLED
- **THEN** return 400 Bad Request
- **AND** error message explaining task must be ENABLED

### Requirement: 执行模式路由
CrawlerEngine SHALL route to appropriate parser based on task urlMode.

#### Scenario: LIST_PAGE模式路由
- **WHEN** task urlMode is LIST_PAGE
- **THEN** use ListPageParser to extract content URLs
- **AND** then use ContentPageExtractor for each URL

#### Scenario: DIRECT_URL模式路由
- **WHEN** task urlMode is DIRECT_URL
- **THEN** use seedUrls directly as content URLs
- **AND** skip list page parsing

### Requirement: 内容保存
The system SHALL save extracted content to ContentItem with PENDING status.

#### Scenario: 保存爬取内容
- **WHEN** content is successfully extracted from a page
- **THEN** create ContentItem with taskId, sourceUrl, extracted fields as JSON
- **AND** set status to PENDING
- **AND** set publishedAt to current timestamp

#### Scenario: 爬取完成后更新任务状态
- **WHEN** crawling completes (all pages processed or error)
- **THEN** update task status to ENABLED
- **AND** log completion summary (pages processed, items extracted)

### Requirement: 执行异常处理
The system SHALL handle crawl errors gracefully without crashing.

#### Scenario: 单页爬取失败不影响整体
- **WHEN** a single page fails to crawl
- **THEN** log the error
- **AND** continue with remaining pages
- **AND** record failed URLs for retry

#### Scenario: 严重错误终止任务
- **WHEN** a critical error occurs (network completely down)
- **THEN** stop crawling
- **AND** set task status back to ENABLED
- **AND** log error summary
