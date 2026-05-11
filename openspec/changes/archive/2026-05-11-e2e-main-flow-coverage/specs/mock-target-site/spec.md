# mock-target-site

**日期**: 2026-05-10
**状态**: 待实现

---

## ADDED Requirements

### Requirement: Mock HTTP Server

系统 SHALL 提供本地 Mock HTTP Server，用于模拟目标网站：

- 技术选型：Express.js (Node.js) 或 Python http.server
- 端口：动态分配可用端口（避免冲突）
- 启动时间：测试前启动，测试结束后关闭
- 功能：
  - 响应 `/list.html` 返回列表页 HTML
  - 响应 `/content-page-1.html` 返回内容页 1 HTML
  - 响应 `/content-page-2.html` 返回内容页 2 HTML
  - 返回正确的 HTTP status code

### Requirement: 列表页 HTML Fixture

系统 SHALL 提供列表页 HTML Fixture：

- 路径：`fixtures/mock-server/list-page.html`
- 结构：
  - 容器选择器：`div.article-list > div.item`
  - 链接选择器：`a.title`
  - 包含至少 2 个列表项
- 作用：模拟 LIST_PAGE 模式的目标网站列表页

### Requirement: 内容页 HTML Fixtures

系统 SHALL 提供内容页 HTML Fixtures：

- 路径：
  - `fixtures/mock-server/content-page-1.html`
  - `fixtures/mock-server/content-page-2.html`
- 结构：
  - 标题选择器：`h1.title`
  - 正文选择器：`div.article-content`
  - 包含真实内容（中文、特殊字符）
- 作用：模拟内容页的 HTML 结构

### Requirement: Mock Server Fixture 启动/停止

系统 SHALL 提供 Mock Server 的启动和停止机制：

- globalSetup：所有测试前启动一次
- globalTeardown：所有测试后停止
- 或在每个测试文件的 beforeAll/afterAll 中管理

---

## ADDED Scenarios

#### Scenario: Mock Server 启动
- **WHEN** E2E 测试开始执行
- **THEN** Mock Server 启动并监听在可用端口

#### Scenario: 列表页请求
- **WHEN** 爬虫请求 `/list.html`
- **THEN** 返回包含列表结构的 HTML

#### Scenario: 内容页请求
- **WHEN** 爬虫请求 `/content-page-1.html`
- **THEN** 返回包含标题和正文的 HTML

#### Scenario: Mock Server 停止
- **WHEN** E2E 测试全部完成
- **THEN** Mock Server 正确关闭，释放端口

---

## Fixture HTML 示例

### list-page.html

```html
<!DOCTYPE html>
<html>
<head><title>Mock List Page</title></head>
<body>
  <div class="article-list">
    <div class="item">
      <a class="title" href="/content-page-1.html">测试文章标题1</a>
    </div>
    <div class="item">
      <a class="title" href="/content-page-2.html">测试文章标题2</a>
    </div>
  </div>
</body>
</html>
```

### content-page-1.html

```html
<!DOCTYPE html>
<html>
<head><title>测试文章标题1</title></head>
<body>
  <h1 class="title">测试文章标题1</h1>
  <div class="article-content">
    <p>这里是文章正文内容，包含中文和特殊字符。</p>
  </div>
</body>
</html>
```
