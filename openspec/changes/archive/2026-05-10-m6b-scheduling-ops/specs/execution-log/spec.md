# Execution Log（执行日志）

**日期**: 2026-05-10
**M6B Change**: `m6b-scheduling-ops`
**状态**: 规划中

---

## ADDED Requirements

### Requirement: 执行日志记录

系统 SHALL 在每次爬取开始时插入一条 ExecutionLog 记录，status=RUNNING，startedAt=当前时间，triggerType 标记触发来源（SCHEDULED/MANUAL）。

### Requirement: 执行日志更新

系统 SHALL 在爬取结束时更新 ExecutionLog：status 更新为 SUCCESS/FAILED，finishedAt=当前时间，itemsCrawled=成功条数，errorMessage=错误信息（若有），durationMs=耗时毫秒。

### Requirement: 执行日志查询 API

系统 SHALL 提供 GET /api/executions?taskId={id}&page=0&size=20 分页查询接口，返回指定任务的所有执行记录，按 startedAt 倒序排列。

### Requirement: 执行日志详情 API

系统 SHALL 提供 GET /api/executions/{id} 详情接口，返回单次执行的完整信息。

### Requirement: 爬虫线程池独立配置

系统 SHALL 使用 spider.crawler.thread-pool-size 配置 crawlTaskExecutor 线程池大小，与 schedulerExecutor 完全独立。

### Requirement: Jsoup 请求超时配置

系统 SHALL 使用 spider.crawler.timeout 配置 Jsoup 请求超时（毫秒），默认 30000（30秒），同时作用于 connectTimeout 和 readTimeout。

### Requirement: 调度触发 INFO 日志

SpiderSchedulerService 触发任务时 SHALL 打印 INFO 级别日志，包含 taskId、triggerType、startTime。

---

## ADDED Scenarios

#### Scenario: 调度触发产生执行日志
- **WHEN** SpiderSchedulerService.scanAndTrigger() 发现应触发的任务并调用 crawlerEngine.execute()
- **THEN** 系统插入 ExecutionLog(taskId, triggerType=SCHEDULED, status=RUNNING, startedAt)
- **AND** 爬取完成后更新 ExecutionLog(status=SUCCESS/FAILED, finishedAt, itemsCrawled, durationMs)

#### Scenario: 手动触发产生执行日志
- **WHEN** 用户调用 POST /api/tasks/{id}/run 手动执行任务
- **THEN** 系统插入 ExecutionLog(taskId, triggerType=MANUAL, status=RUNNING, startedAt)
- **AND** 爬取完成后更新 ExecutionLog(status=SUCCESS/FAILED, finishedAt, itemsCrawled, durationMs)

#### Scenario: 分页查询执行历史
- **WHEN** 用户调用 GET /api/executions?taskId=1&page=0&size=20
- **THEN** 系统返回该任务最近 20 条执行记录，按 startedAt 倒序

#### Scenario: 查询执行详情
- **WHEN** 用户调用 GET /api/executions/123
- **THEN** 系统返回 id=123 的执行记录，包含 errorMessage（若有）

#### Scenario: 爬取失败时记录错误信息
- **WHEN** 爬取过程中抛出异常（非网络异常）
- **THEN** ExecutionLog.status=FAILED，errorMessage=异常信息（截断至 TEXT 长度）

#### Scenario: Jsoup 请求超时
- **WHEN** 爬取过程中网络响应慢，超过 spider.crawler.timeout 配置
- **THEN** Jsoup 抛出 IOException，ExecutionLog.status=FAILED，errorMessage="Connection timeout"
