## Why

M6A 调度核心已完成（Spring @Scheduler + Cron 触发），但缺少执行日志持久化和查询能力。运维人员无法查看任务历史执行记录、排查爬取失败原因、统计爬取成功率。调度可观测性是运维交付的必备能力。

## What Changes

- 新增 `ExecutionLog` Entity，记录每次爬取执行的开始/结束时间、状态、耗时、爬取条数、错误信息
- 修改爬取逻辑：在 `CrawlerEngine` 执行开始时插入 `ExecutionLog(status=RUNNING)`，结束时更新为 `SUCCESS/FAILED` 及相关字段
- 新增 `ExecutionLogService` 封装日志写入和查询逻辑
- 新增 `ExecutionController` 提供 `/api/executions` 分页查询和 `/api/executions/{id}` 详情接口
- 配置项 `spider.crawler.thread-pool-size` 使 `crawlTaskExecutor` 与 `schedulerExecutor` 线程池完全独立可配
- 确认 Jsoup 请求配置了 `connectTimeout` + `readTimeout`（默认 30s）
- 调度触发时打印 INFO 日志（taskId、triggerType、startTime）

## Capabilities

### New Capabilities

- `execution-log`: 执行日志持久化和查询，支持查看历史执行记录和排查问题
  - `scheduling-execution-log`: 调度执行日志记录（插入、更新）+ 查询 API

### Modified Capabilities

- （无 - 现有 scheduling 能力的实现细节变更，不影响需求契约）

## Impact

- **新增 Entity**: `ExecutionLog`（对应数据库表 `execution_logs`）
- **新增 API**: `GET /api/executions?taskId={id}&page=0&size=20`、`GET /api/executions/{id}`
- **修改代码**: `CrawlerEngine`（插入/更新 ExecutionLog）
- **新增代码**: `ExecutionLogService`、`ExecutionController`、`ExecutionLogRepository`
- **配置变更**: `spider.crawler.thread-pool-size`（默认 10）
- **依赖**: M6A（SpiderSchedulerService 已实现触发逻辑）
