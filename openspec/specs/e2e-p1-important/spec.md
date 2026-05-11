# e2e-p1-important

**日期**: 2026-05-11
**状态**: 已实现

---

## ADDED Requirements

### Requirement: E2E-P1-01 内容列表分页

系统 SHALL 提供内容列表分页的 E2E 测试，用例 `e2e-content-pagination`：

- 覆盖链路：内容列表 → 分页
- 前置条件：数据库中至少存在 25 条 ContentItem
- 操作步骤：
  1. 访问 /contents
  2. 验证默认显示 20 条
  3. 点击下一页
  4. 验证显示第 21-25 条
  5. 修改每页条数为 50
  6. 验证显示全部内容
- 断言：
  - 第一页显示 <= 20 条
  - 第二页显示正确范围
  - 总条数 = 25

### Requirement: E2E-P1-02 内容按任务筛选

系统 SHALL 提供内容按任务筛选的 E2E 测试，用例 `e2e-content-filter-by-task`：

- 覆盖链路：内容列表 → 任务筛选
- 前置条件：至少 2 个任务各有内容数据
- 操作步骤：
  1. 访问 /contents
  2. 选择 Task A 筛选
  3. 验证显示 5 条
  4. 选择 Task B 筛选
  5. 验证显示 3 条
  6. 清空筛选
  7. 验证显示全部
- 断言：
  - Task A 筛选后条数 = 5
  - Task B 筛选后条数 = 3
  - 清空后条数 = 8

### Requirement: E2E-P1-03 执行日志查看

系统 SHALL 提供执行日志查看的 E2E 测试，用例 `e2e-execution-log-view`：

- 覆盖链路：执行日志查看
- 前置条件：至少存在 1 条 ExecutionLog
- 操作步骤：
  1. 访问 /tasks
  2. 找到执行过的任务
  3. 点击执行记录按钮
  4. 验证列表显示执行日志
- 断言：
  - 存在执行记录
  - 记录包含开始时间、结束时间、状态
  - 状态为 SUCCESS 或 FAILED

### Requirement: E2E-P1-04 任务启用/停用

系统 SHALL 提供任务启用/停用的 E2E 测试，用例 `e2e-task-enable-disable`：

- 覆盖链路：任务列表 → 启用/停用
- 前置条件：存在一个 DRAFT 或 ENABLED 状态的任务
- 操作步骤：
  1. 访问 /tasks
  2. 找到目标任务
  3. 点击启用按钮
  4. 验证状态变为 ENABLED
  5. 点击停用按钮
  6. 验证状态变为 DISABLED
- 断言：
  - 启用后 status = ENABLED
  - 停用后 status = DISABLED

### Requirement: E2E-P1-05 任务删除

系统 SHALL 提供任务删除的 E2E 测试，用例 `e2e-task-delete`：

- 覆盖链路：任务列表 → 删除
- 前置条件：存在一个 DRAFT 任务
- 操作步骤：
  1. 访问 /tasks
  2. 点击删除按钮
  3. 确认删除对话框
  4. 验证任务从列表消失
- 断言：
  - 删除后任务不存在于列表
  - GET /api/tasks/:id 返回 404

---

## ADDED Scenarios

#### Scenario: E2E-P1-01 内容分页
- **WHEN** 执行 e2e-content-pagination 测试
- **THEN** 分页功能正常，每页条数正确

#### Scenario: E2E-P1-02 任务筛选
- **WHEN** 执行 e2e-content-filter-by-task 测试
- **THEN** 筛选功能正常，显示正确条数

#### Scenario: E2E-P1-03 执行日志
- **WHEN** 执行 e2e-execution-log-view 测试
- **THEN** 执行日志正确显示

#### Scenario: E2E-P1-04 启用停用
- **WHEN** 执行 e2e-task-enable-disable 测试
- **THEN** 状态切换正常

#### Scenario: E2E-P1-05 任务删除
- **WHEN** 执行 e2e-task-delete 测试
- **THEN** 任务删除成功，列表更新