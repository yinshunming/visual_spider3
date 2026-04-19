## Why

构建一个可视化爬虫系统，解决手动配置爬虫规则繁琐、易错的问题。通过可视化的方式让用户通过点击页面元素来生成爬取规则，降低使用门槛，同时支持列表页和详情页的分离规则配置。

## What Changes

- 新增 5 个核心 Domain 的架构定义
- 新增数据库表结构设计（crawl_task, crawl_list_rule, crawl_detail_rule, list_data, detail_data, crawl_log）
- 定义列表规则生成流程：Playwright 渲染 → 截图 → 用户选区 → 候选生成 → 预览
- 定义详情规则生成流程：跳转详情页 → Playwright 渲染 → 用户选区 → 字段规则保存 → 预览
- 定义调度执行流程：Quartz 触发 → 列表提取 → 详情提取 → 分页处理 → Upsert 入库
- 定义失败排查功能：执行历史查看、失败详情、手动重试

## Capabilities

### New Capabilities

- `task-config`: 任务配置管理，包括列表字段和详情字段的定义与映射
- `list-rule`: 列表规则生成，支持容器选择器、分页选择器、列表项选择器
- `detail-rule`: 详情规则生成，支持多字段选择器配置
- `scheduler`: Quartz 调度执行，支持定时爬取和手动触发
- `failure-diagnosis`: 失败排查，记录执行日志并支持手动重试

### Modified Capabilities

<!-- 无现有 capabilities -->

## Impact

- 新增数据库 Migration SQL
- 新增 Spring Boot 项目结构
- 新增 Playwright 和 Jsoup 集成
- 新增 Quartz Scheduler 配置
