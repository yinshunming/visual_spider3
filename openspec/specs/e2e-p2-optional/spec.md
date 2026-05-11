# e2e-p2-optional

**日期**: 2026-05-11
**状态**: 已实现

---

## ADDED Requirements

### Requirement: E2E-P2-01 内容状态筛选

系统 SHALL 提供内容状态筛选的 E2E 测试，用例 `e2e-content-status-filter`：

- 覆盖链路：内容列表 → 状态筛选
- 前置条件：PENDING/PUBLISHED/DELETED 各存在数据
- 操作步骤：
  1. 访问 /contents
  2. 使用状态筛选器分别筛选各状态
  3. 验证每种状态只显示对应条数
- 断言：
  - 每种状态筛选后条数准确

### Requirement: E2E-P2-02 内容删除

系统 SHALL 提供内容删除的 E2E 测试，用例 `e2e-content-delete`：

- 覆盖链路：内容列表 → 删除
- 前置条件：存在至少 2 条内容
- 操作步骤：
  1. 访问 /contents
  2. 点击第一条的删除按钮
  3. 确认删除
  4. 验证列表条数减少 1
- 断言：
  - 删除后条数 = 原条数 - 1

### Requirement: E2E-P2-03 任务创建表单验证

系统 SHALL 提供任务创建表单验证的 E2E 测试，用例 `e2e-task-create-validation`：

- 覆盖链路：任务创建 → 表单验证
- 前置条件：无
- 操作步骤：
  1. 访问 /tasks/new
  2. 不填写任何内容直接点击保存
  3. 验证表单错误提示
- 断言：
  - 显示必填字段错误提示

---

## ADDED Scenarios

#### Scenario: E2E-P2-01 状态筛选
- **WHEN** 执行 e2e-content-status-filter 测试
- **THEN** 状态筛选功能正常，每种状态显示正确条数

#### Scenario: E2E-P2-02 内容删除
- **WHEN** 执行 e2e-content-delete 测试
- **THEN** 内容删除成功，列表更新

#### Scenario: E2E-P2-03 表单验证
- **WHEN** 执行 e2e-task-create-validation 测试
- **THEN** 表单验证正常工作，提示必填字段错误