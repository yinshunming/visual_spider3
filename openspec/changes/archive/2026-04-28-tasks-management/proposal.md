## Why

M2 里程碑需要实现完整的任务管理功能，包括爬虫任务的 CRUD、状态管理和 SpiderField 关联管理。这是运营人员配置爬虫规则的核心功能。

## What Changes

- 实现 SpiderTaskController 的完整 REST API
- 实现 SpiderTaskService 的业务逻辑
- 实现任务状态机（DRAFT → ENABLED/DISABLED）
- 实现任务列表分页查询
- 实现任务启用/停用接口
- 实现 SpiderField 与 SpiderTask 的关联管理
- 新增 SpiderTask 相关 DTO 类

## Capabilities

### New Capabilities

- `task-status-machine`: 任务状态机，支持 DRAFT → ENABLED/DISABLED 状态转换
- `task-field-association`: SpiderTask 与 SpiderField 的关联管理

### Modified Capabilities

- `task-management`: 扩展现有任务管理能力，从骨架升级为完整实现

## Impact

- 修改 `SpiderTaskController.java` - 实现完整 API
- 修改 `SpiderTaskService.java` - 实现业务逻辑
- 新增 DTO 类用于请求/响应
- 影响 `/api/tasks` 端点
