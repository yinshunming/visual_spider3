## ADDED Requirements

### Requirement: 任务与字段关联
SpiderField 通过 task_id 关联到 SpiderTask，一个任务可以有多个字段。

#### Scenario: 创建字段时关联任务
- **WHEN** 创建 SpiderField
- **THEN** task_id 必须指定

#### Scenario: 查询任务时获取字段列表
- **WHEN** 获取 SpiderTask 详情
- **THEN** 同时返回关联的 SpiderField 列表，按 display_order 排序

#### Scenario: 删除任务时级联删除字段
- **WHEN** 删除 SpiderTask
- **THEN** 同时删除关联的所有 SpiderField

### Requirement: 字段与任务的 Cascade 操作
对 SpiderTask 的操作需要考虑关联的 SpiderField。

#### Scenario: 保存任务时保存字段
- **WHEN** 调用 SpiderTaskService.save(task) 并传入字段列表
- **THEN** 同时保存 SpiderField 列表

#### Scenario: 更新任务时更新字段
- **WHEN** 调用 SpiderTaskService.update(task) 并传入字段列表
- **THEN** 更新 SpiderField 列表（先删后插）

#### Scenario: 任务启用前校验字段配置
- **WHEN** 调用 enable() 启用任务
- **THEN** 验证至少有一个字段配置
