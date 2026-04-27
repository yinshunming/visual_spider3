## ADDED Requirements

### Requirement: SpiderTask Entity
爬虫任务实体，包含任务名称、URL模式、解析规则等核心字段

#### Scenario: 创建任务实体
- **WHEN** 开发者创建 SpiderTask 实体
- **THEN** 实体包含 id、name、description、url_mode、list_page_url、list_page_rule、seed_urls、content_page_rule、schedule_cron、status、created_at、updated_at 字段

#### Scenario: URL模式枚举
- **WHEN** 设置任务的 url_mode
- **THEN** 值必须为 LIST_PAGE 或 DIRECT_URL 之一

#### Scenario: 任务状态枚举
- **WHEN** 设置任务的状态
- **THEN** 值必须为 DRAFT、ENABLED 或 DISABLED 之一

#### Scenario: 列表页规则存储
- **WHEN** 保存列表页解析规则
- **THEN** 使用 JSONB 格式存储 containerSelector、itemUrlSelector、paginationRule

#### Scenario: 内容页规则存储
- **WHEN** 保存内容页解析规则
- **THEN** 使用 JSONB 格式存储 fields 数组

#### Scenario: 直接URL模式
- **WHEN** url_mode 为 DIRECT_URL
- **THEN** seed_urls 存储内容页 URL 列表

### Requirement: SpiderTask Repository
SpiderTask 的数据访问接口

#### Scenario: 基础 CRUD
- **WHEN** 调用 SpiderTaskRepository
- **THEN** 提供 save、findById、findAll、delete 方法

#### Scenario: 按状态查询
- **WHEN** 查询某状态的任务列表
- **THEN** 提供 findByStatus 方法

#### Scenario: 分页查询
- **WHEN** 分页查询任务列表
- **THEN** 提供 Pageable 支持
