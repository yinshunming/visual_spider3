# M6A：调度核心 - 技术设计

**日期**: 2026-05-10
**Change**: `m6a-scheduling-core`

---

## Context

M1-M5 已实现可视化爬虫系统基础能力：任务管理（SpiderTask/Field/ContentItem）、爬虫引擎（CrawlerEngine + Jsoup + @Async crawlTaskExecutor）、可视化配置、内容管理。

SpiderTask 已有 `schedule_cron` 字段（String）和 `RUNNING` 状态，但定时调度从未实现。运营配置 Cron 后无法自动触发。

M6A 聚焦调度核心：让配置了有效 Cron 的 ENABLED 任务按表达式自动运行，不做执行日志持久化（m6b 范围）、不做分布式/多实例（超出 MVP）。

---

## Goals / Non-Goals

**Goals:**
- Spring `@EnableScheduling` 启用定时调度
- `ThreadPoolTaskScheduler`（size=3）与 `crawlTaskExecutor`（size=10，M3 已配）完全分离
- 每 60 秒扫描 ENABLED + `schedule_cron IS NOT NULL` + `status != RUNNING` 的任务
- `CronExpression.nextExecution()` 判断是否应触发
- 触发前乐观锁检查 `status != RUNNING`，防止并发重复触发
- 手动循环重试：`retry-times` 次，指数退避 1s→2s→4s，仅 `CrawlException` 重试
- 异常时任务状态回滚 ENABLED，打印 error 日志
- `SpiderTask` 保存时 `CronExpression.parse()` 校验 `schedule_cron` 合法性，非法返回 400

**Non-Goals:**
- 不做 ExecutionLog 持久化和 API（m6b 范围）
- 不做并发/连接池深度调优
- 不做分布式调度（单实例 MVP 足够）
- 不做实时告警、WebSocket 推送

---

## Decisions

### 1. 使用 Spring @Scheduler 而非 XXL-JOB 等外部框架

**决定**: Spring 内置 `@EnableScheduling` + `ThreadPoolTaskScheduler`

**理由**:
- MVP 阶段单实例足够，外部框架引入运维复杂度
- Spring Scheduler 足够应对分钟级定时任务
- `CronTrigger.nextExecution()` 内置，无需引入 Cron-utils

**替代方案**:
- XXL-JOB：功能强大但需要额外部署 executor，不适合 MVP
- Quartz：功能全但配置繁琐，学习成本高

### 2. 调度线程池与爬取线程池完全分离

**决定**: `schedulerExecutor`（size=3）负责扫描+触发判断，`crawlTaskExecutor`（size=10，M3）负责实际爬取

**理由**:
- 职责单一：scheduler 线程池只做轻量级扫描，crawl 线程池做重 I/O 爬取
- 互不影响：爬取慢不影响扫描节拍
- 各自独立配置：`spider.schedule.thread-pool-size` vs `spider.crawler.thread-pool-size`

### 3. 重试机制使用手动循环而非 Spring Retry

**决定**: 手动 `for` 循环 + `Thread.sleep()` 指数退避

**理由**:
- 无额外依赖（Spring Retry 需要 spring-retry 包）
- 重试逻辑简单（仅 3 行 for 循环），自定义退避策略容易
- `CrawlException` 明确，不依赖异常类型判断

### 4. Cron 校验在保存时进行

**决定**: `SpiderTaskService.save()` 时调用 `CronExpression.parse()` 校验

**理由**:
- 用户输入时立即反馈错误，无需等到调度触发才发现
- 400 Bad Request 比运行时异常更友好
- Spring 内置 `CronExpression.parse()` 无需引入额外库

### 5. 防并发用乐观锁（状态检查）而非数据库锁

**决定**: 触发前检查 `status != RUNNING`，更新为 RUNNING 时再次检查

**理由**:
- MVP 单实例，JVM 内并发有限
- 乐观锁比悲观锁（`SELECT FOR UPDATE`）开销小
- `status != RUNNING` 条件查询天然过滤正在运行的任务

---

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|---------|
| 调度线程池满载导致扫描延迟 | thread-pool-size=3 足够小；扫描逻辑轻量（仅 DB 查询+内存判断） |
| 爬取线程池满载导致任务排队 | crawlTaskExecutor size=10 与 scheduler 分离；m6b 考虑队列监控 |
| Cron 表达式合法但语义错误（如 2月30号） | Spring `CronExpression` 会自动处理或抛出异常，运行时可观测 |
| 任务运行中美团-RUNNING 状态，进程重启 | 重启后任务状态仍为 RUNNING，需人工介入；m6b 可加超时检测 |
| 指数退避仍可能连续失败 | retry-times 最大 5 次保护；最终失败回滚状态后可下次 Cron 再次触发 |

---

## Migration Plan

1. **部署前**: 确保 M3 `crawlTaskExecutor` Bean 名称不变，调度器依赖它异步执行
2. **新配置项**: `application.yml` 增加 `spider.schedule.*` 和 `spider.crawler.*` 配置项
3. **启动顺序**: `@EnableScheduling` 在应用启动时立即生效，60 秒内开始首次扫描
4. **回滚**: 注释掉 `@EnableScheduling` 或设置 `spider.schedule.enabled=false` 即可禁用调度

---

## Open Questions

1. **任务超时强制中断**：当前 `@Async` + `Future.get(timeout)` 可间接实现，是否需要显式中断机制？
2. **手动执行与调度冲突**：手动 `/run` 接口触发时是否也要走乐观锁检查？
3. **Cron 最小间隔限制**：是否需要禁止秒级 cron（如 `* * * * *`）？建议限制最小 1 分钟。
