# M6A：调度核心 - Spring @Scheduler 集成

**日期**: 2026-05-10
**状态**: 规划中
**Change**: `m6a-scheduling-core`
**标签**: m6, scheduling

---

## Why

当前系统 SpiderTask 已有 `schedule_cron` 字段，但定时调度能力尚未实现。运营配置 Cron 表达式后无法自动触发爬取，需要手动执行。M6A 将实现 Spring @Scheduler 定时调度核心，使配置了 Cron 的 ENABLED 任务按表达式自动运行，无需人工干预。

---

## What Changes

- **新增调度线程池**：`ThreadPoolTaskScheduler`（size=3），与 M3 已实现的 `crawlTaskExecutor`（size=10）完全分离
- **新增配置项**：`spider.schedule.enabled`、`spider.schedule.thread-pool-size`、`spider.crawler.retry-times`、`spider.crawler.timeout`
- **新增 SpiderSchedulerService**：每 60 秒扫描 ENABLED + `schedule_cron` IS NOT NULL + `status != RUNNING` 的任务，用 `CronExpression.nextExecution()` 判断是否应触发
- **新增 Cron 合法性校验**：SpiderTask 保存时用 `CronExpression.parse()` 校验 `schedule_cron`，非法返回 400
- **触发流程**：触发前用乐观锁检查 `status != RUNNING`，更新为 RUNNING 后调用 `CrawlerEngine.run()`（@Async）
- **重试机制**：手动循环重试，`retry-times` 次，指数退避 1s→2s→4s，仅 `CrawlException`（网络异常）重试
- **错误处理**：爬取失败时任务状态回滚 ENABLED，打印 error 日志

---

## Capabilities

### New Capabilities

- `scheduling`: 定时调度核心，支持 Cron 表达式触发爬虫任务
  - 配置项管理（schedule enabled、thread-pool-size、retry-times、timeout）
  - 定时扫描与触发判断（每 60 秒扫描一次）
  - Cron 表达式合法性校验
  - 乐观锁防重并发触发
  - 手动循环重试 + 指数退避
  - 异常时状态回滚与错误日志

### Modified Capabilities

- 无（M1-M5 已实现的 SpiderTask、CrawlerEngine 无需求变更）

---

## Impact

**新增文件**:
- `backend/src/main/java/com/example/visualspider/config/SchedulerConfig.java`
- `backend/src/main/java/com/example/visualspider/service/SpiderSchedulerService.java`

**修改文件**:
- `backend/src/main/java/com/example/visualspider/VisualSpiderApplication.java`（加 `@EnableScheduling`）
- `backend/src/main/resources/application.yml`（新增配置项）
- `backend/src/main/java/com/example/visualspider/service/SpiderTaskService.java`（Cron 校验）

**依赖**:
- Spring Boot 内置 `org.springframework.scheduling.support.CronExpression`
- Spring 内置 `@EnableScheduling`、`@Scheduled`
- M3 已实现的 `CrawlerEngine`、`crawlTaskExecutor`

**不影响**:
- ExecutionLog 持久化（m6b 范围）
- 分布式调度、多实例抢锁
- 前端界面
