## ADDED Requirements

### Requirement: 任务状态枚举值
SpiderTask 的状态必须是 DRAFT、ENABLED、DISABLED 之一。

#### Scenario: 默认状态为草稿
- **WHEN** 创建新任务
- **THEN** status 默认为 DRAFT

#### Scenario: 状态枚举有效性
- **WHEN** 设置任务状态
- **THEN** 值必须为 DRAFT、ENABLED 或 DISABLED 之一

### Requirement: 状态转换规则
任务状态必须遵循以下转换规则：
- DRAFT → ENABLED（启用）
- DRAFT → DISABLED（禁用）
- DISABLED → DRAFT（取消）
- ENABLED → DISABLED（停止）
- DISABLED → ENABLED（重新启用）

#### Scenario: 草稿任务可以启用
- **WHEN** 调用 enable() 且当前状态为 DRAFT
- **THEN** 状态变为 ENABLED

#### Scenario: 草稿任务可以禁用
- **WHEN** 调用 disable() 且当前状态为 DRAFT
- **THEN** 状态变为 DISABLED

#### Scenario: 已禁用任务可以启用
- **WHEN** 调用 enable() 且当前状态为 DISABLED
- **THEN** 状态变为 ENABLED

#### Scenario: 已启用任务可以禁用
- **WHEN** 调用 disable() 且当前状态为 ENABLED
- **THEN** 状态变为 DISABLED

#### Scenario: 禁用任务可以取消
- **WHEN** 调用 resetToDraft() 且当前状态为 DISABLED
- **THEN** 状态变为 DRAFT

#### Scenario: 非 DISABLED 状态不能取消
- **WHEN** 调用 resetToDraft() 且当前状态为 ENABLED 或 DRAFT
- **THEN** 抛出 IllegalStateException
