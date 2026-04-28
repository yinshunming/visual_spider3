## Context

M2 任务管理里程碑需要在 M1 基础设施之上实现完整的任务 CRUD 和状态管理。SpiderTask 实体已在 M1 中创建骨架。

## Goals / Non-Goals

**Goals:**
- 实现 SpiderTaskController 完整 REST API
- 实现 SpiderTaskService 业务逻辑
- 实现任务状态机（DRAFT ↔ ENABLED/DISABLED）
- 实现 SpiderField 与 SpiderTask 的关联管理
- 提供任务列表分页查询

**Non-Goals:**
- 不实现爬虫执行逻辑（CrawlerEngine - M3）
- 不实现定时调度功能（Scheduler - M6）
- 不实现前端页面

## Decisions

### Decision 1: 状态转换校验
**选择**: 严格状态机校验
**理由**: 只有 DRAFT 可以转为 ENABLED/DISABLED；DISABLED 可以退回 DRAFT；ENABLED 运行中的任务不可直接禁用

### Decision 2: 删除任务时级联删除字段
**选择**: 级联删除 SpiderField
**理由**: SpiderField 依赖 SpiderTask 存在，删除任务时应同时清理关联字段

### Decision 3: DTO 模式
**选择**: 请求/响应使用 DTO
**理由**: 实体直接暴露会有懒加载、循环引用等问题

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| 状态转换合法性校验复杂 | 使用状态模式或规则引擎 |
| 并发状态更新冲突 | 使用乐观锁 @Version |
