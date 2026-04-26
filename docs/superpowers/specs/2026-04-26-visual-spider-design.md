# Visual Spider 可视化爬虫系统设计

**日期**: 2026-04-26
**状态**: 设计中

---

## 1. 概述

### 1.1 目标
为运营同学提供可视化配置爬虫的系统，无需编写代码，通过浏览器点击操作自动生成爬虫规则，支持爬取各类新闻/文章类网站内容。

### 1.2 核心能力
- **可视化配置**: 通过内嵌浏览器点击页面元素，自动生成 CSS/XPath 选择器
- **双模式支持**: 支持列表页模式（新浪NBA列表→内容页）和直接URL模式（无列表页）
- **自定义字段**: 运营可动态添加/配置需要爬取的字段
- **内容管理**: 爬取内容存储到数据库，前端预览/编辑/导出
- **混合调度**: 支持手动触发和定时Cron配置

---

## 2. 实体模型

### 2.1 SpiderTask（爬虫任务）

| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(255) | 任务名称 |
| description | TEXT | 任务描述 |
| url_mode | ENUM | LIST_PAGE / DIRECT_URL |
| list_page_url | VARCHAR(2048) | 列表页URL（LIST_PAGE模式） |
| list_page_rule | JSONB | 列表页解析规则 |
| seed_urls | TEXT[] | 直接内容页URL列表（DIRECT_URL模式） |
| content_page_rule | JSONB | 内容页解析规则 |
| schedule_cron | VARCHAR(100) | Cron表达式（可选） |
| status | ENUM | DRAFT / ENABLED / DISABLED |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 2.2 SpiderField（自定义字段）

| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键 |
| task_id | BIGINT | 关联任务ID |
| field_name | VARCHAR(100) | 字段标识（英文） |
| field_label | VARCHAR(255) | 字段显示名 |
| field_type | ENUM | text / image / link / richText |
| selector | VARCHAR(500) | CSS/XPath选择器 |
| selector_type | ENUM | CSS / XPATH |
| extract_type | ENUM | text / attr / html |
| attr_name | VARCHAR(100) | 属性名（如href、src） |
| required | BOOLEAN | 是否必填 |
| default_value | VARCHAR(500) | 默认值 |
| display_order | INT | 展示顺序 |

### 2.3 ContentItem（爬取内容）

| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键 |
| task_id | BIGINT | 关联任务ID |
| source_url | VARCHAR(2048) | 来源URL |
| fields | JSONB | 动态字段值 |
| raw_html | TEXT | 原始HTML（可选） |
| status | ENUM | PUBLISHED / PENDING / DELETED |
| published_at | TIMESTAMP | 发布时间 |
| created_at | TIMESTAMP | 创建时间 |

---

## 3. 规则数据结构

### 3.1 列表页规则（list_page_rule）

```json
{
  "containerSelector": "div.article-list > div.item",
  "itemUrlSelector": "a.title",
  "paginationRule": {
    "type": "INFINITE_SCROLL | PAGE_NUMBER | NEXT_BUTTON",
    "nextPageSelector": "a.next",
    "pagePattern": "/page/{page}"
  }
}
```

### 3.2 内容页规则（content_page_rule）

```json
{
  "fields": [
    {
      "fieldName": "title",
      "fieldLabel": "标题",
      "selector": "h1.title",
      "selectorType": "CSS",
      "extractType": "text"
    },
    {
      "fieldName": "content",
      "fieldLabel": "正文",
      "selector": "div.article-content",
      "selectorType": "CSS",
      "extractType": "html"
    }
  ]
}
```

---

## 4. 系统架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Vue3 Frontend                                │
├────────────────┬─────────────────────────┬─────────────────────────┤
│   任务管理      │     可视化配置           │      内容管理            │
│   列表/启停     │   内嵌Chromium(CDP)     │   内容列表/预览/导出     │
└───────┬────────┴───────────┬─────────────┴─────────────────────────┘
        │                    │
        │    DevTools Protocol
        │    元素点击 → CSS/XPath
        │
┌───────▼─────────────────────────────────────────────────────────────┐
│                        Spring Boot Backend                           │
├─────────────────┬─────────────────────────┬─────────────────────────┤
│   任务管理API     │    爬虫执行引擎          │    内容管理API          │
│   CRUD + 调度    │   Jsoup + HttpClient    │    CRUD + 预览         │
│                 │    CdpService           │                        │
│                 │   (Playwright/Chrome)    │                        │
└─────────────────┴─────────────────────────┴────────────────────────┘
                          │
                   ┌──────▼──────┐
                   │  PostgreSQL  │
                   │  JSONB存储   │
                   └─────────────┘
```

---

## 5. 可视化配置流程

### 5.1 列表页模式

```
1. 选择"列表页"模式
2. 输入列表页URL → 后端用Playwright加载 → 返回页面快照
3. 配置列表页规则：
   a. 点击"选择容器" → 点击页面上一个列表项 → 生成containerSelector
   b. 点击"选择链接" → 点击某篇文章链接 → 生成itemUrlSelector
4. 配置分页规则（可选）
5. 配置内容页字段：
   a. 从列表页点击进入内容页（或直接输入内容页URL）
   b. 点击页面元素配置字段（标题→点击h1，正文→点击div.content）
   c. 每个字段实时生成CSS/XPath，可手动调整
6. 保存任务
```

### 5.2 直接URL模式

```
1. 选择"直接URL"模式
2. 输入/导入内容页URL列表（支持粘贴、CSV导入）
3. 配置内容页字段（同5.1的b、c步骤）
4. 保存任务
```

---

## 6. 技术选型

| 组件 | 技术 | 说明 |
|-----|------|------|
| 前端框架 | Vue3 + Vite |  |
| UI组件库 | Element Plus |  |
| 内嵌浏览器 | Chrome DevTools Protocol | 通过 Puppeteer/Playwright 实现 |
| 后端框架 | Spring Boot 3.x |  |
| 爬虫内核 | Jsoup + HttpClient |  |
| 数据库 | PostgreSQL | JSONB存储动态字段 |
| 调度 | Spring @Scheduler |  |
| API风格 | RESTful |  |

---

## 7. 目录结构

```
visual-spider/
├── backend/
│   └── src/main/java/com/example/visualspider/
│       ├── controller/
│       │   ├── SpiderTaskController.java
│       │   ├── ContentController.java
│       │   └── CdpController.java
│       ├── service/
│       │   ├── SpiderTaskService.java
│       │   ├── CrawlerEngine.java
│       │   ├── ContentService.java
│       │   └── CdpService.java
│       ├── entity/
│       │   ├── SpiderTask.java
│       │   ├── SpiderField.java
│       │   └── ContentItem.java
│       ├── repository/
│       │   ├── SpiderTaskRepository.java
│       │   ├── SpiderFieldRepository.java
│       │   └── ContentItemRepository.java
│       ├── dto/
│       │   └── (Request/Response DTOs)
│       └── config/
│           └── (配置类)
├── frontend/
│   └── src/
│       ├── views/
│       │   ├── TaskList.vue
│       │   ├── TaskConfig.vue
│       │   └── ContentManage.vue
│       ├── components/
│       │   └── EmbeddedBrowser.vue
│       └── api/
│           └── (API调用)
└── docs/
    └── superpowers/
        └── specs/
            └── 2026-04-26-visual-spider-design.md
```

---

## 8. API设计

### 8.1 任务管理

| 方法 | 路径 | 说明 |
|-----|------|------|
| GET | /api/tasks | 分页查询任务列表 |
| GET | /api/tasks/{id} | 获取任务详情（含字段定义） |
| POST | /api/tasks | 创建任务 |
| PUT | /api/tasks/{id} | 更新任务 |
| DELETE | /api/tasks/{id} | 删除任务 |
| POST | /api/tasks/{id}/enable | 启用任务 |
| POST | /api/tasks/{id}/disable | 停用任务 |
| POST | /api/tasks/{id}/run | 手动执行任务 |

### 8.2 可视化配置

| 方法 | 路径 | 说明 |
|-----|------|------|
| POST | /api/cdp/load-page | 加载页面并返回DOM快照 |
| POST | /api/cdp/generate-selector | 根据点击位置生成选择器 |
| POST | /api/cdp/preview-selector | 预览选择器匹配结果 |

### 8.3 内容管理

| 方法 | 路径 | 说明 |
|-----|------|------|
| GET | /api/contents | 分页查询内容列表 |
| GET | /api/contents/{id} | 获取内容详情 |
| PUT | /api/contents/{id} | 更新内容 |
| DELETE | /api/contents/{id} | 删除内容 |
| GET | /api/contents/export | 导出内容（Excel/CSV） |

---

## 9. 下一步

- 用户确认设计文档
- 使用 `/writing-plans` 生成详细实现计划
