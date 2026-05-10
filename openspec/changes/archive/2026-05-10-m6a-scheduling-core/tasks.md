# M6A：调度核心 - 实现任务

**Change**: `m6a-scheduling-core`
**状态**: 实现完成

---

## 1. 配置项引入

- [x] 1.1 在 `application.yml` 新增 `spider.schedule.enabled`（默认 true）、`spider.schedule.thread-pool-size`（默认 3）、`spider.crawler.retry-times`（默认 2）、`spider.crawler.timeout`（默认 30000）配置项

## 2. SchedulerConfig 配置类

- [x] 2.1 创建 `SchedulerConfig.java`，配置 `ThreadPoolTaskScheduler` Bean，线程池大小从 `spider.schedule.thread-pool-size` 读取，Bean 名称为 `schedulerExecutor`
- [x] 2.2 在启动类或配置类添加 `@EnableScheduling` 注解

## 3. SpiderSchedulerService 实现

- [x] 3.1 创建 `SpiderSchedulerService.java`，注入 `SpiderTaskRepository`、`CrawlerEngine`、`SpiderTaskService`
- [x] 3.2 实现 `@Scheduled(fixedRate = 60000)` 扫描方法，查询条件：ENABLED + schedule_cron IS NOT NULL + status != RUNNING
- [x] 3.3 对每条任务用 `CronExpression.next()` 计算下次触发时间，当前时间 >= 下次触发时间则触发
- [x] 3.4 触发前用乐观锁检查 `status != RUNNING`，更新为 RUNNING 后调用 `CrawlerEngine.executeAsync()`（@Async）

## 4. Cron 合法性校验

- [x] 4.1 在 `SpiderTaskService.save()` 和 `update()` 方法中，保存时调用 `CronExpression.parse()` 校验 `schedule_cron` 合法性
- [x] 4.2 非法 Cron（如 `abc`）抛出 `IllegalArgumentException`，被 `GlobalExceptionHandler` 捕获后返回 400 Bad Request
- [x] 4.3 每秒级 cron（`* * * * *`）应被拒绝，提示最小间隔为 1 分钟

## 5. 重试机制实现

- [x] 5.1 在 `SpiderSchedulerService` 触发爬取的方法中，实现手动循环重试：`for (int i = 0; i <= retryTimes; i++)`
- [x] 5.2 重试间隔采用指数退避：`Thread.sleep(1000L * (1 << i))`（1s → 2s → 4s）
- [x] 5.3 仅 `CrawlException`（网络异常）触发重试，配置错误、校验失败不重试
- [x] 5.4 所有重试耗尽后，将任务状态回滚为 ENABLED，打印 error 日志

## 6. 验证与测试

- [ ] 6.1 启动应用无报错，确认 `schedulerExecutor` Bean 已注册
- [ ] 6.2 测试非法 Cron 保存返回 400
- [ ] 6.3 测试每秒级 Cron 保存返回 400（最小间隔 1 分钟）
- [ ] 6.4 创建 ENABLED + cron 任务，等待定时触发，确认任务状态变为 RUNNING
- [ ] 6.5 模拟网络异常，验证重试 2 次后状态回滚 ENABLED
- [ ] 6.6 并发测试：同一任务同时触发，只有一个执行

---

## 新增/修改文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `backend/src/main/resources/application.yml` | 修改 | 新增 spider.schedule.* 和 spider.crawler.* 配置 |
| `backend/.../config/SchedulerConfig.java` | 新增 | ThreadPoolTaskScheduler Bean |
| `backend/.../VisualSpiderApplication.java` | 修改 | 添加 @EnableScheduling |
| `backend/.../service/SpiderSchedulerService.java` | 新增 | 定时扫描 + 乐观锁 + 重试 + 触发 |
| `backend/.../service/SpiderTaskService.java` | 修改 | save/update 添加 CronExpression.parse() 校验 |
| `backend/.../exception/GlobalExceptionHandler.java` | 新增 | IllegalArgumentException → 400 |
