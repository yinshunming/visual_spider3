# Scheduling（定时调度核心）

**日期**: 2026-05-10
**M6A Change**: `m6a-scheduling-core`
**状态**: 规划中

---

## ADDED Requirements

### Requirement: 调度开关配置

系统 SHALL 提供 `spider.schedule.enabled` 配置项控制调度是否启用，默认为 `true`。

### Requirement: 调度线程池独立配置

系统 SHALL 提供 `spider.schedule.thread-pool-size` 配置调度线程池大小，默认为 `3`，与爬取线程池（`crawler.thread-pool-size=10`）完全分离。

### Requirement: 爬虫重试次数配置

系统 SHALL 提供 `spider.crawler.retry-times` 配置爬虫失败重试次数，默认为 `2`，最大 `5`。

### Requirement: 爬虫超时配置

系统 SHALL 提供 `spider.crawler.timeout` 配置爬虫请求超时（毫秒），默认为 `30000`（30秒）。

### Requirement: 定时扫描与触发判断

系统 SHALL 每 60 秒扫描一次数据库，查询条件为 `status=ENABLED AND schedule_cron IS NOT NULL AND status != RUNNING`，对每条任务使用 `CronExpression.nextExecution()` 计算下次触发时间，若当前时间 >= 下次触发时间则触发爬取。

### Requirement: 乐观锁防重并发

调度触发前 SHALL 检查 `status != RUNNING`，更新为 `RUNNING` 时再次检查状态，防止并发重复触发同一任务。

### Requirement: 异步执行爬取

触发后系统 SHALL 调用已存在的 `CrawlerEngine.run()` 方法（已配置 `@Async` + `crawlTaskExecutor`），爬取逻辑与调度逻辑线程池分离。

### Requirement: 手动循环重试机制

爬取失败后系统 SHALL 执行手动循环重试，重试次数为 `retry-times`，重试间隔采用指数退避策略（1s → 2s → 4s），仅网络异常（`CrawlException`）触发重试，配置错误、校验失败不重试。

### Requirement: 异常时状态回滚

爬取最终失败后（所有重试耗尽）系统 SHALL 将任务状态回滚为 `ENABLED`，并打印 error 日志记录错误信息。

### Requirement: Cron 表达式合法性校验

SpiderTask 保存时（create / update）系统 SHALL 使用 `CronExpression.parse()` 解析 `schedule_cron` 字段，非法表达式（如 `abc`）应抛出 `IllegalArgumentException`，返回 HTTP 400 Bad Request。

### Requirement: Cron 表达式最小间隔限制

系统 SHALL 限制 `schedule_cron` 最小触发间隔为 1 分钟，禁止每秒级别 cron（`"* * * * *"`）。

---

## ADDED Scenarios

#### Scenario: 调度扫描发现应触发任务
- **WHEN** 调度器每 60 秒扫描时，发现某 ENABLED 任务 `schedule_cron="0 0 * * * *"` 且上次触发时间已过 1 小时
- **THEN** 系统检查任务状态不为 RUNNING，更新为 RUNNING，调用 CrawlerEngine.run() 异步执行

#### Scenario: 任务正在运行时跳过调度
- **WHEN** 调度器扫描时发现某任务状态为 RUNNING（可能手动执行中）
- **THEN** 系统跳过该任务，不重复触发

#### Scenario: 爬取失败后重试
- **WHEN** 爬取过程中抛出 CrawlException（网络超时），retry-times=2
- **THEN** 系统等待 1 秒后第一次重试，等待 2 秒后第二次重试，第二次仍失败则回滚状态为 ENABLED

#### Scenario: 非网络异常不重试
- **WHEN** 爬取过程中抛出配置错误或解析异常（非 CrawlException）
- **THEN** 系统直接回滚状态为 ENABLED，不进行重试

#### Scenario: 非法 Cron 保存时被拒绝
- **WHEN** 用户创建/更新任务时 `schedule_cron="abc"`
- **THEN** 系统抛出 IllegalArgumentException，返回 400 Bad Request，错误信息提示 cron 表达式非法

#### Scenario: 每秒级 Cron 保存时被拒绝
- **WHEN** 用户创建/更新任务时 `schedule_cron="* * * * *"`
- **THEN** 系统抛出 IllegalArgumentException，返回 400 Bad Request，错误信息提示最小间隔为 1 分钟
