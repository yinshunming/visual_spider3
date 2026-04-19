# task-config

## ADDED Requirements

### Requirement: Task CRUD
系统 SHALL 提供爬虫任务的创建、读取、更新、删除功能。

#### Scenario: Create task
- **WHEN** 用户填写任务名称、目标URL、调度策略
- **THEN** 系统创建任务并返回任务ID

#### Scenario: Update task
- **WHEN** 用户修改任务配置
- **THEN** 系统更新任务配置

#### Scenario: Delete task
- **WHEN** 用户删除任务
- **THEN** 系统标记任务为DELETED状态

#### Scenario: List tasks
- **WHEN** 用户查看任务列表
- **THEN** 系统返回所有非DELETED状态的任务

### Requirement: List field definition
系统 SHALL 支持定义列表页字段（标题、链接、时间），并映射到 list_data 表。

#### Scenario: Define list fields
- **WHEN** 用户配置列表字段
- **THEN** 系统保存字段定义并关联到任务

### Requirement: Detail field definition
系统 SHALL 支持定义详情页字段（正文、作者、标签），并映射到 detail_data 表。

#### Scenario: Define detail fields
- **WHEN** 用户配置详情字段
- **THEN** 系统保存字段定义并关联到任务

### Requirement: Rule association
系统 SHALL 支持关联列表规则和详情规则到任务。

#### Scenario: Associate rules
- **WHEN** 用户选择列表规则和详情规则
- **THEN** 系统更新任务的 list_rule_id 和 detail_rule_id
