## Why

M2 实现了任务管理，但爬虫执行逻辑尚未实现。目前 `POST /api/tasks/{id}/run` 抛出 `UnsupportedOperationException`。M3 需要实现爬虫核心引擎，完成列表页解析、内容页提取、URL 收集，为 M5（内容管理）和 M6（调度）奠定基础。

## What Changes

- 新增 `CrawlerEngine` 爬虫执行引擎
- 实现列表页解析器 `ListPageParser`：支持 containerSelector、itemUrlSelector
- 实现分页规则 `PaginationRule`：支持 INFINITE_SCROLL、PAGE_NUMBER、NEXT_BUTTON
- 实现内容页提取器 `ContentPageExtractor`：基于 SpiderField 配置提取字段
- 实现直接URL模式 `DirectUrlMode`：使用 seed_urls 直接爬取
- 实现 `POST /api/tasks/{id}/run` 执行接口
- 新增 `ContentService` 保存爬取结果到 ContentItem

## Capabilities

### New Capabilities

- `list-page-parsing`: 列表页解析 - 从列表页提取内容页URL
- `pagination-rule`: 分页规则 - 支持多种分页模式
- `content-extraction`: 内容提取 - 从内容页提取自定义字段
- `crawl-execution`: 爬取执行 - 任务执行入口和流程控制

### Modified Capabilities

- `task-management`: run 接口从 TODO 实现为可用（需修改 SpiderTaskService.run()）

## Impact

- 新增 `CrawlerEngine.java`、`ListPageParser.java`、`ContentPageExtractor.java`、`PaginationRule.java`
- 修改 `SpiderTaskService.run()` 实现
- 新增 `ContentService` 保存内容
- 依赖 M2 的 SpiderTask、SpiderField 已完成
