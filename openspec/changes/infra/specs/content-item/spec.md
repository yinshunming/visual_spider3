## ADDED Requirements

### Requirement: ContentItem Entity
爬取内容实体，存储采集到的具体内容

#### Scenario: 创建内容实体
- **WHEN** 开发者创建 ContentItem 实体
- **THEN** 实体包含 id、task_id、source_url、fields、raw_html、status、published_at、created_at 字段

#### Scenario: 动态字段存储
- **WHEN** 保存内容字段值
- **THEN** fields 使用 JSONB 格式，key 为字段名，value 为字段值

#### Scenario: 原始HTML存储
- **WHEN** 需要保存原始HTML
- **THEN** raw_html 字段存储页面完整HTML（可选）

#### Scenario: 内容状态枚举
- **WHEN** 设置内容状态
- **THEN** 值必须为 PUBLISHED、PENDING 或 DELETED 之一

#### Scenario: 内容关联任务
- **WHEN** 内容关联到任务
- **THEN** task_id 指向 SpiderTask 的 id

#### Scenario: 来源追踪
- **WHEN** 记录内容来源
- **THEN** source_url 存储原始内容页URL

### Requirement: ContentItem Repository
ContentItem 的数据访问接口

#### Scenario: 按任务查询内容
- **WHEN** 查询某任务的所有内容
- **THEN** 提供 findByTaskId 方法

#### Scenario: 分页查询
- **WHEN** 分页查询内容列表
- **THEN** 提供 Pageable 支持

#### Scenario: 按状态查询
- **WHEN** 查询某状态的内容
- **THEN** 提供 findByStatus 方法

#### Scenario: 内容 CRUD
- **WHEN** 调用 ContentItemRepository
- **THEN** 提供 save、findById、delete 方法
