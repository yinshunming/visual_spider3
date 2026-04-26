# Visual Spider 开发路线图

**更新日期**: 2026-04-26

---

## 里程碑总览

| 里程碑 | 名称 | 依赖 |
|--------|------|------|
| M1 | 基础设施 | - |
| M2 | 任务管理 | M1 |
| M3 | 爬虫核心 | M2 |
| M4 | 可视化配置 | M2 |
| M5 | 内容管理 | M3 |
| M6 | 调度与发布 | M5 |

---

## M1: 基础设施

**目标**: 完成项目脚手架、数据库表结构、基础配置

### 交付物
- [ ] Spring Boot 项目初始化（Maven/Gradle）
- [ ] PostgreSQL 数据库连接配置
- [ ] JPA Entity 生成（SpiderTask、SpiderField、ContentItem）
- [ ] Repository 层基础 CRUD
- [ ] 基础 REST API 骨架

### 目录结构
```
backend/src/main/java/com/example/visualspider/
├── controller/    # API 骨架
├── service/       # 基础 Service
├── entity/        # 3张 Entity
└── repository/    # 3个 Repository
```

---

## M2: 任务管理

**目标**: 完成爬虫任务的完整 CRUD 和状态管理

### 交付物
- [ ] SpiderTaskController - 任务 CRUD API
- [ ] SpiderTaskService - 业务逻辑
- [ ] 任务状态机（DRAFT → ENABLED/DISABLED）
- [ ] 任务列表分页查询
- [ ] 任务启用/停用接口
- [ ] SpiderField 关联管理

### API
| 方法 | 路径 | 说明 |
|-----|------|------|
| GET | /api/tasks | 分页查询任务列表 |
| GET | /api/tasks/{id} | 获取任务详情 |
| POST | /api/tasks | 创建任务 |
| PUT | /api/tasks/{id} | 更新任务 |
| DELETE | /api/tasks/{id} | 删除任务 |
| POST | /api/tasks/{id}/enable | 启用任务 |
| POST | /api/tasks/{id}/disable | 停用任务 |

---

## M3: 爬虫核心

**目标**: 完成列表页解析、内容页解析、URL 收集

### 交付物
- [ ] CrawlerEngine - 爬虫执行引擎
- [ ] 列表页解析（containerSelector、itemUrlSelector）
- [ ] 分页规则支持（INFINITE_SCROLL / PAGE_NUMBER / NEXT_BUTTON）
- [ ] 内容页字段提取（CSS/XPath 选择器）
- [ ] 直接URL模式支持（seed_urls）
- [ ] 任务执行接口 POST /api/tasks/{id}/run

### 规则数据结构
```json
// list_page_rule
{
  "containerSelector": "div.article-list > div.item",
  "itemUrlSelector": "a.title",
  "paginationRule": { ... }
}

// content_page_rule
{
  "fields": [
    { "fieldName": "title", "selector": "h1", "extractType": "text" },
    { "fieldName": "content", "selector": "div.content", "extractType": "html" }
  ]
}
```

---

## M4: 可视化配置

**目标**: 完成内嵌浏览器、元素选择、选择器生成

### 交付物
- [ ] EmbeddedBrowser.vue - Vue3 内嵌 Chromium 组件
- [ ] CdpController - DevTools Protocol 交互 API
- [ ] CdpService - Playwright/Chrome CDP 服务
- [ ] 页面加载与 DOM 快照返回
- [ ] 元素点击位置 → CSS/XPath 选择器生成
- [ ] 选择器预览与调试

### API
| 方法 | 路径 | 说明 |
|-----|------|------|
| POST | /api/cdp/load-page | 加载页面并返回DOM快照 |
| POST | /api/cdp/generate-selector | 根据点击位置生成选择器 |
| POST | /api/cdp/preview-selector | 预览选择器匹配结果 |

---

## M5: 内容管理

**目标**: 完成内容存储、预览、编辑、导出

### 交付物
- [ ] ContentController - 内容 CRUD API
- [ ] ContentService - 内容业务逻辑
- [ ] ContentItemRepository - 分页查询
- [ ] 内容状态管理（PUBLISHED / PENDING / DELETED）
- [ ] 内容预览界面
- [ ] 内容编辑功能
- [ ] Excel/CSV 导出

### API
| 方法 | 路径 | 说明 |
|-----|------|------|
| GET | /api/contents | 分页查询内容列表 |
| GET | /api/contents/{id} | 获取内容详情 |
| PUT | /api/contents/{id} | 更新内容 |
| DELETE | /api/contents/{id} | 删除内容 |
| GET | /api/contents/export | 导出内容 |

---

## M6: 调度与发布

**目标**: 完成定时任务、系统优化、稳定发布

### 交付物
- [ ] Spring @Scheduler 集成
- [ ] Cron 表达式配置与解析
- [ ] 定时任务调度执行
- [ ] 任务执行日志
- [ ] 错误处理与重试机制
- [ ] 性能优化（并发爬取、连接池）
- [ ] 监控与告警（可选）

### 配置示例
```yaml
spider:
  schedule:
    enabled: true
    thread-pool-size: 5
  crawler:
    timeout: 30000
    retry-times: 3
```

---

## 执行顺序

```
M1 (基础设施)
    ↓
M2 (任务管理)     M4 (可视化配置)
    ↓               ↓
M3 (爬虫核心) ────┘
    ↓
M5 (内容管理)
    ↓
M6 (调度与发布)
```

**说明**:
- M2 和 M4 可并行开发
- M3 依赖 M2 的任务模型
- M5 依赖 M3 的爬取能力
- M6 是最后阶段

---

## 里程碑进度

| 里程碑 | 状态 | 完成日期 |
|--------|------|----------|
| M1 | ⬜ 未开始 | - |
| M2 | ⬜ 未开始 | - |
| M3 | ⬜ 未开始 | - |
| M4 | ⬜ 未开始 | - |
| M5 | ⬜ 未开始 | - |
| M6 | ⬜ 未开始 | - |
