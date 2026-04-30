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
| status | ENUM | DRAFT / ENABLED / DISABLED / RUNNING |
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
         │    前端直接通过 CDP 获取选择器
         │    不需要后端参与
         │
┌───────▼─────────────────────────────────────────────────────────────┐
│                        Spring Boot Backend                           │
├─────────────────┬─────────────────────────┬─────────────────────────┤
│   任务管理API     │    爬虫执行引擎         │    内容管理API          │
│   CRUD + 调度    │   Jsoup + HttpClient   │    CRUD + 预览         │
│                 │                         │                        │
└─────────────────┴─────────────────────────┴────────────────────────┘
                           │
                    ┌──────▼──────┐
                    │  PostgreSQL  │
                    │  JSONB存储   │
                    └─────────────┘
```

**架构说明**：
- **前端职责**：内嵌浏览器加载页面、元素选择、选择器生成（通过前端 Playwright/CDP 直接获取，不发送请求到后端）
- **后端职责**：任务配置存储、爬虫执行引擎、内容管理
- **前后端分离**：选择器生成在前端完成，后端只存储最终配置

---

## 5. 可视化配置流程

### 5.1 列表页模式

```
1. 选择"列表页"模式
2. 输入列表页URL → 前端内嵌浏览器直接加载页面
3. 配置列表页规则：
   a. 点击"选择容器" → 前端CDP直接获取元素CSS/XPath → 生成containerSelector
   b. 点击"选择链接" → 前端CDP直接获取元素CSS/XPath → 生成itemUrlSelector
4. 配置分页规则（可选）
5. 配置内容页字段：
   a. 从列表页点击进入内容页（或直接输入内容页URL）
   b. 点击页面元素配置字段（标题→点击h1，正文→点击div.content）
   c. 每个字段前端CDP直接生成CSS/XPath，可手动调整
6. 保存任务（选择器配置发送到后端存储）
```

**关键区别**：页面加载和选择器生成都在前端完成，后端只负责存储最终配置

### 5.2 直接URL模式

```
1. 选择"直接URL"模式
2. 输入/导入内容页URL列表（支持粘贴、CSV导入）
3. 配置内容页字段（同5.1步骤5-6，前端CDP直接生成选择器）
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
│       │   └── ContentController.java
│       ├── service/
│       │   ├── SpiderTaskService.java
│       │   ├── CrawlerEngine.java          # M3: 爬虫执行引擎
│       │   ├── ListPageParser.java         # M3: 列表页解析
│       │   ├── ContentPageExtractor.java   # M3: 内容页提取
│       │   ├── PaginationRule.java         # M3: 分页规则
│       │   ├── DirectUrlParser.java        # M3: 直接URL模式
│       │   └── ContentService.java
│       ├── config/
│       │   └── AsyncConfig.java            # M3: 异步配置
│       └── exception/
│           └── CrawlException.java          # M3: 爬虫异常
│       ├── entity/
│       │   ├── SpiderTask.java
│       │   ├── SpiderField.java
│       │   └── ContentItem.java
│       ├── repository/
│       │   ├── SpiderTaskRepository.java
│       │   ├── SpiderFieldRepository.java
│       │   └── ContentItemRepository.java
│       └── dto/
│           └── (Request/Response DTOs)
├── frontend/
│   └── src/
│       ├── views/
│       │   ├── TaskList.vue
│       │   ├── TaskConfig.vue
│       │   └── ContentManage.vue
│       ├── components/
│       │   └── EmbeddedBrowser.vue          # 内嵌Chromium，前端CDP直接获取选择器
│       └── api/
│           └── (API调用，仅CRUD和任务执行，无CDP交互)
└── docs/
    └── superpowers/
        └── specs/
            └── 2026-04-26-visual-spider-design.md
```

**说明**：后端不再包含 CdpController、CdpService，选择器生成全部由前端 EmbeddedBrowser.vue 通过 Playwright/CDP 直接完成

---

## 8. API设计

### 8.1 任务管理

| 方法 | 路径 | 说明 |
|-----|------|-----|
| GET | /api/tasks | 分页查询任务列表 |
| GET | /api/tasks/{id} | 获取任务详情（含字段定义） |
| POST | /api/tasks | 创建任务 |
| PUT | /api/tasks/{id} | 更新任务 |
| DELETE | /api/tasks/{id} | 删除任务 |
| POST | /api/tasks/{id}/enable | 启用任务 |
| POST | /api/tasks/{id}/disable | 停用任务 |
| POST | /api/tasks/{id}/run | 手动执行任务（M3） |

### 8.2 爬虫执行（M3）

#### 8.2.1 执行入口
- **POST /api/tasks/{id}/run**: 异步启动爬虫任务
  - 任务状态必须为 ENABLED
  - 返回 200 OK 后立即开始异步爬取
  - 爬取期间任务状态变为 RUNNING
  - 爬取完成或失败后恢复为 ENABLED

#### 8.2.2 执行引擎（CrawlerEngine）
- **LIST_PAGE 模式**: 
  - 调用 ListPageParser 解析列表页
  - 支持分页规则（INFINITE_SCROLL / PAGE_NUMBER / NEXT_BUTTON）
  - 从列表页提取内容页 URL
  - 调用 ContentPageExtractor 提取每个内容页字段
  
- **DIRECT_URL 模式**:
  - 直接使用 seedUrls 作为内容页 URL
  - 跳过列表页解析

#### 8.2.3 内容页提取（ContentPageExtractor）
- 根据 SpiderField 配置提取字段值
- 支持 CSS 和 XPath 选择器
- 支持三种提取类型：text、attr、html
- 图片/链接字段自动解析相对 URL 为绝对 URL
- 选择器未匹配时返回默认值

#### 8.2.4 分页规则（PaginationRule）
- **INFINITE_SCROLL**: 容器选择器返回空时停止
- **PAGE_NUMBER**: 按 pagePattern 生成页码 URL，{page} 占位符替换
- **NEXT_BUTTON**: 查找下一页按钮元素获取下一页 URL
- **停止条件**: 容器为空 或 达到 maxPages 限制

### 8.3 可视化配置

**说明**：选择器生成由前端 EmbeddedBrowser.vue 通过 Playwright/CDP 直接完成，不涉及后端 API

| 方法 | 路径 | 说明 |
|-----|------|------|
| （无） | - | 选择器生成在前端完成，无需后端 API |

### 8.4 内容管理

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
