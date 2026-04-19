# failure-diagnosis

## ADDED Requirements

### Requirement: View execution history
系统 SHALL 支持查看任务执行历史。

#### Scenario: View history
- **WHEN** 用户查看任务执行历史
- **THEN** 系统返回该任务的所有执行记录

### Requirement: View failure detail
系统 SHALL 支持查看失败详情。

#### Scenario: View failure detail
- **WHEN** 用户点击某条失败记录
- **THEN** 系统展示失败URL、错误类型、错误信息

### Requirement: Manual retry
系统 SHALL 支持手动重试失败任务。

#### Scenario: Retry single
- **WHEN** 用户点击单条失败记录的重试按钮
- **THEN** 系统重新执行该条记录的数据提取

#### Scenario: Retry all
- **WHEN** 用户点击"重试全部"按钮
- **THEN** 系统重新执行所有失败记录
