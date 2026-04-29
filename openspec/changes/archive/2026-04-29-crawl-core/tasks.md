## 1. CrawlerEngine 核心实现

- [x] 1.1 创建 CrawlerEngine.java 服务类
- [x] 1.2 实现 execute(Long taskId) 入口方法
- [x] 1.3 实现根据 urlMode 路由到对应解析器
- [x] 1.4 实现任务状态检查 (ENABLED 才能执行)
- [x] 1.5 实现异步执行 @Async

## 2. ListPageParser 列表页解析

- [x] 2.1 创建 ListPageParser 解析器类
- [x] 2.2 实现 fetchListPage(String url) 获取列表页
- [x] 2.3 实现 parseContainers(Document doc, String containerSelector) 查找容器
- [x] 2.4 实现 extractUrls(Elements containers, String itemUrlSelector) 提取内容页URL
- [x] 2.5 实现链接去重

## 3. PaginationRule 分页规则

- [x] 3.1 创建 PaginationRule 解析类
- [x] 3.2 实现 INFINITE_SCROLL 类型处理
- [x] 3.3 实现 PAGE_NUMBER 类型处理 (pagePattern 模板替换)
- [x] 3.4 实现 NEXT_BUTTON 类型处理 (nextPageSelector)
- [x] 3.5 实现分页停止条件判断

## 4. ContentPageExtractor 内容页提取

- [x] 4.1 创建 ContentPageExtractor 内容提取器
- [x] 4.2 实现 fetchContentPage(String url) 获取内容页
- [x] 4.3 实现 extractText(Element el, String selector) 文本提取
- [x] 4.4 实现 extractAttr(Element el, String selector, String attrName) 属性提取
- [x] 4.5 实现 extractHtml(Element el, String selector) HTML提取
- [x] 4.6 实现 XPath 选择器支持

## 5. DirectUrlParser 直接URL模式

- [x] 5.1 创建 DirectUrlParser 直接URL解析器
- [x] 5.2 实现 getUrls(String[] seedUrls) 直接返回seedUrls
- [x] 5.3 实现URL去重

## 6. ContentService 内容保存

- [x] 6.1 创建 ContentService 内容服务
- [x] 6.2 实现 saveContent(Long taskId, String sourceUrl, Map<String, Object> fields) 保存内容项
- [x] 6.3 实现批量保存优化

## 7. SpiderTaskService.run() 实现

- [x] 7.1 修改 SpiderTaskService.run() 调用 CrawlerEngine
- [x] 7.2 实现任务状态变更 (RUNNING 状态)
- [x] 7.3 实现执行完成/失败的状态回滚

## 8. 异常处理与日志

- [x] 8.1 创建 CrawlException 自定义异常
- [x] 8.2 实现单页失败不影响整体的错误处理
- [x] 8.3 实现爬取日志记录

---

**更新日期**: 2026-04-29
**完成状态**: 全部完成
