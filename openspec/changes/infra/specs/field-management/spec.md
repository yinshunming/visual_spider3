## ADDED Requirements

### Requirement: SpiderField Entity
自定义字段实体，用于定义爬虫任务需要采集的字段

#### Scenario: 创建字段实体
- **WHEN** 开发者创建 SpiderField 实体
- **THEN** 实体包含 id、task_id、field_name、field_label、field_type、selector、selector_type、extract_type、attr_name、required、default_value、display_order 字段

#### Scenario: 字段类型枚举
- **WHEN** 设置字段类型
- **THEN** 值必须为 text、image、link 或 richText 之一

#### Scenario: 选择器类型枚举
- **WHEN** 设置选择器类型
- **THEN** 值必须为 CSS 或 XPATH 之一

#### Scenario: 提取类型枚举
- **WHEN** 设置提取类型
- **THEN** 值必须为 text、attr 或 html 之一

#### Scenario: 属性提取
- **WHEN** extract_type 为 attr
- **THEN** attr_name 指定要提取的属性名（如 href、src）

#### Scenario: 字段关联任务
- **WHEN** 字段关联到任务
- **THEN** task_id 指向 SpiderTask 的 id

#### Scenario: 字段顺序
- **WHEN** 设置字段显示顺序
- **THEN** display_order 小的排在前面

### Requirement: SpiderField Repository
SpiderField 的数据访问接口

#### Scenario: 按任务查询字段
- **WHEN** 查询某任务的所有字段
- **THEN** 提供 findByTaskId 方法，返回按 display_order 排序的列表

#### Scenario: 字段 CRUD
- **WHEN** 调用 SpiderFieldRepository
- **THEN** 提供 save、findById、delete 方法
