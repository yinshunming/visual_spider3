# scheduler

## ADDED Requirements

### Requirement: Quartz scheduling
系统 SHALL 支持 Quartz 定时触发爬取任务。

#### Scenario: Schedule task
- **WHEN** 用户配置任务的调度策略（Cron表达式）
- **THEN** Quartz 在指定时间触发任务执行

### Requirement: Manual trigger
系统 SHALL 支持手动立即触发任务执行。

#### Scenario: Trigger now
- **WHEN** 用户点击"立即执行"按钮
- **THEN** 系统立即开始执行爬取任务

### Requirement: List extraction
系统 SHALL 根据列表规则提取列表数据。

#### Scenario: Extract list
- **WHEN** 任务触发执行
- **THEN** 系统加载列表规则，提取标题、链接、时间

### Requirement: Pagination handling
系统 SHALL 根据分页规则处理分页翻页。

#### Scenario: Handle pagination
- **WHEN** 列表页存在分页
- **THEN** 系统根据分页规则翻页并继续提取

### Requirement: Detail extraction
系统 SHALL 遍历列表链接，加载详情规则提取详情数据。

#### Scenario: Extract detail
- **WHEN** 系统获取到列表项链接
- **THEN** 系统加载详情规则，提取详情数据

### Requirement: Upsert storage
系统 SHALL 使用 Upsert 方式入库，保证幂等性。

#### Scenario: Upsert data
- **WHEN** 提取到数据
- **THEN** 系统根据唯一标识判断 Insert 或 Update

### Requirement: Execution logging
系统 SHALL 记录每次执行的日志。

#### Scenario: Log execution
- **WHEN** 任务执行完成
- **THEN** 系统记录执行状态、耗时、提取数量到 crawl_log 表
