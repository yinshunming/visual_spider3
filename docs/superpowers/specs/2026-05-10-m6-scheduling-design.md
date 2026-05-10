# M6：调度与发布 — 设计方案与任务拆分

**日期**: 2026-05-10
**状态**: 已完成（m6a ✅, m6b ✅）
**前置里程碑**: M5 已完成

---

## 0. 约束与前提

- M1～M5 已全部完成（M1 基础设施、M2 任务管理、M3 爬虫核心、M4 可视化配置、M5 内容管理）
- SpiderTask 已有 `schedule_cron` 字段（String）和 `RUNNING` 状态
- M3 已实现 `CrawlerEngine` + `@Async` + `crawlTaskExecutor`
- **不重做 M1～M5 设计**
- **不扩大 MVP 范围**（不引入分布式、代理池、反爬对抗）
- **不写代码**，仅设计 + 任务拆分

---

## 1. M6 范围收敛

### Must（无此功能 MVP 不完整，不做无法交付）

| 交付项 | 说明 |
|--------|------|
| Spring @Scheduler 集成 | 启动类加 `@EnableScheduling`，配置调度开/关 |
| Cron 表达式解析与校验 | 用 `CronExpression`（Spring 内置）解析，保存时校验合法性 |
| 定时调度执行 | 定时扫描 ENABLED + 有 cron 的任务，触发爬取 |
| 任务执行日志（内存/in-memory） | 记录每次触发的开始/结束/成功/失败/耗时；可先存内存，后续持久化 |
| 错误处理 | 捕获爬取异常，任务状态回滚 ENABLED，不污染调度器 |
| 重试机制 | 配置 retry-times，失败后自动重试 N 次 |

### Should（显著提升稳健性，强烈建议本阶段做）

| 交付项 | 说明 |
|--------|------|
| 执行日志持久化 | ExecutionLog Entity + 表，查询历史执行记录 API |
| 并发控制 | 调度线程池与爬取线程池分离，防止同时爬取过多站点 |
| 连接池复用检查 | 确认 Jsoup 请求有 connectTimeout + readTimeout |
| 手动执行与调度互不干扰 | /run 接口独立；调度触发时检查任务是否已在 RUNNING 状态 |
| 乐观锁防重 | 触发前检查 `status != RUNNING`，避免重复触发 |

### Could（本阶段不建议做）

| 交付项 | 原因 |
|--------|------|
| 分布式调度（多实例抢锁） | MVP 单实例足够；引入 Redis/JVM 锁增加部署复杂度 |
| 代理池 / IP 轮换 | 反爬对抗是独立话题；MVP 阶段 User-Agent + crawl-delay 足够 |
| 实时告警（钉钉/飞书/邮件） | 需要告警渠道配置；日志级别 + 管理后台可替代 |
| 实时 WebSocket 进度推送 | 前端已有手动刷新；优先级低于调度核心 |
| 监控 Dashboard | ExecutionLog API + Grafana 简单看板可替代 |
| Cron 可视化编辑器 | 让用户直接输入标准 cron 表达式，降低前端工作量 |
| 任务超时强制中断 | 需要线程中断机制；当前 @Async + Future.get(timeout) 可间接实现 |
| 分布式爬虫 | 多节点协调复杂度高，超出 MVP 范围 |

---

## 2. 推荐技术设计

### 2.1 调度框架

**Spring @Scheduler（内置，无需引入 XXL-JOB 等外部框架）**

- `@EnableScheduling` 加到启动类或配置类
- **调度线程池**：`ThreadPoolTaskScheduler`，默认大小 3
- 与爬取线程池（`crawlTaskExecutor`，M3 已配，大小 10）**完全分离**，各自独立配置

```
# 配置项
spider:
  schedule:
    enabled: true
    thread-pool-size: 3
  crawler:
    thread-pool-size: 10
    retry-times: 2
    timeout: 30000
```

### 2.2 Cron 配置与解析

- **解析**：`org.springframework.scheduling.support.CronExpression`（Spring 内置，无需引入 Cron-utils）
- **校验时机**：SpiderTask 保存时（create / update）解析 `schedule_cron`，非法 cron 抛 `IllegalArgumentException`，返回 400
- **安全限制**：不允许每秒级别（`* * * * *`），建议在配置或代码层面限制最小间隔为 1 分钟
- **触发判断**：用 `CronTrigger.nextExecution()` 计算下次触发时间，"当前时间 >= 下次触发时间" 则触发

### 2.3 定时触发逻辑

```java
@Scheduled(fixedRate = 60000) // 每 60 秒扫描一次
void scanAndTrigger() {
    // 1. 查询 ENABLED + schedule_cron IS NOT NULL + status != RUNNING 的任务
    // 2. 对每条任务：用 CronExpression.nextExecution() 计算下次触发时间
    // 3. 如果 "当前时间 >= 下次触发时间"，提交到 crawlTaskExecutor 异步执行
}
```

### 2.4 任务执行日志（ExecutionLog Entity）

**Entity：ExecutionLog**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| task_id | BIGINT | 关联任务 ID |
| trigger_type | ENUM | SCHEDULED / MANUAL |
| started_at | TIMESTAMP | 开始时间 |
| finished_at | TIMESTAMP | 结束时间（null = 运行中） |
| status | ENUM | RUNNING / SUCCESS / FAILED |
| items_crawled | INT | 成功爬取条数 |
| error_message | TEXT | 失败原因（截断） |
| duration_ms | BIGINT | 耗时毫秒 |

**API（新建）**：

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/executions | 分页查询执行历史（支持 taskId 过滤） |
| GET | /api/executions/{id} | 执行详情 |

### 2.5 错误处理与重试

**错误处理原则**：
- 爬虫异常（`CrawlException`）→ 记录日志 → 更新 ExecutionLog status=FAILED → 任务状态回滚 ENABLED
- 网络超时、解析失败均属 `CrawlException`
- 调度器自身异常（扫描逻辑）→ 打印 error 日志，不影响其他任务

**重试机制**：
- 配置项：`spider.crawler.retry-times`（默认 2，最大 5）
- 使用手动循环，不引入 Spring Retry 额外依赖
- 重试间隔：指数退避（1s → 2s → 4s）
- 重试条件：仅网络异常；配置错误、校验失败不重试

```java
for (int i = 0; i <= retryTimes; i++) {
    try {
        doCrawl();
        break;
    } catch (NetworkException e) {
        if (i == retryTimes) throw e;
        Thread.sleep(1000L * (1 << i)); // 1, 2, 4 秒
    }
}
```

### 2.6 并发控制

**两层线程池分离**：

| 线程池 | 用途 | 默认大小 | 配置项 |
|--------|------|----------|--------|
| `crawlTaskExecutor`（M3 已配） | 实际爬取（HTTP + 解析） | 10 | `spider.crawler.thread-pool-size` |
| `schedulerExecutor`（M6 新增） | 定时扫描 + 触发判断 | 3 | `spider.schedule.thread-pool-size` |

**防并发措施**：
- 查询任务时加 `AND status != 'RUNNING'`，防止重复触发
- 用乐观锁（状态更新前再次检查），不用悲观锁

### 2.7 连接池

- Jsoup 默认每次请求新建连接，无连接池
- **本阶段建议**：先不加连接池，上线后按监控数据决定是否需要
- 确保 Jsoup 请求配置了 `connectTimeout` + `readTimeout`（默认 30s），避免请求hang死

---

## 3. 目录结构（新增部分）

```
backend/src/main/java/com/example/visualspider/
├── config/
│   └── SchedulerConfig.java          # M6: ThreadPoolTaskScheduler Bean
├── service/
│   ├── SpiderSchedulerService.java   # M6: 定时扫描 + 触发判断
│   └── ExecutionLogService.java      # M6: 执行日志写入与查询
├── entity/
│   └── ExecutionLog.java             # M6: 执行日志实体
├── repository/
│   └── ExecutionLogRepository.java   # M6: 执行日志数据访问
└── controller/
    └── ExecutionController.java      # M6: 执行历史 API
```

---

## 4. OpenSpec Changes 拆分

**原则**：避免一个巨大 change，每个控制在 ~9 个任务，专注单一目标。

建议拆分为 **2 个独立的 change**：

| Change | 名称 | 核心交付 |
|--------|------|----------|
| `m6a-scheduling-core` | 调度核心 | 能不能定时跑起来 |
| `m6b-scheduling-ops` | 调度可观测性 | 跑了之后能不能看清楚 |

---

## 5. Change 1：`m6a-scheduling-core`

### 目标

Spring @Scheduler 定时调度能力，支持 Cron 表达式触发爬虫任务，包含重试和错误处理。

### Non-Goals

- 不做 ExecutionLog 持久化和 API
- 不做并发/连接池深度调优
- 不做分布式调度

### 任务草案

| # | 任务 | 验证方式 |
|---|------|----------|
| 1 | 引入配置项：`spider.schedule.enabled`、`thread-pool-size`、`crawler.retry-times`、`crawler.timeout` | `curl` 或测试确认配置可读取 |
| 2 | 创建 `SchedulerConfig`：配置 `ThreadPoolTaskScheduler`，注册为 Bean | `ApplicationContext` 能拿到 scheduler bean |
| 3 | 创建 `SpiderSchedulerService`：实现定时扫描 + `CronExpression` 触发判断 | 单元测试：给一个过去时间的 cron，验证触发 |
| 4 | 启动类或配置类加 `@EnableScheduling` | 启动应用无报错 |
| 5 | 任务触发：调用现有 `CrawlerEngine.run()`（已有 @Async） | 手动给 ENABLED + cron 任务，等待定时触发 |
| 6 | 乐观锁防重：触发前检查 `status != RUNNING`，更新为 RUNNING | 并发测试：同一任务同时触发，只有一个执行 |
| 7 | 重试机制：手动循环 + 指数退避（1s→2s→4s），仅网络异常重试 | Mock 网络异常，验证重试 N 次后记录失败 |
| 8 | 错误处理：异常时任务状态回滚 ENABLED + 打印 error 日志 | 模拟爬取失败，验证状态回滚 |
| 9 | Cron 合法性校验：`CronExpression.parse()` 解析，非法返回 400 | 测试非法 cron（如 `abc`）抛异常 |

### 验收标准

- [ ] `POST /api/tasks` 带 `schedule_cron` 保存成功；非法 cron 返回 400
- [ ] 定时扫描每 60 秒执行一次，ENABLED + cron 任务在应触发时间点被调用
- [ ] 爬取失败后重试 `retry-times` 次，最终失败时任务状态回滚 ENABLED
- [ ] 同一任务并发触发只执行一次（其他跳过或等待）
- [ ] 调度线程池与爬取线程池分离，互不影响

---

## 6. Change 2：`m6b-scheduling-ops`

### 目标

为调度系统添加执行日志持久化和查询 API，支持运维人员查看历史执行记录和排查问题。

### Non-Goals

- 不做分布式调度 / 多实例抢锁
- 不做钉钉 / 邮件 / 飞书告警
- 不做可视化 Cron 编辑器
- 不做实时 WebSocket 进度推送

### 任务草案

| # | 任务 | 验证方式 |
|---|------|----------|
| 1 | 创建 `ExecutionLog` Entity + `ExecutionLogRepository` | JPA 自动建表或手动 DDL |
| 2 | 修改爬取逻辑：开始插入 `ExecutionLog(status=RUNNING)`，结束时更新 `status/finishedAt/itemsCrawled/errorMessage/durationMs` | 执行一次任务，查询 `execution_logs` 表有记录 |
| 3 | 创建 `ExecutionLogService`：封装日志写入和查询 | 单元测试覆盖 |
| 4 | 创建 `ExecutionController`：`GET /api/executions?taskId={id}&page=0` 分页查询 | `curl` 验证分页结果 |
| 5 | `GET /api/executions/{id}` 返回单次执行详情（errorMessage、durationMs、itemsCrawled） | `curl` 验证 |
| 6 | 配置 `spider.crawler.thread-pool-size` 使 `crawlTaskExecutor` 与 scheduler 线程池独立可配 | 启动时两个线程池都初始化 |
| 7 | 确认 Jsoup 请求有 `connectTimeout` + `readTimeout`（默认 30s） | 看代码确认 |
| 8 | 执行记录 API 分页参数（page、size、sort）规范化 | 验证分页、排序 |
| 9 | 调度触发时打印 INFO 日志（taskId、triggerType、startTime） | 查看日志输出 |

### 验收标准

- [ ] `GET /api/executions?taskId={id}` 返回该任务所有执行记录，分页正常
- [ ] `GET /api/executions/{id}` 返回单次执行详情，含耗时、条数、错误信息
- [ ] 爬取完成后 `execution_logs` 表记录准确（status、finished_at、items_crawled）
- [ ] `crawlTaskExecutor` 线程池大小可独立配置，不受 scheduler 影响
- [ ] Jsoup 请求有明确 timeout 配置（connectTimeout、readTimeout 默认 30s）
- [ ] 调度触发时 INFO 日志记录 taskId、triggerType、startTime

---

## 7. `/opsx-propose` 提示词建议

### Change 1 提示词

```
请用 /opsx-propose 创建一个新的 change：

名称：m6a-scheduling-core
标题：调度核心 - Spring @Scheduler 集成
标签：m6, scheduling

目标：
实现 Spring @Scheduler 定时调度能力，支持 Cron 表达式触发爬虫任务。

具体要求：
1. 引入 spider.schedule.enabled、thread-pool-size、crawler.retry-times、crawler.timeout 配置
2. 配置 ThreadPoolTaskScheduler（size=3），与现有 crawlTaskExecutor（M3 已配，size=10）分离
3. 实现 SpiderSchedulerService：每 60 秒扫描 ENABLED + schedule_cron IS NOT NULL + status != RUNNING 的任务，CronExpression.nextExecution() 判断是否应触发
4. 触发前用乐观锁检查 status != RUNNING，更新为 RUNNING 后调用 CrawlerEngine.run()（@Async）
5. 手动循环重试机制：retry-times 次，指数退避 1s→2s→4s，仅网络异常（CrawlException）重试
6. 异常时任务状态回滚 ENABLED，打印 error 日志
7. 保存任务时用 CronExpression.parse() 校验 schedule_cron 合法性，非法返回 400

Non-Goals：
- 不做 ExecutionLog 持久化和 API
- 不做并发/连接池深度调优
- 不做分布式调度

参考：M3 已实现 CrawlerEngine + crawlTaskExecutor；SpiderTask 已有 schedule_cron 字段和 RUNNING 状态。
```

### Change 2 提示词

```
请用 /opsx-propose 创建一个新的 change：

名称：m6b-scheduling-ops
标题：调度可观测性 - 执行日志与运维增强
标签：m6, scheduling, ops

目标：
为调度系统添加执行日志持久化和查询 API，支持查看历史执行记录和排查问题。

具体要求：
1. 创建 ExecutionLog Entity（taskId, triggerType, startedAt, finishedAt, status, itemsCrawled, errorMessage, durationMs）+ ExecutionLogRepository
2. 修改爬取逻辑：开始时插入 ExecutionLog(status=RUNNING)，结束时更新 status=SUCCESS/FAILED + finishedAt + itemsCrawled + errorMessage + durationMs
3. 创建 ExecutionLogService 封装日志写入和查询
4. 创建 ExecutionController：
   - GET /api/executions?taskId={id}&page=0&size=20 — 分页查询执行历史
   - GET /api/executions/{id} — 单次执行详情
5. 配置 spider.crawler.thread-pool-size 使 crawlTaskExecutor 与 scheduler 线程池独立可配
6. 确认 Jsoup 请求有 connectTimeout + readTimeout（默认 30s）
7. 调度触发时打印 INFO 日志（taskId, triggerType, startTime）

Non-Goals：
- 不做分布式调度/多实例抢锁
- 不做钉钉/邮件/飞书告警
- 不做可视化 Cron 编辑器
- 不做实时 WebSocket 进度推送

前置依赖：m6a-scheduling-core 已完成（ExecutionLog 依赖爬取逻辑的调用点）
```

---

## 8. 架构草图

```
┌──────────────────────────────────────────────────────────┐
│              Spring Boot Application                      │
├────────────────┬────────────────────────────────────────┤
│ Scheduler      │  ThreadPoolTaskScheduler (size=3)       │
│ (@Scheduled    │  每 60 秒扫描 ENABLED + cron 任务        │
│  fixedRate=60s)│  CronExpression.nextExecution()        │
├────────────────┴────────────────────────────────────────┤
│  SpiderSchedulerService                                 │
│   - scanAndTrigger() → 乐观锁检查 → crawlTaskExecutor   │
│   - shouldTrigger(task) → boolean                       │
│   - 重试循环 (retry-times, 指数退避)                    │
├──────────────────────────────────────────────────────────┤
│  crawlTaskExecutor (size=10, M3 已配)                   │
│   CrawlerEngine.run(taskId)                            │
├──────────────────────────────────────────────────────────┤
│  ExecutionLog ← 写入                                     │
│  (taskId, triggerType, startedAt, finishedAt,           │
│   status, itemsCrawled, errorMessage, durationMs)       │
└────────────────────────┬───────────────────────────────┘
                          │
                   ExecutionController
                   GET /api/executions
                   GET /api/executions/{id}
```

---

## 9. 配置项汇总

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spider.schedule.enabled` | `true` | 调度总开关 |
| `spider.schedule.thread-pool-size` | `3` | 调度扫描线程池大小 |
| `spider.crawler.thread-pool-size` | `10` | 爬取线程池大小（M3 已配，可覆盖） |
| `spider.crawler.retry-times` | `2` | 最大重试次数 |
| `spider.crawler.timeout` | `30000` | Jsoup 请求超时（ms） |

---

## 10. 后续步骤

1. 确认本设计方案无误后，分别用上述提示词调用 `/opsx-propose` 创建两个 change
2. 进入 `/opsx-propose` → 生成 proposal → design → tasks → spec artifacts
3. 按 `m6a` 先 `m6b` 后的顺序进入 `/opsx-apply-change` 实现
4. M6 完成后更新 `visual-spider-spec.md` 的 milestone 进度和 `docs/roadmap.md`
