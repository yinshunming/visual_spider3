# M6B：调度可观测性 - 技术设计

**日期**: 2026-05-10
**Change**: `m6b-scheduling-ops`

---

## Context

M6A 调度核心已完成（Spring @Scheduler + Cron 触发 + 乐观锁 + 重试），SpiderSchedulerService 可正确触发爬取任务。

当前问题：
- 每次爬取没有持久化执行记录，运维无法查看历史执行情况
- 无法统计爬取成功率、平均耗时等指标
- 爬取失败时只能看日志，无法通过 API 查询失败原因
- AsyncConfig 中 crawlTaskExecutor 线程池大小硬编码为 core=2/max=5，未使用 application.yml 中的 spider.crawler.thread-pool-size=10
- ContentPageExtractor 中 Jsoup timeout 硬编码为 30000，未使用 spider.crawler.timeout 配置

M6B 聚焦调度可观测性：执行日志持久化 + 查询 API + 线程池配置修复。

---

## Goals / Non-Goals

**Goals:**
- ExecutionLog Entity + Repository：记录 taskId、triggerType、startedAt、finishedAt、status、itemsCrawled、errorMessage、durationMs
- 修改 CrawlerEngine：执行开始时插入 ExecutionLog(status=RUNNING)，结束时更新为 SUCCESS/FAILED + finishedAt + itemsCrawled + errorMessage + durationMs
- ExecutionLogService：封装日志写入和查询
- ExecutionController：GET /api/executions?taskId={id}&page=0&size=20 分页查询，GET /api/executions/{id} 详情
- AsyncConfig.crawlTaskExecutor 使用 spider.crawler.thread-pool-size 配置
- ContentPageExtractor.fetchContentPage 使用 spider.crawler.timeout 配置
- SpiderSchedulerService 触发时打印 INFO 日志（taskId、triggerType、startTime）

**Non-Goals:**
- 不做分布式调度/多实例抢锁
- 不做钉钉/邮件/飞书告警
- 不做可视化 Cron 编辑器
- 不做实时 WebSocket 进度推送

---

## Decisions

### 1. ExecutionLog 与 SpiderTask 通过 taskId 关联而非外键约束

**决定**: ExecutionLog.taskId 为普通 BIGINT 字段，不加 @ManyToOne 关联

**理由**:
- 执行日志是历史记录，任务删除后日志仍可保留（审计用途）
- 不加级联删除，避免误删数据
- 查询时通过 taskId 过滤足够，无需实体关联

### 2. triggerType 使用 ENUM 区分 SCHEDULED / MANUAL

**决定**: TriggerType 为枚举字段，区分调度触发和手动触发

**理由**:
- 同一任务可能被手动和调度两种方式触发，日志需要区分
- SpiderTaskController.run() 触发时传入 MANUAL，SpiderSchedulerService 触发时传入 SCHEDULED

### 3. itemsCrawled 记录成功爬取的 ContentItem 数量

**决定**: 爬取完成后统计实际入库的 ContentItem 条数

**理由**:
- successCount + failedCount 在遍历 URL 时统计
- 最终入库数量以 contentService.saveContent() 成功次数为准

### 4. 线程池配置使用 @Value 而非 @ConfigurationProperties

**决定**: AsyncConfig 使用 @Value 注入 spider.crawler.thread-pool-size

**理由**:
- 配置简单，仅一个 Int 值
- 与现有 SchedulerConfig 保持一致

### 5. Jsoup timeout 配置为 connectTimeout + readTimeout

**决定**: Jsoup.connect().timeout(ms) 同时设置 connect 和 read timeout

**理由**:
- Jsoup.timeout(ms) 同时设置 connectTimeout 和 readTimeout
- 默认 30s，与 application.yml 中 spider.crawler.timeout=30000 对齐

---

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|---------|
| ExecutionLog 写入失败导致爬取也跟着失败 | 日志写入使用 try-catch，失败时仅打印 warn，不影响爬取主流程 |
| 高频调度产生大量日志行 | 执行记录按 taskId 分页查询，单次查询不超过 100 条 |
| ThreadPoolTaskExecutor 配置未生效 | 启动时打印 INFO 日志确认配置值 |

---

## Migration Plan

1. **新增 Entity**: JPA 自动建表或手动 DDL `execution_logs`
2. **修改 AsyncConfig**: corePoolSize/maxPoolSize 从 @Value 读取
3. **修改 ContentPageExtractor**: fetchContentPage() 从 @Value 读取 timeout
4. **修改 SpiderSchedulerService**: 触发时传入 triggerType，打印 startTime
5. **回滚**: 注释掉 ExecutionLog 相关代码即可，Entity 不影响已有表
