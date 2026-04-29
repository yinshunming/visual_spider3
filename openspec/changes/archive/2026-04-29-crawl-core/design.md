## Context

M2 完成了任务管理（SpiderTask CRUD、状态机、字段管理），`POST /api/tasks/{id}/run` 接口目前抛出 `UnsupportedOperationException`。M3 需要实现爬虫核心引擎，完成从列表页提取 URL、从内容页提取字段的核心逻辑。

**技术背景**：
- 使用 Jsoup + HttpClient 进行 HTTP 请求和 HTML 解析
- SpiderField.selector 支持 CSS 和 XPath 选择器
- SpiderField.extractType 支持 text、attr、html 三种提取方式
- 列表页规则（list_page_rule）和内容页规则（content_page_rule）已定义为 JSONB 结构

**Stakeholders**：
- 运营同学（主要用户）
- M5（内容管理）依赖爬取结果
- M6（调度）依赖执行引擎

## Goals / Non-Goals

**Goals:**
- 实现 CrawlerEngine 爬虫执行引擎
- 实现列表页解析（containerSelector → 容器，itemUrlSelector → 内容页链接）
- 实现分页规则（INFINITE_SCROLL / PAGE_NUMBER / NEXT_BUTTON）
- 实现内容页字段提取（CSS/XPath → text/attr/html）
- 实现直接URL模式（seed_urls）
- 实现任务执行接口 POST /api/tasks/{id}/run

**Non-Goals:**
- 不实现定时调度（M6）
- 不实现并发爬取（M6 性能优化）
- 不实现错误重试机制（M6）
- 不实现前端页面（M4）

## Decisions

### Decision 1: 爬虫引擎架构 - 策略模式
**选择**: CrawlerEngine 作为入口，委托给 ListPageParser / DirectUrlParser
**理由**: 两种 URL 模式逻辑不同，分离便于维护和扩展

### Decision 2: HTML 解析库 - Jsoup
**选择**: Jsoup
**理由**: 轻量、支持 CSS 选择器、Spring Boot 生态成熟
**替代方案**:
- HtmlUnit: 太重，适合复杂 JS 渲染场景
- Jsoup + 自定义 XPath: Jsoup 不支持 XPath，仅用 CSS 选择器

### Decision 3: CSS vs XPath 选择器
**选择**: CSS 选择器优先，XPath 作为补充
**理由**: CSS 选择器更简洁，开发者更熟悉；XPath 用于复杂层级关系
**实现**: 使用 Jsoup 的 CSS 选择器 + javax.xml.xpath.XPathFactory 处理 XPath

### Decision 4: 任务执行方式 - 异步
**选择**: @Async 异步执行，立即返回
**理由**: 爬取可能是长时间操作，同步等待会导致超时
**替代方案**:
- 返回 Future/Promise: 简单但前端轮询麻烦
- WebSocket: 复杂度高

### Decision 5: 内容页提取流程
**选择**: 遍历 SpiderField 列表，逐一提取
**理由**: 每个字段独立提取，便于调试和错误处理
**提取顺序**: 按 display_order 排序

### Decision 6: 分页处理策略
**选择**: 分页规则内嵌在 ListPageParser
**理由**: 不同分页类型逻辑差异大，内聚便于管理
**分页类型**:
- INFINITE_SCROLL: 检测是否还有新内容（无固定分页）
- PAGE_NUMBER: URL 模板 /page/{page}
- NEXT_BUTTON: 点击"下一页"按钮选择器

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| 目标网站反爬虫 | User-Agent 随机化、请求间隔、代理池（M6 考虑） |
| 列表页结构不固定 | 提供 selector 调试接口（M4） |
| 内容页加载需要 JS | 静态 HTML 解析，复杂 JS 站需要 M4 的 Playwright |
| 字段提取失败 | 单字段失败不影响其他字段，记录错误 |
| 编码问题 | Jsoup 自动检测编码，但特殊编码需手动处理 |

## Migration Plan

1. 实现 CrawlerEngine + ListPageParser + ContentPageExtractor
2. 修改 SpiderTaskService.run() 调用引擎
3. 实现 ContentService 保存结果
4. 单元测试覆盖核心逻辑
5. 手动测试完整流程

## Open Questions

- 是否需要支持登录/Cookie？
- 爬取速率限制策略？
- 失败重试次数和间隔？
- 是否需要清理已爬取URL（去重）？
