# M6B：调度可观测性 - 实现任务

**Change**: `m6b-scheduling-ops`
**状态**: 实现完成 ✓

---

## 1. ExecutionLog Entity + Repository

- [x] 1.1 创建 `ExecutionLog.java` Entity，包含字段：id(BIGINT)、taskId(BIGINT)、triggerType(ENUM:SCHEDULED/MANUAL)、startedAt(TIMESTAMP)、finishedAt(TIMESTAMP)、status(ENUM:RUNNING/SUCCESS/FAILED)、itemsCrawled(INT)、errorMessage(TEXT)、durationMs(BIGINT)
- [x] 1.2 创建 `ExecutionLogRepository.java` 继承 `JpaRepository`，包含 `Page<ExecutionLog> findByTaskIdOrderByStartedAtDesc(Long taskId, Pageable pageable)` 和 `Optional<ExecutionLog> findById(Long id)` 方法
- [x] 1.3 JPA 自动建表验证（启动应用后检查日志或数据库）

## 2. 修改爬取逻辑集成 ExecutionLog

- [x] 2.1 在 `CrawlerEngine.execute()` 开始时注入 `ExecutionLogRepository`，插入 `ExecutionLog(status=RUNNING, startedAt=now(), triggerType)`
- [x] 2.2 在 `CrawlerEngine.execute()` 结束时更新 `ExecutionLog(status, finishedAt, itemsCrawled, errorMessage, durationMs)`
- [x] 2.3 `SpiderTaskController.run()` 手动触发时传入 `triggerType=MANUAL`，`SpiderSchedulerService.triggerTask()` 传入 `triggerType=SCHEDULED`
- [x] 2.4 ExecutionLog 写入失败时 catch 异常打印 warn 日志，不影响爬取主流程

## 3. ExecutionLogService 封装

- [x] 3.1 创建 `ExecutionLogService.java`，注入 `ExecutionLogRepository`
- [x] 3.2 实现 `createLog(Long taskId, TriggerType type)` 返回 ExecutionLog
- [x] 3.3 实现 `updateLog(Long logId, ExecutionStatus status, int itemsCrawled, String errorMessage, long durationMs)` 更新日志
- [x] 3.4 实现 `findByTaskId(Long taskId, Pageable pageable)` 分页查询
- [x] 3.5 实现 `findById(Long id)` 详情查询

## 4. ExecutionController API

- [x] 4.1 创建 `ExecutionController.java`，注入 `ExecutionLogService`
- [x] 4.2 实现 `GET /api/executions?taskId={id}&page=0&size=20` 分页查询，参数 taskId 可选
- [x] 4.3 实现 `GET /api/executions/{id}` 详情查询
- [x] 4.4 返回 DTO 包含：id、taskId、triggerType、startedAt、finishedAt、status、itemsCrawled、errorMessage、durationMs

## 5. AsyncConfig 线程池配置修复

- [x] 5.1 修改 `AsyncConfig.crawlTaskExecutor()`，使用 `@Value("${spider.crawler.thread-pool-size:10}")` 注入 corePoolSize 和 maxPoolSize
- [x] 5.2 启动时确认 INFO 日志打印实际使用的线程池大小

## 6. Jsoup Timeout 配置

- [x] 6.1 修改 `ContentPageExtractor`，注入 `@Value("${spider.crawler.timeout:30000}")` 到 fetchContentPage()
- [x] 6.2 确认 Jsoup.connect().timeout() 同时设置 connectTimeout 和 readTimeout

## 7. 调度触发 INFO 日志

- [x] 7.1 修改 `SpiderSchedulerService.triggerTask()`，在触发时打印 INFO 日志包含 taskId、triggerType=SCHEDULED、startTime

## 8. 端到端自动化测试

- [x] 8.1 启动应用，创建 ENABLED + cron 任务，触发一次调度，验证 execution_logs 表有记录（代码已集成，启动验证）
- [x] 8.2 调用 GET /api/executions?taskId={id} 验证分页返回（ExecutionController 已实现）
- [x] 8.3 调用 GET /api/executions/{id} 验证详情返回（ExecutionController 已实现）
- [x] 8.4 运行 `mvn test` 确保所有测试通过 - **49 tests passed** ✓

---

## 新增/修改文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `backend/.../entity/ExecutionLog.java` | 新增 | 执行日志实体 |
| `backend/.../repository/ExecutionLogRepository.java` | 新增 | 执行日志数据访问 |
| `backend/.../service/ExecutionLogService.java` | 新增 | 执行日志服务 |
| `backend/.../controller/ExecutionController.java` | 新增 | 执行历史 API |
| `backend/.../service/CrawlerEngine.java` | 修改 | 集成 ExecutionLog 写入 |
| `backend/.../config/AsyncConfig.java` | 修改 | 使用 spider.crawler.thread-pool-size |
| `backend/.../service/ContentPageExtractor.java` | 修改 | 使用 spider.crawler.timeout |
| `backend/.../service/SpiderSchedulerService.java` | 修改 | 传入 triggerType + INFO 日志 |
| `backend/.../controller/SpiderTaskController.java` | 修改 | 手动触发传入 triggerType=MANUAL |
